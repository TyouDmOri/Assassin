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
 * BadPacketsF — Impossible duplicate packets.
 *
 * <p>Detects when the player sends duplicate movement packets in the same
 * tick with identical position values. This is physically impossible in
 * vanilla and indicates packet manipulation.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "BadPacketsF",
    type              = CheckType.BAD_PACKETS_F,
    category          = CheckCategory.PLAYER,
    description       = "Detects impossible duplicate movement packets.",
    maxVl             = 3.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "badpackets"
)
public final class BadPacketsF extends Check {

    private double lastX, lastY, lastZ;
    private long   lastPacketTick;
    private int    duplicateCount;

    public BadPacketsF(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final double x = data.getX();
        final double y = data.getY();
        final double z = data.getZ();

        if (tick == lastPacketTick
                && x == lastX && y == lastY && z == lastZ) {
            duplicateCount++;
            if (duplicateCount >= 3) {
                flag(player, data, 1.5,
                    String.format("duplicate packets: count=%d at (%.2f,%.2f,%.2f)",
                        duplicateCount, x, y, z),
                    tick);
            }
        } else {
            duplicateCount = 0;
        }

        lastX          = x;
        lastY          = y;
        lastZ          = z;
        lastPacketTick = tick;
    }
}
