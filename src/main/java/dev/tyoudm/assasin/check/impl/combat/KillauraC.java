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
import dev.tyoudm.assasin.data.prediction.CollisionEngine;
import dev.tyoudm.assasin.data.tracker.CombatTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * KillauraC — Wall attack detection using Amanatides-Woo DDA voxel traversal.
 *
 * <p>Detects killaura attacking through solid walls by raycasting from the
 * attacker's eye position to the target. If the ray hits a solid block before
 * reaching the target, the attack is through a wall.
 *
 * <h2>Algorithm</h2>
 * Uses {@link CollisionEngine#raycast} (Amanatides-Woo DDA) — at most ~12
 * voxel iterations for a 6-block reach. This is O(n) with n ≤ 18, not O(1),
 * so it's only invoked when an attack is detected (not every tick).
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "KillauraC",
    type              = CheckType.KILLAURA_C,
    category          = CheckCategory.COMBAT,
    description       = "Detects wall attacks using DDA voxel traversal.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class KillauraC extends Check {

    /** Maximum reach distance to raycast (blocks). */
    private static final double MAX_REACH = 6.5;

    public KillauraC(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return;

        // Raycast from attacker eye to target
        final var result = CollisionEngine.raycastFromLocation(
            player.getEyeLocation(), MAX_REACH);

        if (result.hit() && result.distance() < ct.getAttackDistance()) {
            flag(player, data, 2.0,
                String.format("wall attack: ray hit block at dist=%.2f targetDist=%.2f",
                    result.distance(), ct.getAttackDistance()),
                tick);
        }
    }
}
