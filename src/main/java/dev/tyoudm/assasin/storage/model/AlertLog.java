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
 * Immutable record representing an alert sent to staff, stored in
 * {@code assasin_alerts}.
 *
 * @param id             auto-generated row ID
 * @param staffUuid      UUID of the staff member who received the alert
 *                       ({@code null} for broadcast alerts)
 * @param playerUuid     UUID of the flagged player
 * @param checkName      check that triggered the alert
 * @param violationLevel VL at alert time
 * @param timestampMs    system time in ms
 *
 * @author TyouDm
 * @version 1.0.0
 */
public record AlertLog(
    long   id,
    UUID   staffUuid,
    UUID   playerUuid,
    String checkName,
    double violationLevel,
    long   timestampMs
) {
    /** Creates an unsaved broadcast alert (no specific staff recipient). */
    public static AlertLog broadcast(final UUID playerUuid, final String checkName,
                                     final double vl, final long timestampMs) {
        return new AlertLog(0, null, playerUuid, checkName, vl, timestampMs);
    }
}
