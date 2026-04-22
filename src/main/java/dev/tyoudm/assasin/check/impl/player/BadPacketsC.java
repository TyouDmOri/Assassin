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
 * BadPacketsC — Extreme Y position (pos.y &gt; 1e7).
 *
 * <p>Detects when the player sends an extremely high Y coordinate that
 * could cause server-side overflow or crash issues.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "BadPacketsC",
    type              = CheckType.BAD_PACKETS_C,
    category          = CheckCategory.PLAYER,
    description       = "Detects extreme Y position (pos.y > 1e7).",
    maxVl             = 3.0,
    severity          = CheckInfo.Severity.CRITICAL,
    mitigationProfile = "badpackets"
)
public final class BadPacketsC extends Check {

    private static final double MAX_Y_EXTREME = 1e7;

    public BadPacketsC(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (Math.abs(data.getY()) > MAX_Y_EXTREME) {
            flag(player, data, 3.0,
                String.format("extreme Y: y=%.1f max=%.0f", data.getY(), MAX_Y_EXTREME),
                tick);
        }
    }
}
