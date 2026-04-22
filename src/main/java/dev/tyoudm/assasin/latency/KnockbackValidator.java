/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.latency;

/**
 * Tracks pending knockback vectors and validates the player's response.
 *
 * <p>When the server sends a {@code SET_ENTITY_VELOCITY} packet to a player,
 * the expected velocity vector is stored here. When the next movement packet
 * arrives, {@code VelocityA/B/C} calls {@link #consume()} to retrieve the
 * expected vector and compare it against the observed delta.
 *
 * <h2>Timing</h2>
 * The player must respond within {@code baseWindow + pingCompensation} ticks.
 * If no response arrives in time, the pending entry is discarded.
 *
 * <h2>Block-hit modifier</h2>
 * When {@link dev.tyoudm.assasin.exempt.ExemptType#BLOCK_HIT} is active,
 * the expected horizontal magnitude is multiplied by 0.5 (shield absorption).
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class KnockbackValidator {

    /** Base response window in ticks (before ping compensation). */
    public static final int BASE_WINDOW_TICKS = 3;

    // ─── Pending KB entry ─────────────────────────────────────────────────────

    /**
     * A pending knockback entry awaiting the player's response.
     *
     * @param velX       expected X velocity component (blocks/tick)
     * @param velY       expected Y velocity component (blocks/tick)
     * @param velZ       expected Z velocity component (blocks/tick)
     * @param sentTick   server tick when the velocity packet was sent
     * @param expiryTick server tick after which this entry is considered stale
     */
    public record PendingKnockback(
        double velX, double velY, double velZ,
        long sentTick, long expiryTick
    ) {
        /** Horizontal magnitude of the expected velocity vector. */
        public double horizontalMagnitude() {
            return Math.sqrt(velX * velX + velZ * velZ);
        }
    }

    // ─── State ────────────────────────────────────────────────────────────────

    /** The single pending knockback entry (only one can be active at a time). */
    private PendingKnockback pending = null;

    // ─── Record ───────────────────────────────────────────────────────────────

    /**
     * Records an outgoing knockback vector.
     *
     * <p>If a previous entry is still pending, it is overwritten (the server
     * sent a new velocity before the player responded to the old one).
     *
     * @param velX        expected X velocity (blocks/tick)
     * @param velY        expected Y velocity (blocks/tick)
     * @param velZ        expected Z velocity (blocks/tick)
     * @param sentTick    current server tick
     * @param pingMs      player's current ping in ms (for expiry calculation)
     */
    public void record(
            final double velX, final double velY, final double velZ,
            final long sentTick, final int pingMs) {
        final int window = BASE_WINDOW_TICKS + PingCompensator.velocityCompensationTicks(pingMs);
        pending = new PendingKnockback(velX, velY, velZ, sentTick, sentTick + window);
    }

    // ─── Consume ──────────────────────────────────────────────────────────────

    /**
     * Retrieves and removes the pending knockback entry if it is still valid.
     *
     * <p>Returns {@code null} if:
     * <ul>
     *   <li>No entry is pending.</li>
     *   <li>The entry has expired ({@code currentTick > expiryTick}).</li>
     * </ul>
     *
     * @param currentTick the current server tick
     * @return the pending {@link PendingKnockback}, or {@code null}
     */
    public PendingKnockback consume(final long currentTick) {
        if (pending == null) return null;
        if (currentTick > pending.expiryTick()) {
            pending = null;
            return null;
        }
        final PendingKnockback result = pending;
        pending = null;
        return result;
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if a knockback entry is pending and not yet expired.
     *
     * @param currentTick the current server tick
     * @return {@code true} if a valid pending entry exists
     */
    public boolean hasPending(final long currentTick) {
        if (pending == null) return false;
        if (currentTick > pending.expiryTick()) {
            pending = null;
            return false;
        }
        return true;
    }

    /**
     * Returns the current pending entry without consuming it, or {@code null}.
     *
     * @return pending entry or {@code null}
     */
    public PendingKnockback peek() {
        return pending;
    }

    /**
     * Clears the pending entry unconditionally.
     * Call on player death, respawn, or teleport.
     */
    public void clear() {
        pending = null;
    }
}
