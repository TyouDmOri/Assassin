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
 * BadPacketsE — Invalid hotbar slot.
 *
 * <p>Detects when the player sends a held-item slot value outside the
 * valid range [0, 8]. This can be used to access invalid inventory slots.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "BadPacketsE",
    type              = CheckType.BAD_PACKETS_E,
    category          = CheckCategory.PLAYER,
    description       = "Detects invalid hotbar slot (outside 0–8).",
    maxVl             = 3.0,
    severity          = CheckInfo.Severity.CRITICAL,
    mitigationProfile = "badpackets"
)
public final class BadPacketsE extends Check {

    public BadPacketsE(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final InventoryTracker it = data.getInventoryTracker();
        if (it == null) return;

        final int slot = it.getHeldSlot();
        if (slot < 0 || slot > 8) {
            flag(player, data, 3.0,
                String.format("invalid hotbar slot: %d (valid: 0–8)", slot),
                tick);
        }
    }
}
