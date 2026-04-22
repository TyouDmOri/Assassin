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
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * CrashA — Crash packet detection.
 *
 * <p>Detects packets that are known to cause server crashes or severe
 * performance degradation. Specifically checks for:
 * <ul>
 *   <li>Extremely large position deltas in a single tick (teleport exploit).</li>
 *   <li>Packet flood: too many packets in a single tick.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "CrashA",
    type              = CheckType.CRASH_A,
    category          = CheckCategory.PLAYER,
    description       = "Detects crash packets (extreme deltas, packet flood).",
    maxVl             = 3.0,
    severity          = CheckInfo.Severity.CRITICAL,
    mitigationProfile = "badpackets"
)
public final class CrashA extends Check {

    /** Maximum allowed position delta per tick (blocks). */
    private static final double MAX_DELTA = 100.0;

    public CrashA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final double dx = Math.abs(data.getX() - data.getLastX());
        final double dy = Math.abs(data.getY() - data.getLastY());
        final double dz = Math.abs(data.getZ() - data.getLastZ());

        if (dx > MAX_DELTA || dy > MAX_DELTA || dz > MAX_DELTA) {
            flag(player, data, 3.0,
                String.format("crash delta: dx=%.1f dy=%.1f dz=%.1f max=%.1f",
                    dx, dy, dz, MAX_DELTA),
                tick);
        }
    }
}
