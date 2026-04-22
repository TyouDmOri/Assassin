/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.storage.model;

/**
 * Immutable record representing a mitigation action stored in
 * {@code assasin_mitigations}.
 *
 * @param id          auto-generated row ID
 * @param violationId FK → assasin_violations.id
 * @param strategy    name of the strategy executed (e.g., "SoftSetback")
 * @param result      result description (e.g., "ok", "noop", "failure: ...")
 * @param timestampMs system time in ms when the strategy executed
 *
 * @author TyouDm
 * @version 1.0.0
 */
public record MitigationLog(
    long   id,
    long   violationId,
    String strategy,
    String result,
    long   timestampMs
) {
    /** Creates an unsaved log entry. */
    public static MitigationLog of(final long violationId, final String strategy,
                                   final String result, final long timestampMs) {
        return new MitigationLog(0, violationId, strategy, result, timestampMs);
    }
}
