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
 * HitboxA — AABB expansion detection.
 *
 * <p>Detects hitbox expansion hacks by checking whether the attack ray
 * (from attacker eye to target) intersects the target's vanilla AABB.
 * If the ray misses the vanilla AABB but the attack was registered,
 * the client is using an expanded hitbox.
 *
 * <h2>AABB dimensions</h2>
 * Player: 0.6 × 1.8 (standing), 0.6 × 1.5 (sneaking).
 * Other entities: use their Bukkit bounding box.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "HitboxA",
    type              = CheckType.HITBOX_A,
    category          = CheckCategory.COMBAT,
    description       = "Detects AABB hitbox expansion.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class HitboxA extends Check {

    /** Expansion tolerance (blocks) added to vanilla AABB for ping/lag. */
    private static final double EXPANSION_TOLERANCE = 0.1;

    public HitboxA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return;
        if (ct.getLastTargetUuid() == null) return;

        final Entity target = player.getWorld().getEntities().stream()
            .filter(e -> e.getUniqueId().equals(ct.getLastTargetUuid()))
            .findFirst().orElse(null);
        if (target == null) return;

        // Target AABB center
        final var bb = target.getBoundingBox();
        final double halfW = (bb.getWidthX() / 2.0) + EXPANSION_TOLERANCE;
        final double halfH = (bb.getHeight()  / 2.0) + EXPANSION_TOLERANCE;

        // Attacker eye position
        final double eyeX = data.getX();
        final double eyeY = data.getY() + 1.62;
        final double eyeZ = data.getZ();

        // Distance from attacker eye to target AABB center
        final double cx = bb.getCenterX();
        final double cy = bb.getCenterY();
        final double cz = bb.getCenterZ();

        // Check if attacker eye is outside expanded AABB
        final double dx = Math.max(0, Math.abs(eyeX - cx) - halfW);
        final double dy = Math.max(0, Math.abs(eyeY - cy) - halfH);
        final double dz = Math.max(0, Math.abs(eyeZ - cz) - halfW);
        final double distToAABB = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // If the attacker is outside the AABB by more than reach, it's suspicious
        if (distToAABB > 3.0 + EXPANSION_TOLERANCE) {
            flag(player, data, 1.5,
                String.format("distToAABB=%.3f max=%.3f", distToAABB, 3.0 + EXPANSION_TOLERANCE),
                tick);
        }
    }
}
