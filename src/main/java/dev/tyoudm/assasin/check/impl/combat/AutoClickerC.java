/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.combat;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.AttackTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.handler.async.AsyncProcessor;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.FFT;
import dev.tyoudm.assasin.util.RingBuffer;
import org.bukkit.entity.Player;

/**
 * AutoClickerC — FFT radix-2 frequency analysis (async).
 *
 * <p>Detects autoclickers with periodic click patterns using a radix-2
 * FFT on the last 32 inter-click intervals. A dominant frequency peak
 * with low kurtosis indicates a periodic (machine-generated) pattern.
 *
 * <h2>Lazy computation</h2>
 * FFT is only computed when preliminary Welford σ is suspicious
 * (σ &lt; {@link #SIGMA_TRIGGER}). The computation is offloaded to
 * {@link AsyncProcessor} to keep the main thread overhead near zero.
 *
 * <h2>Legit clicking</h2>
 * Butterfly / jitter / drag click produce bimodal distributions with
 * high kurtosis — they will NOT be flagged by this check.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "AutoClickerC",
    type              = CheckType.AUTO_CLICKER_C,
    category          = CheckCategory.COMBAT,
    description       = "Detects periodic autoclicker patterns via FFT (async).",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class AutoClickerC extends Check {

    /** FFT window size (must be power of 2). */
    private static final int    FFT_SIZE       = 32;
    /** Preliminary σ threshold to trigger FFT (ms). */
    private static final double SIGMA_TRIGGER  = 5.0;
    /** Kurtosis below this = periodic pattern (suspicious). */
    private static final double MIN_KURTOSIS   = 1.0;
    /** Dominant bin magnitude ratio to mean — high = single peak. */
    private static final double PEAK_RATIO     = 3.0;
    /** Ticks between FFT computations. */
    private static final int    COMPUTE_INTERVAL = 40;

    private final AsyncProcessor asyncProcessor;
    private int ticksSinceCompute;

    // Shared result from async FFT (written by async, read by main)
    private volatile boolean asyncFlagged;
    private volatile String  asyncDetails;

    public AutoClickerC(final MitigationEngine engine, final AsyncProcessor asyncProcessor) {
        super(engine);
        this.asyncProcessor = asyncProcessor;
    }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.ATTRIBUTE_SWAP,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        // Check async result from previous computation
        if (asyncFlagged) {
            asyncFlagged = false;
            flag(player, data, 1.5, asyncDetails, tick);
            return;
        }

        ticksSinceCompute++;
        if (ticksSinceCompute < COMPUTE_INTERVAL) return;
        ticksSinceCompute = 0;

        final AttackTracker at = data.getAttackTracker();
        if (at == null) return;

        final var stats = at.getIntervalStats();
        if (stats.count() < FFT_SIZE) return;

        // Only trigger FFT if preliminary σ is suspicious
        if (stats.stdDev() >= SIGMA_TRIGGER) return;

        // Snapshot intervals for async processing
        final RingBuffer.OfLong intervals = at.getIntervals();
        final int n = Math.min(FFT_SIZE, intervals.size());
        final double[] re = new double[FFT_SIZE];
        final double[] im = new double[FFT_SIZE];
        for (int i = 0; i < n; i++) re[i] = intervals.get(intervals.size() - n + i);

        final String playerName = player.getName();

        asyncProcessor.submit(() -> {
            try {
                FFT.fft(re, im);
                final double[] mags     = FFT.magnitudes(re, im);
                final double   kurtosis = FFT.kurtosis(mags);
                final int      domBin   = FFT.dominantBin(mags);

                // Compute mean magnitude (excluding DC)
                double meanMag = 0.0;
                for (int k = 1; k < mags.length; k++) meanMag += mags[k];
                meanMag /= (mags.length - 1);

                final double peakRatio = meanMag > 0 ? mags[domBin] / meanMag : 0;

                if (kurtosis < MIN_KURTOSIS && peakRatio > PEAK_RATIO) {
                    asyncFlagged = true;
                    asyncDetails = String.format(
                        "fft kurtosis=%.3f min=%.3f peakRatio=%.2f domBin=%d",
                        kurtosis, MIN_KURTOSIS, peakRatio, domBin);
                }
            } catch (final Exception ignored) { /* FFT errors are non-fatal */ }
        });
    }
}
