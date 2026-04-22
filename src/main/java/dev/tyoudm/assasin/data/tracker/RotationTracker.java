/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.tracker;

import dev.tyoudm.assasin.util.MathUtil;
import dev.tyoudm.assasin.util.RingBuffer;
import dev.tyoudm.assasin.util.WelfordStats;

/**
 * Tracks per-player rotation (yaw/pitch) history for aim and killaura checks.
 *
 * <p>Maintains ring buffers of raw yaw/pitch values and their deltas,
 * plus a {@link WelfordStats} accumulator for online variance computation.
 * All fields are accessed from the netty pipeline thread only.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class RotationTracker {

    /** Number of rotation samples to retain. */
    public static final int HISTORY = 40;

    // ─── History buffers ──────────────────────────────────────────────────────

    /** Raw yaw values (degrees). */
    private final RingBuffer.OfDouble yawHistory   = new RingBuffer.OfDouble(HISTORY);
    /** Raw pitch values (degrees). */
    private final RingBuffer.OfDouble pitchHistory = new RingBuffer.OfDouble(HISTORY);
    /** Absolute yaw deltas per tick. */
    private final RingBuffer.OfDouble yawDeltas    = new RingBuffer.OfDouble(HISTORY);
    /** Absolute pitch deltas per tick. */
    private final RingBuffer.OfDouble pitchDeltas  = new RingBuffer.OfDouble(HISTORY);

    // ─── Online stats ─────────────────────────────────────────────────────────

    /** Welford accumulator for yaw delta variance (used by AimA GCD). */
    private final WelfordStats yawDeltaStats   = new WelfordStats();
    /** Welford accumulator for pitch delta variance. */
    private final WelfordStats pitchDeltaStats = new WelfordStats();

    // ─── Current-tick state ───────────────────────────────────────────────────

    private float  currentYaw;
    private float  currentPitch;
    private double lastDeltaYaw;
    private double lastDeltaPitch;

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Called every rotation packet to update history and stats.
     *
     * @param yaw   new yaw in degrees
     * @param pitch new pitch in degrees
     */
    public void update(final float yaw, final float pitch) {
        final double dYaw   = MathUtil.angleDiff(yaw,   currentYaw);
        final double dPitch = MathUtil.angleDiff(pitch, currentPitch);

        currentYaw   = yaw;
        currentPitch = pitch;
        lastDeltaYaw   = dYaw;
        lastDeltaPitch = dPitch;

        yawHistory.add(yaw);
        pitchHistory.add(pitch);
        yawDeltas.add(dYaw);
        pitchDeltas.add(dPitch);

        yawDeltaStats.add(dYaw);
        pitchDeltaStats.add(dPitch);
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public float  getCurrentYaw()               { return currentYaw; }
    public float  getCurrentPitch()             { return currentPitch; }
    public double getLastDeltaYaw()             { return lastDeltaYaw; }
    public double getLastDeltaPitch()           { return lastDeltaPitch; }
    public RingBuffer.OfDouble getYawHistory()  { return yawHistory; }
    public RingBuffer.OfDouble getPitchHistory(){ return pitchHistory; }
    public RingBuffer.OfDouble getYawDeltas()   { return yawDeltas; }
    public RingBuffer.OfDouble getPitchDeltas() { return pitchDeltas; }
    public WelfordStats getYawDeltaStats()      { return yawDeltaStats; }
    public WelfordStats getPitchDeltaStats()    { return pitchDeltaStats; }

    /** Resets all state. Call on respawn. */
    public void reset() {
        yawHistory.clear();
        pitchHistory.clear();
        yawDeltas.clear();
        pitchDeltas.clear();
        yawDeltaStats.reset();
        pitchDeltaStats.reset();
        currentYaw     = 0f;
        currentPitch   = 0f;
        lastDeltaYaw   = 0.0;
        lastDeltaPitch = 0.0;
    }
}
