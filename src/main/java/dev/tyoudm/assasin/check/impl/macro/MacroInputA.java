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
import org.bukkit.entity.Player;

/**
 * MacroInputA — 4+ actions in 1 tick detection.
 *
 * <p>Detects macros that perform 4 or more distinct actions in a single
 * server tick (50ms). Humans cannot physically perform this many actions
 * simultaneously. Flags when this occurs more than {@link #MAX_OCCURRENCES_PER_MINUTE}
 * times per minute.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "MacroInputA",
    type              = CheckType.MACRO_INPUT_A,
    category          = CheckCategory.MACRO,
    description       = "Detects 4+ actions in 1 tick (impossible for humans).",
    maxVl             = 20.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "macro"
)
public final class MacroInputA extends Check {

    /** Minimum actions in a single tick to be suspicious. */
    private static final int MIN_ACTIONS_PER_TICK = 4;

    /** Maximum occurrences per minute before flagging. */
    private static final int MAX_OCCURRENCES_PER_MINUTE = 3;

    /** Window size in ticks (1 minute = 1200 ticks). */
    private static final int WINDOW_TICKS = 1200;

    private int  occurrencesInWindow;
    private long windowStartTick;

    public MacroInputA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.HIGH_PING, ExemptType.LAG_SPIKE,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final ActionTracker at = data.getActionTracker();
        if (at == null) return;

        // Check if this tick had too many actions
        if (at.getActionsThisTick() >= MIN_ACTIONS_PER_TICK
                && at.getLastActionTick() == tick) {
            occurrencesInWindow++;
        }

        // Reset window
        if (windowStartTick == 0) windowStartTick = tick;
        if (tick - windowStartTick >= WINDOW_TICKS) {
            if (occurrencesInWindow > MAX_OCCURRENCES_PER_MINUTE) {
                final MacroStateTracker mst = data.getMacroStateTracker();
                if (mst != null) mst.addEvidence(25, tick);

                flag(player, data, 1.5,
                    String.format("macro input: %d occurrences/min (max %d), actions=%d",
                        occurrencesInWindow, MAX_OCCURRENCES_PER_MINUTE,
                        at.getActionsThisTick()),
                    tick);
            }
            occurrencesInWindow = 0;
            windowStartTick     = tick;
        }
    }
}
