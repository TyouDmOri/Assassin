/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.macro;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.AttackTracker;
import dev.tyoudm.assasin.data.tracker.MacroStateTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.handler.async.AsyncProcessor;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.FFT;
import dev.tyoudm.assasin.util.RingBuffer;
import org.bukkit.entity.Player;

/**
 * MacroClickerA — FFT kurtosis + bimodal analysis for macro clicker detection.
 *
 * <p>Detects macro clickers by analyzing the frequency spectrum of click
 * intervals. Human clicking produces a bimodal distribution with high kurtosis;
 * macro clickers produce a unimodal distribution with a single dominant peak.
 *
 * <h2>Lazy async computation</h2>
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
    name              = "MacroClickerA",
    type              = CheckType.MACRO_CLICKER_A,
    category          = CheckCategory.MACRO,
    description       = "Detects macro clicker via FFT kurtosis + bimodal analysis.",
    maxVl             = 20.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "macro"
)
public final class MacroClickerA extends Check {

    private static final int    FFT_SIZE         = 32;
    private static final double SIGMA_TRIGGER    = 5.0;
    /** Kurtosis below this = unimodal (macro-like). */
    private static final double MIN_KURTOSIS     = 0.5;
    /** Peak ratio above this = single dominant frequency. */
    private static final double PEAK_RATIO       = 4.0;
    private static final int    COMPUTE_INTERVAL = 40;
    private static final int    MIN_SAMPLES      = 30;

    private final AsyncProcessor asyncProcessor;
    private int ticksSinceCompute;

    private volatile boolean asyncFlagged;
    private volatile String  asyncDetails;

    public MacroClickerA(final MitigationEngine engine, final AsyncProcessor asyncProcessor) {
        super(engine);
        this.asyncProcessor = asyncProcessor;
    }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.HIGH_PING, ExemptType.LAG_SPIKE,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        // Consume async result
        if (asyncFlagged) {
            asyncFlagged = false;
            final MacroStateTracker mst = data.getMacroStateTracker();
            if (mst != null) mst.addEvidence(25, tick);
            flag(player, data, 1.5, asyncDetails, tick);
            return;
        }

        ticksSinceCompute++;
        if (ticksSinceCompute < COMPUTE_INTERVAL) return;
        ticksSinceCompute = 0;

        final AttackTracker at = data.getAttackTracker();
        if (at == null || at.getIntervalStats().count() < MIN_SAMPLES) return;

        // Only trigger FFT if preliminary σ is suspicious
        if (at.getIntervalStats().stdDev() >= SIGMA_TRIGGER) return;

        // Snapshot intervals
        final RingBuffer.OfLong intervals = at.getIntervals();
        final int n = Math.min(FFT_SIZE, intervals.size());
        final double[] re = new double[FFT_SIZE];
        final double[] im = new double[FFT_SIZE];
        for (int i = 0; i < n; i++) re[i] = intervals.get(intervals.size() - n + i);

        asyncProcessor.submit(() -> {
            try {
                FFT.fft(re, im);
                final double[] mags     = FFT.magnitudes(re, im);
                final double   kurtosis = FFT.kurtosis(mags);
                final int      domBin   = FFT.dominantBin(mags);

                double meanMag = 0.0;
                for (int k = 1; k < mags.length; k++) meanMag += mags[k];
                meanMag /= (mags.length - 1);

                final double peakRatio = meanMag > 0 ? mags[domBin] / meanMag : 0;

                if (kurtosis < MIN_KURTOSIS && peakRatio > PEAK_RATIO) {
                    asyncFlagged = true;
                    asyncDetails = String.format(
                        "macro clicker fft: kurtosis=%.3f min=%.3f peakRatio=%.2f domBin=%d",
                        kurtosis, MIN_KURTOSIS, peakRatio, domBin);
                }
            } catch (final Exception ignored) {}
        });
    }
}
