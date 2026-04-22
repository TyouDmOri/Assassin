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
 * BadPacketsA — NaN / Infinity position values.
 *
 * <p>Detects when the player sends NaN or Infinity as position coordinates.
 * These values can crash the server or cause undefined behaviour.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "BadPacketsA",
    type              = CheckType.BAD_PACKETS_A,
    category          = CheckCategory.PLAYER,
    description       = "Detects NaN/Infinity position values.",
    maxVl             = 3.0,
    severity          = CheckInfo.Severity.CRITICAL,
    mitigationProfile = "badpackets"
)
public final class BadPacketsA extends Check {

    public BadPacketsA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final double x = data.getX();
        final double y = data.getY();
        final double z = data.getZ();

        if (!MathUtil.isFinite(x) || !MathUtil.isFinite(y) || !MathUtil.isFinite(z)) {
            flag(player, data, 3.0,
                String.format("NaN/Inf position: x=%s y=%s z=%s", x, y, z),
                tick);
        }
    }
}
