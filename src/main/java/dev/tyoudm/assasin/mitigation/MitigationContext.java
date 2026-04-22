/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable context passed to every {@link MitigationStrategy} during execution.
 *
 * <p>Carries all information a strategy needs to act: the offending player,
 * their data snapshot, the check name that triggered the mitigation, the
 * current violation level, and optionally the raw packet event (for
 * packet-cancel strategies).
 *
 * @param player       the offending player
 * @param data         the player's data container
 * @param checkName    the name of the check that triggered this mitigation
 *                     (e.g., {@code "SpeedA"})
 * @param violationLevel the current VL at the time of triggering
 * @param details      optional human-readable details for alerts/logs
 * @param packetEvent  the triggering packet event, or {@code null} if not
 *                     triggered by a packet (e.g., event-based checks)
 * @param currentTick  the server tick at which the mitigation was triggered
 *
 * @author TyouDm
 * @version 1.0.0
 */
public record MitigationContext(
    Player                        player,
    PlayerData                    data,
    String                        checkName,
    double                        violationLevel,
    String                        details,
    CheckInfo.Severity            severity,
    @Nullable PacketReceiveEvent  packetEvent,
    long                          currentTick
) {

    // ─── Convenience ──────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if a packet event is available (packet-cancel
     * strategies can only act when this is non-null).
     *
     * @return {@code true} if {@link #packetEvent()} is non-null
     */
    public boolean hasPacketEvent() {
        return packetEvent != null;
    }

    /**
     * Cancels the triggering packet if a packet event is available.
     * No-op if {@link #packetEvent()} is {@code null}.
     */
    public void cancelPacket() {
        if (packetEvent != null) {
            packetEvent.setCancelled(true);
        }
    }

    // ─── Builder ──────────────────────────────────────────────────────────────

    /**
     * Creates a context without a packet event (for event-based checks).
     *
     * @param player         the offending player
     * @param data           the player's data
     * @param checkName      the triggering check name
     * @param violationLevel current VL
     * @param details        optional details string
     * @param currentTick    current server tick
     * @return a new {@link MitigationContext}
     */
    public static MitigationContext of(final Player player, final PlayerData data,
                                       final String checkName, final double violationLevel,
                                       final String details, final long currentTick) {
        return new MitigationContext(player, data, checkName, violationLevel,
                                     details, CheckInfo.Severity.MEDIUM, null, currentTick);
    }

    public static MitigationContext of(final Player player, final PlayerData data,
                                       final String checkName, final double violationLevel,
                                       final String details, final CheckInfo.Severity severity,
                                       final long currentTick) {
        return new MitigationContext(player, data, checkName, violationLevel,
                                     details, severity, null, currentTick);
    }

    public static MitigationContext ofPacket(final Player player, final PlayerData data,
                                             final String checkName, final double violationLevel,
                                             final String details,
                                             final PacketReceiveEvent packetEvent,
                                             final long currentTick) {
        return new MitigationContext(player, data, checkName, violationLevel,
                                     details, CheckInfo.Severity.MEDIUM, packetEvent, currentTick);
    }

    public static MitigationContext ofPacket(final Player player, final PlayerData data,
                                             final String checkName, final double violationLevel,
                                             final String details,
                                             final CheckInfo.Severity severity,
                                             final PacketReceiveEvent packetEvent,
                                             final long currentTick) {
        return new MitigationContext(player, data, checkName, violationLevel,
                                     details, severity, packetEvent, currentTick);
    }
}
