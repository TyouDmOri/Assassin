/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation;

/**
 * Immutable result returned by a {@link MitigationStrategy} after execution.
 *
 * <p>Carries the outcome of the strategy and any metadata needed by the
 * {@link MitigationEngine} to chain subsequent strategies or update state.
 *
 * @param success       whether the strategy executed successfully
 * @param cancelPacket  whether the triggering packet should be cancelled
 * @param message       optional human-readable description (for logs/debug)
 *
 * @author TyouDm
 * @version 1.0.0
 */
public record MitigationResult(
    boolean success,
    boolean cancelPacket,
    String  message
) {

    // ─── Factory methods ──────────────────────────────────────────────────────

    /** A successful result that does not cancel the packet. */
    public static MitigationResult ok() {
        return new MitigationResult(true, false, "ok");
    }

    /** A successful result that cancels the triggering packet. */
    public static MitigationResult cancel() {
        return new MitigationResult(true, true, "packet cancelled");
    }

    /** A successful result with a custom message. */
    public static MitigationResult ok(final String message) {
        return new MitigationResult(true, false, message);
    }

    /** A failed result (strategy could not execute). */
    public static MitigationResult failure(final String reason) {
        return new MitigationResult(false, false, reason);
    }

    /** A no-op result (strategy decided not to act). */
    public static MitigationResult noop() {
        return new MitigationResult(true, false, "noop");
    }
}
