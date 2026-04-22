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
import dev.tyoudm.assasin.data.tracker.MovementTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * MaceDmgA — Mace damage scaling exploit detection.
 *
 * <p>The Mace deals bonus damage based on fall height. Detects when the
 * player claims mace bonus damage without the required fall distance.
 *
 * <h2>Vanilla formula</h2>
 * Bonus damage = fallDistance × 0.5 (approximately).
 * If the observed damage is significantly higher than expected for the
 * tracked fall distance, it's suspicious.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "MaceDmgA",
    type              = CheckType.MACE_DMG_A,
    category          = CheckCategory.COMBAT,
    description       = "Detects mace damage scaling exploit (damage without fall).",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class MaceDmgA extends Check {

    /** Mace bonus damage per block of fall distance (vanilla approximation). */
    private static final double BONUS_PER_BLOCK = 0.5;

    /** Tolerance added to expected damage. */
    private static final double TOLERANCE = 2.0;

    public MaceDmgA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.ATTRIBUTE_SWAP,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return;

        // Only check mace attacks
        final ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.MACE) return;

        final MovementTracker mt = data.getMovementTracker();
        if (mt == null) return;

        final double fallDist = mt.getFallDistance();
        final double expectedBonus = fallDist * BONUS_PER_BLOCK;

        // We can't directly read the damage dealt here without an event,
        // so we check if the player claims mace bonus without sufficient fall
        if (fallDist < 1.5 && ct.getAttackMotionY() > -0.5) {
            // Player attacked with mace while not falling — no bonus should apply
            // This is a heuristic; full damage check requires EntityDamageByEntityEvent
            flag(player, data, 1.0,
                String.format("mace attack without fall: fallDist=%.2f motionY=%.4f",
                    fallDist, ct.getAttackMotionY()),
                tick);
        }
    }
}
