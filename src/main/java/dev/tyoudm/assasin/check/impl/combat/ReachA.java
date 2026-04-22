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
import dev.tyoudm.assasin.latency.PingCompensator;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * ReachA — Ping-compensated reach distance check.
 *
 * <p>Flags when the distance between the attacker and target at attack time
 * exceeds the vanilla melee reach (3.0 blocks) plus ping compensation.
 *
 * <h2>Ping compensation</h2>
 * {@link PingCompensator#reachCompensation} adds up to 1.5 blocks of
 * tolerance for high-ping players to prevent false positives.
 *
 * <h2>Distance calculation</h2>
 * Uses squared distance (avoids sqrt) with the threshold² precomputed.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "ReachA",
    type              = CheckType.REACH_A,
    category          = CheckCategory.COMBAT,
    description       = "Detects reach violations (ping-compensated).",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class ReachA extends Check {

    /** Vanilla melee reach (blocks). */
    private static final double BASE_REACH = 3.0;

    public ReachA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return;

        final double pingComp  = PingCompensator.reachCompensation(data.getPing());
        final double maxReach  = BASE_REACH + pingComp;
        final double maxReachSq = maxReach * maxReach;
        final double distSq    = ct.getAttackDistance() * ct.getAttackDistance();

        if (distSq > maxReachSq) {
            final double dist   = ct.getAttackDistance();
            final double excess = dist - maxReach;
            flag(player, data, excess * 2.0,
                String.format("dist=%.3f max=%.3f excess=%.3f ping=%d",
                    dist, maxReach, excess, data.getPing()),
                tick);
        }
    }
}
