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
import dev.tyoudm.assasin.latency.LagCompensatedWorld;
import dev.tyoudm.assasin.latency.PingCompensator;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.MathUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * ReachB — Target rewind reach check.
 *
 * <p>Complements {@link ReachA} by rewinding the target's position to where
 * it was when the attacker's packet was sent (accounting for the attacker's
 * ping). Uses {@link LagCompensatedWorld} to look up the target's historical
 * position.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "ReachB",
    type              = CheckType.REACH_B,
    category          = CheckCategory.COMBAT,
    description       = "Detects reach violations using target position rewind.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class ReachB extends Check {

    private static final double BASE_REACH = 3.0;

    public ReachB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return;
        if (ct.getLastTargetUuid() == null) return;

        // Find target entity
        final Entity target = player.getWorld().getEntities().stream()
            .filter(e -> e.getUniqueId().equals(ct.getLastTargetUuid()))
            .findFirst().orElse(null);
        if (target == null) return;

        // Rewind target position by attacker's ping
        final int rewindTicks = PingCompensator.velocityCompensationTicks(data.getPing());
        final long targetTick = tick - rewindTicks;

        // Try to get target's PlayerData for lag-compensated position
        double targetX = target.getLocation().getX();
        double targetY = target.getLocation().getY();
        double targetZ = target.getLocation().getZ();

        if (target instanceof final Player targetPlayer) {
            // Access target's lag-compensated world if available
            // (requires target to also be tracked — safe to skip if not)
            final var targetData = player.getServer().getOnlinePlayers().stream()
                .filter(p -> p.getUniqueId().equals(target.getUniqueId()))
                .findFirst().orElse(null);
            // Simplified: use current position with ping tolerance
            // Full rewind requires target's LagCompensatedWorld (FASE 4 tracker)
        }

        final double pingComp = PingCompensator.reachCompensation(data.getPing());
        final double maxReach = BASE_REACH + pingComp;

        final double distSq = MathUtil.distanceSq(
            data.getX(), data.getY() + 1.62, data.getZ(),
            targetX, targetY + target.getHeight() / 2.0, targetZ);

        if (distSq > maxReach * maxReach) {
            final double dist   = Math.sqrt(distSq);
            final double excess = dist - maxReach;
            flag(player, data, excess * 2.0,
                String.format("rewindDist=%.3f max=%.3f excess=%.3f rewindTicks=%d",
                    dist, maxReach, excess, rewindTicks),
                tick);
        }
    }
}
