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
 * Immutable record representing accumulated macro detection evidence stored in
 * {@code assasin_macro_evidence}.
 *
 * @param id            auto-generated row ID
 * @param playerUuid    UUID of the suspected player
 * @param patternHash   Rabin-Karp hash of the detected sequence pattern
 * @param occurrences   number of times this pattern was observed
 * @param avgDeltaMs    average inter-action interval (ms)
 * @param stdDev        standard deviation of inter-action intervals (ms)
 * @param lastSeenMs    system time of the last observation (ms)
 * @param evidenceJson  optional JSON blob with raw evidence data
 *
 * @author TyouDm
 * @version 1.0.0
 */
public record MacroEvidence(
    long   id,
    UUID   playerUuid,
    long   patternHash,
    int    occurrences,
    double avgDeltaMs,
    double stdDev,
    long   lastSeenMs,
    String evidenceJson
) {
    /** Creates an unsaved evidence record. */
    public static MacroEvidence of(final UUID playerUuid, final long patternHash,
                                   final int occurrences, final double avgDeltaMs,
                                   final double stdDev, final long lastSeenMs,
                                   final String evidenceJson) {
        return new MacroEvidence(0, playerUuid, patternHash, occurrences,
            avgDeltaMs, stdDev, lastSeenMs, evidenceJson);
    }
}
