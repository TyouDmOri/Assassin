/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.storage.model;

import java.util.UUID;

/**
 * Immutable record representing a staff member's alert preferences stored in
 * {@code assasin_alert_preferences}.
 *
 * <h2>Channel bitmask</h2>
 * <pre>
 *   bit 0 (0x01) — CHAT
 *   bit 1 (0x02) — ACTION_BAR
 *   bit 2 (0x04) — TITLE
 *   bit 3 (0x08) — SOUND
 *   bit 4 (0x10) — DISCORD
 * </pre>
 *
 * @param staffUuid      UUID of the staff member
 * @param checkName      check name, or {@code "*"} for all checks
 * @param enabled        whether alerts are enabled for this check
 * @param channelBitmask bitmask of enabled alert channels
 *
 * @author TyouDm
 * @version 1.0.0
 */
public record AlertPreference(
    UUID    staffUuid,
    String  checkName,
    boolean enabled,
    int     channelBitmask
) {
    // ─── Channel constants ────────────────────────────────────────────────────

    public static final int CHANNEL_CHAT       = 0x01;
    public static final int CHANNEL_ACTION_BAR = 0x02;
    public static final int CHANNEL_TITLE      = 0x04;
    public static final int CHANNEL_SOUND      = 0x08;
    public static final int CHANNEL_DISCORD    = 0x10;

    /** Default preference: all channels enabled. */
    public static final int DEFAULT_CHANNELS =
        CHANNEL_CHAT | CHANNEL_ACTION_BAR | CHANNEL_SOUND;

    // ─── Factory ──────────────────────────────────────────────────────────────

    /** Creates a default preference (all channels, all checks enabled). */
    public static AlertPreference defaultFor(final UUID staffUuid) {
        return new AlertPreference(staffUuid, "*", true, DEFAULT_CHANNELS);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Returns {@code true} if the given channel is enabled. */
    public boolean hasChannel(final int channel) {
        return (channelBitmask & channel) != 0;
    }
}
