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
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * InventoryB — Inventory click without prior OPEN_WINDOW packet.
 *
 * <p>Detects when a player sends inventory click packets without having
 * received an OPEN_WINDOW packet first. This is a common exploit used
 * by chest-stealer and auto-armor hacks.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "InventoryB",
    type              = CheckType.INVENTORY_B,
    category          = CheckCategory.PLAYER,
    description       = "Detects inventory click without prior OPEN_WINDOW.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class InventoryB extends Check {

    public InventoryB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final InventoryTracker it = data.getInventoryTracker();
        if (it == null) return;

        // Window ID 0 = player's own inventory — always valid, no OPEN_WINDOW sent
        // Window ID -1 = no window tracked yet (just joined) — skip
        final int windowId = it.getOpenWindowId();
        if (windowId == 0 || windowId == -1) return;

        // If clicks are happening but no external window is open, it's suspicious
        if (!it.isWindowOpen() && it.getClicksThisWindow() > 0) {
            flag(player, data, 2.0,
                String.format("click without open window: clicks=%d", it.getClicksThisWindow()),
                tick);
        }
    }
}
