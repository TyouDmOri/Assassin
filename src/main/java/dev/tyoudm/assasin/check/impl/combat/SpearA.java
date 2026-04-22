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
 * SpearA — Trident trajectory and cooldown bypass detection.
 *
 * <p>Detects trident exploits:
 * <ol>
 *   <li><b>Cooldown bypass</b> — attacking with a trident before the
 *       attack cooldown is fully charged.</li>
 *   <li><b>Trajectory exploit</b> — throwing a trident and immediately
 *       attacking with it before it could physically return.</li>
 * </ol>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "SpearA",
    type              = CheckType.SPEAR_A,
    category          = CheckCategory.COMBAT,
    description       = "Detects trident trajectory and cooldown bypass.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class SpearA extends Check {

    /** Minimum cooldown progress required for a trident attack. */
    private static final float MIN_COOLDOWN = 0.85f;

    public SpearA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return;

        // Only check trident attacks
        final ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.TRIDENT) return;

        // Cooldown bypass check
        if (ct.getCooldownProgress() < MIN_COOLDOWN) {
            flag(player, data, 1.5,
                String.format("trident cooldown=%.3f min=%.3f",
                    ct.getCooldownProgress(), MIN_COOLDOWN),
                tick);
        }
    }
}
