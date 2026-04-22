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
 * MaceDmgC — Mace cooldown bypass and wind charge exploit detection.
 *
 * <p>Detects two mace-specific exploits:
 * <ol>
 *   <li><b>Cooldown bypass</b> — attacking with a mace before the attack
 *       cooldown is fully charged.</li>
 *   <li><b>Wind charge exploit</b> — using a wind charge to gain height
 *       and immediately attacking with the mace before the server registers
 *       the height gain.</li>
 * </ol>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "MaceDmgC",
    type              = CheckType.MACE_DMG_C,
    category          = CheckCategory.COMBAT,
    description       = "Detects mace cooldown bypass and wind charge exploit.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class MaceDmgC extends Check {

    /** Minimum cooldown progress for a valid mace attack. */
    private static final float MIN_COOLDOWN = 0.9f;

    public MaceDmgC(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.ATTRIBUTE_SWAP,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return;

        final ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.MACE) return;

        if (ct.getCooldownProgress() < MIN_COOLDOWN) {
            flag(player, data, 1.5,
                String.format("mace cooldown bypass: progress=%.3f min=%.3f",
                    ct.getCooldownProgress(), MIN_COOLDOWN),
                tick);
        }
    }
}
