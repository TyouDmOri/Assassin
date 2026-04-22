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
import dev.tyoudm.assasin.latency.PingCompensator;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.RingBuffer;
import org.bukkit.entity.Player;

/**
 * MacroTimingA — Sub-human reaction time detection.
 *
 * <p>Detects macros that react to game events (damage, death, etc.) faster
 * than the human neurological minimum reaction time of ~150ms.
 *
 * <h2>Disabled for high-ping players</h2>
 * Network jitter can make legitimate actions appear faster than they are.
 * This check is completely disabled when ping &gt; {@link PingCompensator#MAX_COMPENSATED_PING_MS}.
 *
 * <h2>Algorithm</h2>
 * Measures the interval between consecutive actions in the {@link ActionTracker}.
 * If any interval is below {@link #MIN_REACTION_MS} for multiple consecutive
 * pairs, it's suspicious.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "MacroTimingA",
    type              = CheckType.MACRO_TIMING_A,
    category          = CheckCategory.MACRO,
    description       = "Detects sub-human reaction time (<150ms) between actions.",
    maxVl             = 20.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "macro"
)
public final class MacroTimingA extends Check {

    /** Minimum human neurological reaction time (ms). */
    private static final long MIN_REACTION_MS = 150L;

    /** Consecutive sub-human intervals required before flagging. */
    private static final int MIN_CONSECUTIVE = 5;

    /** Minimum actions in buffer before running. */
    private static final int MIN_ACTIONS = 10;

    private int subHumanCount;

    public MacroTimingA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        // Disabled for high-ping players — network jitter confuses timing
        if (isExemptAny(data, tick,
                ExemptType.HIGH_PING, ExemptType.LAG_SPIKE,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) {
            subHumanCount = 0;
            return;
        }

        final ActionTracker at = data.getActionTracker();
        if (at == null || at.getActions().size() < MIN_ACTIONS) return;

        final RingBuffer.OfLong actions = at.getActions();
        final int n = actions.size();

        // Check the last two actions
        if (n < 2) return;

        final long ms1 = ActionTracker.decodeMs(actions.get(n - 2));
        final long ms2 = ActionTracker.decodeMs(actions.get(n - 1));
        final long interval = ms2 - ms1;

        if (interval > 0 && interval < MIN_REACTION_MS) {
            subHumanCount++;
            if (subHumanCount >= MIN_CONSECUTIVE) {
                final MacroStateTracker mst = data.getMacroStateTracker();
                if (mst != null) mst.addEvidence(10, tick);

                flag(player, data, 0.5,
                    String.format("sub-human reaction: %dms min=%dms consecutive=%d",
                        interval, MIN_REACTION_MS, subHumanCount),
                    tick);
            }
        } else {
            subHumanCount = Math.max(0, subHumanCount - 1);
        }
    }
}
