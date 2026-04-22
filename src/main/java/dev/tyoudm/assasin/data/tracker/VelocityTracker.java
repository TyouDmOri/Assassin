/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.tracker;

/**
 * Tracks pending server-sent velocity (knockback) and the player's observed response.
 *
 * <p>When the server sends {@code SET_ENTITY_VELOCITY}, the expected vector is
 * stored here. When the next movement packet arrives, {@code VelocityA/B/C}
 * reads the expected vector and computes the acceptance ratio.
 *
 * <p>This tracker is the high-level companion to
 * {@link dev.tyoudm.assasin.latency.KnockbackValidator}; it adds
 * per-tick bookkeeping (accepted count, rejected count) for VL accumulation.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class VelocityTracker {

    // ─── Pending KB ───────────────────────────────────────────────────────────

    /** Expected X velocity component (blocks/tick). */
    private double pendingVelX;
    /** Expected Y velocity component (blocks/tick). */
    private double pendingVelY;
    /** Expected Z velocity component (blocks/tick). */
    private double pendingVelZ;

    /** Whether a pending KB entry is currently active. */
    private boolean hasPending;

    /** Server tick when the pending KB was recorded. */
    private long pendingTick;

    // ─── Statistics ───────────────────────────────────────────────────────────

    /** Number of KB packets accepted (ratio ≥ threshold) since last reset. */
    private int acceptedCount;

    /** Number of KB packets rejected (ratio < threshold) since last reset. */
    private int rejectedCount;

    // ─── Record ───────────────────────────────────────────────────────────────

    /**
     * Records an outgoing knockback vector.
     *
     * @param velX       expected X velocity (blocks/tick)
     * @param velY       expected Y velocity (blocks/tick)
     * @param velZ       expected Z velocity (blocks/tick)
     * @param currentTick server tick when the packet was sent
     */
    public void recordPending(final double velX, final double velY, final double velZ,
                              final long currentTick) {
        pendingVelX  = velX;
        pendingVelY  = velY;
        pendingVelZ  = velZ;
        hasPending   = true;
        pendingTick  = currentTick;
    }

    // ─── Consume ──────────────────────────────────────────────────────────────

    /**
     * Consumes the pending KB entry and returns whether it was accepted.
     *
     * <p>Acceptance is determined by the caller (VelocityA/B/C) comparing
     * the observed delta against the expected vector. This method only
     * clears the pending state and increments counters.
     *
     * @param accepted {@code true} if the player's response was within tolerance
     */
    public void consume(final boolean accepted) {
        hasPending = false;
        if (accepted) acceptedCount++;
        else          rejectedCount++;
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    public boolean hasPending()    { return hasPending; }
    public double  getPendingVelX(){ return pendingVelX; }
    public double  getPendingVelY(){ return pendingVelY; }
    public double  getPendingVelZ(){ return pendingVelZ; }
    public long    getPendingTick(){ return pendingTick; }
    public int     getAccepted()   { return acceptedCount; }
    public int     getRejected()   { return rejectedCount; }

    /**
     * Returns the horizontal magnitude of the pending expected velocity.
     *
     * @return horizontal magnitude (blocks/tick)
     */
    public double getPendingHorizontalMagnitude() {
        return Math.sqrt(pendingVelX * pendingVelX + pendingVelZ * pendingVelZ);
    }

    /** Resets counters and clears pending state. Call on respawn or death. */
    public void reset() {
        hasPending    = false;
        pendingVelX   = 0.0;
        pendingVelY   = 0.0;
        pendingVelZ   = 0.0;
        pendingTick   = 0L;
        acceptedCount = 0;
        rejectedCount = 0;
    }
}
