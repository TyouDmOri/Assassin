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
import dev.tyoudm.assasin.util.WelfordStats;
import org.bukkit.entity.Player;

/**
 * MacroVarianceA — Input interval variance detection.
 *
 * <p>Detects macros by measuring the variance (σ) of inter-action intervals.
 * Human input has σ ≥ 15–40ms; macros have σ &lt; {@link #MIN_SIGMA_MS}.
 *
 * <h2>Hardware gaming tolerance</h2>
 * High-end gaming hardware (Razer, Logitech) can produce consistent input
 * with σ ≈ 2–5ms. Requires n ≥ {@link #MIN_SAMPLES} to avoid false positives
 * from short bursts.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "MacroVarianceA",
    type              = CheckType.MACRO_VARIANCE_A,
    category          = CheckCategory.MACRO,
    description       = "Detects macro via input interval variance (σ < 1.5ms, n≥20).",
    maxVl             = 20.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "macro"
)
public final class MacroVarianceA extends Check {

    /** σ below this threshold is suspicious (ms). */
    private static final double MIN_SIGMA_MS  = 1.5;

    /** Minimum samples before flagging. */
    private static final int    MIN_SAMPLES   = 20;

    /** Ticks between variance computations. */
    private static final int    COMPUTE_INTERVAL = 20;

    private final WelfordStats intervalStats = new WelfordStats();
    private long lastActionMs;
    private int  ticksSinceCompute;

    public MacroVarianceA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.HIGH_PING, ExemptType.LAG_SPIKE,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) {
            intervalStats.reset();
            return;
        }

        final ActionTracker at = data.getActionTracker();
        if (at == null) return;

        // Feed new intervals from the action tracker
        final RingBuffer.OfLong actions = at.getActions();
        if (actions.size() >= 2) {
            final long ms = ActionTracker.decodeMs(actions.newest());
            if (lastActionMs > 0 && ms > lastActionMs) {
                final long interval = ms - lastActionMs;
                if (interval > 0 && interval < 5000) {
                    intervalStats.add(interval);
                }
            }
            lastActionMs = ms;
        }

        ticksSinceCompute++;
        if (ticksSinceCompute < COMPUTE_INTERVAL) return;
        ticksSinceCompute = 0;

        if (intervalStats.count() < MIN_SAMPLES) return;

        final double sigma = intervalStats.stdDev();
        if (sigma < MIN_SIGMA_MS) {
            final MacroStateTracker mst = data.getMacroStateTracker();
            if (mst != null) mst.addEvidence(20, tick);

            flag(player, data, 1.0,
                String.format("macro variance: σ=%.3fms min=%.3fms n=%d",
                    sigma, MIN_SIGMA_MS, intervalStats.count()),
                tick);
        }
    }
}
