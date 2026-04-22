/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.alert;

import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.entity.Player;

/**
 * Immutable context for a single alert event.
 *
 * <p>Carries all information needed by {@link AlertFormatter} and
 * {@link DiscordWebhook} to render the alert in any channel.
 *
 * @param playerName     display name of the flagged player
 * @param playerUuid     UUID string of the flagged player
 * @param checkName      name of the check that flagged
 * @param violationLevel current VL at flag time
 * @param severity       check severity
 * @param details        human-readable flag details
 * @param pingMs         player ping at flag time
 * @param tps            server TPS at flag time
 * @param world          world name
 * @param x              player X at flag time
 * @param y              player Y at flag time
 * @param z              player Z at flag time
 * @param timestampMs    system time in ms
 *
 * @author TyouDm
 * @version 1.0.0
 */
public record AlertContext(
    String             playerName,
    String             playerUuid,
    String             checkName,
    double             violationLevel,
    CheckInfo.Severity severity,
    String             details,
    int                pingMs,
    double             tps,
    String             world,
    double             x,
    double             y,
    double             z,
    long               timestampMs
) {
    /**
     * Creates an {@link AlertContext} from a live player and their data.
     *
     * @param player    the flagged player
     * @param data      the player's data
     * @param checkName the check name
     * @param vl        current violation level
     * @param severity  check severity
     * @param details   flag details
     * @param tps       current server TPS
     * @return a new {@link AlertContext}
     */
    public static AlertContext of(final Player player, final PlayerData data,
                                  final String checkName, final double vl,
                                  final CheckInfo.Severity severity,
                                  final String details, final double tps) {
        return new AlertContext(
            player.getName(),
            player.getUniqueId().toString(),
            checkName,
            vl,
            severity,
            details,
            data.getPing(),
            tps,
            player.getWorld().getName(),
            data.getX(),
            data.getY(),
            data.getZ(),
            System.currentTimeMillis()
        );
    }
}
