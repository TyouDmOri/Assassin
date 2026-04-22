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
import dev.tyoudm.assasin.data.tracker.InventoryTracker;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * AutoTotemC — Totem swap without OPEN_WINDOW packet.
 *
 * <p>Detects auto-totem that swaps the totem directly via packet
 * manipulation without opening the inventory first. Legitimate totem
 * swaps require the player to open their inventory.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "AutoTotemC",
    type              = CheckType.AUTO_TOTEM_C,
    category          = CheckCategory.PLAYER,
    description       = "Detects totem swap without prior OPEN_WINDOW packet.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class AutoTotemC extends Check {

    public AutoTotemC(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final InventoryTracker it = data.getInventoryTracker();
        if (it == null) return;

        final long swapMs = it.getLastTotemSwapMs();
        if (swapMs == 0) return;

        // If a totem swap happened but no window was open, it's suspicious
        if (!it.isWindowOpen() && it.getTotemSwapCount() > 0) {
            final long nowMs = System.currentTimeMillis();
            if (nowMs - swapMs < 200) { // recent swap
                flag(player, data, 2.0,
                    String.format("totem swap without open window: swapCount=%d",
                        it.getTotemSwapCount()),
                    tick);
            }
        }
    }
}
