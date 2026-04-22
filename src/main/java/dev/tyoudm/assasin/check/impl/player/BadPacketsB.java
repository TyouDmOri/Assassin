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
 * BadPacketsB — Position values out of valid range.
 *
 * <p>Detects when the player sends position values that are outside the
 * valid Minecraft world bounds. The world height is [-64, 320] in 1.18+
 * and horizontal bounds are ±30,000,000.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "BadPacketsB",
    type              = CheckType.BAD_PACKETS_B,
    category          = CheckCategory.PLAYER,
    description       = "Detects position values outside valid world bounds.",
    maxVl             = 3.0,
    severity          = CheckInfo.Severity.CRITICAL,
    mitigationProfile = "badpackets"
)
public final class BadPacketsB extends Check {

    private static final double MAX_HORIZONTAL = 30_000_000.0;
    private static final double MIN_Y          = -64.0;
    private static final double MAX_Y          = 320.0;

    public BadPacketsB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final double x = data.getX();
        final double y = data.getY();
        final double z = data.getZ();

        if (Math.abs(x) > MAX_HORIZONTAL || Math.abs(z) > MAX_HORIZONTAL
                || y < MIN_Y - 64 || y > MAX_Y + 64) {
            flag(player, data, 2.0,
                String.format("out-of-bounds position: x=%.1f y=%.1f z=%.1f", x, y, z),
                tick);
        }
    }
}
