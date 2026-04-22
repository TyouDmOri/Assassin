/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.combat;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.CombatTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * MaceDmgB — Density smash without airtime detection.
 *
 * <p>The Mace's "density smash" mechanic requires the player to be airborne
 * for a minimum duration before the smash activates. Detects when the player
 * performs a density smash without the required airtime.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "MaceDmgB",
    type              = CheckType.MACE_DMG_B,
    category          = CheckCategory.COMBAT,
    description       = "Detects mace density smash without required airtime.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class MaceDmgB extends Check {

    /** Minimum air ticks required for a valid density smash. */
    private static final int MIN_AIR_TICKS = 5;

    public MaceDmgB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.ATTRIBUTE_SWAP,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return;

        final ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.MACE) return;

        final var mt = data.getMovementTracker();
        if (mt == null) return;

        // Density smash requires airtime; if player was on ground, no smash
        if (data.isOnGround() && mt.getAirTicks() < MIN_AIR_TICKS) {
            flag(player, data, 1.5,
                String.format("density smash without airtime: airTicks=%d min=%d",
                    mt.getAirTicks(), MIN_AIR_TICKS),
                tick);
        }
    }
}
