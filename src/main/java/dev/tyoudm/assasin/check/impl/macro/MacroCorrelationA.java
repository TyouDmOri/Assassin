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
import dev.tyoudm.assasin.data.tracker.ActionTracker;
import dev.tyoudm.assasin.data.tracker.MacroStateTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.RingBuffer;
import org.bukkit.entity.Player;

/**
 * MacroCorrelationA — Pearson r² event→action correlation detection.
 *
 * <p>Detects macros that react to game events (damage received, totem pop,
 * shield break, arrow hit) with a suspiciously consistent delay. Computes
 * the Pearson correlation coefficient between event timestamps and subsequent
 * action timestamps.
 *
 * <h2>Algorithm</h2>
 * Maintains two parallel arrays:
 * <ul>
 *   <li>{@code eventMs[]} — timestamps of trigger events (damage, etc.)</li>
 *   <li>{@code actionMs[]} — timestamps of the first action after each event</li>
 * </ul>
 * Computes r² over the last {@link #WINDOW_SIZE} pairs. If r² &gt;
 * {@link #MIN_R_SQUARED}, the reaction delay is suspiciously consistent.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "MacroCorrelationA",
    type              = CheckType.MACRO_CORRELATION_A,
    category          = CheckCategory.MACRO,
    description       = "Detects macro via Pearson r²>0.95 event→action correlation.",
    maxVl             = 20.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "macro"
)
public final class MacroCorrelationA extends Check {

    /** Minimum r² to flag. */
    private static final double MIN_R_SQUARED = 0.95;

    /** Window size for correlation computation. */
    private static final int WINDOW_SIZE = 10;

    /** Minimum pairs before computing correlation. */
    private static final int MIN_PAIRS = 8;

    // Parallel arrays for event and action timestamps
    private final double[] eventDelays  = new double[WINDOW_SIZE];
    private final double[] actionDelays = new double[WINDOW_SIZE];
    private int pairCount;

    // Last event timestamp
    private long lastEventMs;
    private long lastEventActionMs;

    public MacroCorrelationA(final MitigationEngine engine) { super(engine); }

    /**
     * Records a trigger event (damage received, totem pop, etc.).
     * Called externally by event handlers.
     *
     * @param nowMs current system time in ms
     */
    public void recordEvent(final long nowMs) {
        lastEventMs = nowMs;
        lastEventActionMs = 0; // reset — waiting for next action
    }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.HIGH_PING, ExemptType.LAG_SPIKE,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        if (lastEventMs == 0) return;

        final ActionTracker at = data.getActionTracker();
        if (at == null || at.getActions().isEmpty()) return;

        final long actionMs = ActionTracker.decodeMs(at.getActions().newest());

        // Record the first action after the event
        if (lastEventActionMs == 0 && actionMs > lastEventMs) {
            lastEventActionMs = actionMs;
            final long delay = actionMs - lastEventMs;

            if (delay > 0 && delay < 5000) {
                final int idx = pairCount % WINDOW_SIZE;
                eventDelays[idx]  = lastEventMs % 10000; // relative timestamp
                actionDelays[idx] = delay;
                pairCount++;
            }

            lastEventMs = 0;
        }

        if (pairCount < MIN_PAIRS) return;

        // Compute Pearson r² over the window
        final int n = Math.min(pairCount, WINDOW_SIZE);
        final double r2 = pearsonR2(actionDelays, n);

        if (r2 > MIN_R_SQUARED) {
            final MacroStateTracker mst = data.getMacroStateTracker();
            if (mst != null) mst.addEvidence(30, tick);

            flag(player, data, 2.0,
                String.format("macro correlation: r²=%.3f min=%.3f n=%d",
                    r2, MIN_R_SQUARED, n),
                tick);
            pairCount = 0; // reset after flag
        }
    }

    // ─── Pearson r² ───────────────────────────────────────────────────────────

    /**
     * Computes the Pearson r² of the action delay array against a linear index.
     * High r² means the delays are nearly constant (macro-like).
     *
     * @param delays the delay values
     * @param n      number of valid entries
     * @return r² in [0, 1]
     */
    private static double pearsonR2(final double[] delays, final int n) {
        if (n < 2) return 0.0;

        // Mean
        double meanX = 0.0, meanY = 0.0;
        for (int i = 0; i < n; i++) {
            meanX += i;
            meanY += delays[i];
        }
        meanX /= n;
        meanY /= n;

        // Covariance and variances
        double cov = 0.0, varX = 0.0, varY = 0.0;
        for (int i = 0; i < n; i++) {
            final double dx = i - meanX;
            final double dy = delays[i] - meanY;
            cov  += dx * dy;
            varX += dx * dx;
            varY += dy * dy;
        }

        if (varX < 1e-10 || varY < 1e-10) return 1.0; // constant = perfect correlation
        final double r = cov / Math.sqrt(varX * varY);
        return r * r;
    }
}
