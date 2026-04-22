/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.player;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.ActionTracker;
import dev.tyoudm.assasin.data.tracker.InventoryTracker;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * AutoTotemD — Totem swap with simultaneous multitasking.
 *
 * <p>Detects auto-totem that performs the totem swap simultaneously with
 * other actions in the same tick (e.g., attacking + swapping totem in 1 tick).
 * Legitimate players cannot perform multiple complex actions in a single tick.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "AutoTotemD",
    type              = CheckType.AUTO_TOTEM_D,
    category          = CheckCategory.PLAYER,
    description       = "Detects auto-totem with simultaneous multitasking.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class AutoTotemD extends Check {

    /** Minimum actions in the same tick as a totem swap to be suspicious. */
    private static final int MIN_SIMULTANEOUS_ACTIONS = 3;

    public AutoTotemD(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final InventoryTracker it = data.getInventoryTracker();
        if (it == null) return;

        final long swapMs = it.getLastTotemSwapMs();
        if (swapMs == 0) return;

        final long nowMs = System.currentTimeMillis();
        if (nowMs - swapMs > 100) return; // not a recent swap

        final ActionTracker at = data.getActionTracker();
        if (at == null) return;

        // Check if many actions happened in the same tick as the swap
        if (at.getActionsThisTick() >= MIN_SIMULTANEOUS_ACTIONS) {
            flag(player, data, 1.5,
                String.format("auto-totem multitask: actions=%d in same tick",
                    at.getActionsThisTick()),
                tick);
        }
    }
}
