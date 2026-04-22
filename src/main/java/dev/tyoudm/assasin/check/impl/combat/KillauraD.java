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
import dev.tyoudm.assasin.util.MathUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * KillauraD — Angle difference detection.
 *
 * <p>Detects killaura by computing the angle between the player's look
 * direction and the direction to the target at attack time. Legitimate
 * players face their target; killaura often attacks entities outside the
 * player's field of view.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "KillauraD",
    type              = CheckType.KILLAURA_D,
    category          = CheckCategory.COMBAT,
    description       = "Detects killaura via angle difference to target.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class KillauraD extends Check {

    /** Maximum allowed angle (degrees) between look direction and target direction. */
    private static final double MAX_ANGLE = 90.0;

    public KillauraD(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return;

        // Find the target entity
        final Entity target = player.getWorld().getEntities().stream()
            .filter(e -> e.getUniqueId().equals(ct.getLastTargetUuid()))
            .findFirst().orElse(null);
        if (target == null) return;

        // Direction from attacker to target
        final var attackerLoc = player.getEyeLocation();
        final var targetLoc   = target.getLocation().add(0, target.getHeight() / 2.0, 0);
        final double dx = targetLoc.getX() - attackerLoc.getX();
        final double dy = targetLoc.getY() - attackerLoc.getY();
        final double dz = targetLoc.getZ() - attackerLoc.getZ();
        final double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 0.01) return;

        // Attacker look direction
        final double yawRad   = Math.toRadians(ct.getAttackYaw());
        final double pitchRad = Math.toRadians(ct.getAttackPitch());
        final double cosP     = Math.cos(-pitchRad);
        final double lookX    = -Math.sin(yawRad) * cosP;
        final double lookY    =  Math.sin(-pitchRad);
        final double lookZ    =  Math.cos(yawRad)  * cosP;

        // Dot product → angle
        final double dot   = (dx / dist) * lookX + (dy / dist) * lookY + (dz / dist) * lookZ;
        final double angle = Math.toDegrees(Math.acos(MathUtil.clamp(dot, -1.0, 1.0)));

        if (angle > MAX_ANGLE) {
            flag(player, data, 1.5,
                String.format("angle=%.1f° max=%.1f°", angle, MAX_ANGLE),
                tick);
        }
    }
}
