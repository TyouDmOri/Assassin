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
 * Immutable record representing a single violation event stored in
 * {@code assasin_violations}.
 *
 * @param id                 auto-generated row ID (0 for unsaved records)
 * @param playerUuid         UUID of the offending player
 * @param checkName          name of the check that flagged (e.g., "SpeedA")
 * @param violationLevel     VL at the time of the flag
 * @param timestampMs        system time in ms when the flag occurred
 * @param pingMs             player ping at flag time
 * @param tps                server TPS at flag time
 * @param world              world name
 * @param x                  player X at flag time
 * @param y                  player Y at flag time
 * @param z                  player Z at flag time
 * @param mitigationApplied  name of the mitigation strategy applied (may be empty)
 * @param dataJson           optional JSON blob with check-specific details
 *
 * @author TyouDm
 * @version 1.0.0
 */
public record ViolationRecord(
    long   id,
    UUID   playerUuid,
    String checkName,
    double violationLevel,
    long   timestampMs,
    int    pingMs,
    double tps,
    String world,
    double x,
    double y,
    double z,
    String mitigationApplied,
    String dataJson
) {
    /** Creates an unsaved record (id = 0). */
    public static ViolationRecord of(
            final UUID playerUuid, final String checkName,
            final double violationLevel, final long timestampMs,
            final int pingMs, final double tps,
            final String world, final double x, final double y, final double z,
            final String mitigationApplied, final String dataJson) {
        return new ViolationRecord(0, playerUuid, checkName, violationLevel,
            timestampMs, pingMs, tps, world, x, y, z, mitigationApplied, dataJson);
    }
}
