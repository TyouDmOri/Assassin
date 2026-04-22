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
import dev.tyoudm.assasin.util.MathUtil;
import org.bukkit.entity.Player;

/**
 * BadPacketsD — Invalid rotation values.
 *
 * <p>Detects when the player sends rotation values outside the valid range:
 * yaw must be in [-180, 180) and pitch in [-90, 90].
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "BadPacketsD",
    type              = CheckType.BAD_PACKETS_D,
    category          = CheckCategory.PLAYER,
    description       = "Detects invalid rotation values (yaw/pitch out of range).",
    maxVl             = 3.0,
    severity          = CheckInfo.Severity.CRITICAL,
    mitigationProfile = "badpackets"
)
public final class BadPacketsD extends Check {

    public BadPacketsD(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final float yaw   = data.getYaw();
        final float pitch = data.getPitch();

        if (!MathUtil.isFinite(yaw) || !MathUtil.isFinite(pitch)) {
            flag(player, data, 3.0,
                String.format("NaN/Inf rotation: yaw=%s pitch=%s", yaw, pitch),
                tick);
            return;
        }

        if (pitch < -90.1f || pitch > 90.1f) {
            flag(player, data, 2.0,
                String.format("invalid pitch: %.4f (valid: [-90, 90])", pitch),
                tick);
        }
    }
}
