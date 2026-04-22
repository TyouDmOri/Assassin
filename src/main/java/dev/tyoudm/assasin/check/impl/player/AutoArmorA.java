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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * AutoArmorA — Automatic armor equip detection.
 *
 * <p>Detects auto-armor hacks that equip armor pieces faster than humanly
 * possible. Legitimate players need at least 2 ticks per armor piece.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "AutoArmorA",
    type              = CheckType.AUTO_ARMOR_A,
    category          = CheckCategory.PLAYER,
    description       = "Detects automatic armor equip.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class AutoArmorA extends Check {

    /** Minimum ticks between armor equip actions. */
    private static final int MIN_EQUIP_INTERVAL_TICKS = 2;

    private long lastEquipTick;
    private int  armorPiecesEquipped;

    public AutoArmorA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final InventoryTracker it = data.getInventoryTracker();
        if (it == null || !it.isWindowOpen()) return;

        // Check if armor was equipped this tick
        final PlayerInventory inv = player.getInventory();
        final int armorCount = countArmorPieces(inv);

        if (armorCount > armorPiecesEquipped) {
            final long interval = tick - lastEquipTick;
            if (lastEquipTick > 0 && interval < MIN_EQUIP_INTERVAL_TICKS) {
                flag(player, data, 1.5,
                    String.format("auto-armor: equip interval=%dt min=%dt pieces=%d",
                        interval, MIN_EQUIP_INTERVAL_TICKS, armorCount),
                    tick);
            }
            lastEquipTick = tick;
        }

        armorPiecesEquipped = armorCount;
    }

    private static int countArmorPieces(final PlayerInventory inv) {
        int count = 0;
        if (inv.getHelmet()     != null) count++;
        if (inv.getChestplate() != null) count++;
        if (inv.getLeggings()   != null) count++;
        if (inv.getBoots()      != null) count++;
        return count;
    }
}
