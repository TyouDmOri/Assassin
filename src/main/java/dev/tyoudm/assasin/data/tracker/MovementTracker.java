/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.tracker;

import dev.tyoudm.assasin.util.RingBuffer;

/**
 * Tracks per-player movement history for movement checks.
 *
 * <p>Stores the last {@link #HISTORY} position deltas and derived values
 * (horizontal speed, vertical speed, onGround transitions) in primitive
 * ring buffers. All fields are accessed from the netty pipeline thread only.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MovementTracker {

    /** Number of ticks of movement history to retain. */
    public static final int HISTORY = 20;

    // ─── History buffers ──────────────────────────────────────────────────────

    /** Horizontal speed (blocks/tick) per tick. */
    private final RingBuffer.OfDouble speedH = new RingBuffer.OfDouble(HISTORY);

    /** Vertical delta (blocks/tick, positive = up) per tick. */
    private final RingBuffer.OfDouble speedY = new RingBuffer.OfDouble(HISTORY);

    /** onGround flag per tick (1.0 = on ground, 0.0 = in air). */
    private final RingBuffer.OfDouble groundHistory = new RingBuffer.OfDouble(HISTORY);

    // ─── Current-tick state ───────────────────────────────────────────────────

    /** Horizontal speed this tick (blocks/tick). */
    private double currentSpeedH;

    /** Vertical delta this tick (blocks/tick). */
    private double currentSpeedY;

    /** Number of consecutive ticks the player has been airborne. */
    private int airTicks;

    /** Number of consecutive ticks the player has been on the ground. */
    private int groundTicks;

    /** Number of consecutive ticks the player has been moving faster than walk speed. */
    private int sprintTicks;

    /** Accumulated fall distance since last onGround=true. */
    private double fallDistance;

    // ─── Timer tracking ───────────────────────────────────────────────────────

    /** Movement packets received in the current 20-tick window. */
    private int  movementPackets;

    /** Server tick when the current window started. */
    private long windowStartTick;

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Called every movement packet to update all derived values.
     *
     * @param dx       X delta (current - last)
     * @param dy       Y delta (current - last)
     * @param dz       Z delta (current - last)
     * @param onGround client-reported onGround flag
     */
    public void update(final double dx, final double dy, final double dz, final boolean onGround) {
        currentSpeedH = Math.sqrt(dx * dx + dz * dz);
        currentSpeedY = dy;

        speedH.add(currentSpeedH);
        speedY.add(currentSpeedY);
        groundHistory.add(onGround ? 1.0 : 0.0);

        if (onGround) {
            airTicks    = 0;
            groundTicks++;
            fallDistance = 0.0;
        } else {
            airTicks++;
            groundTicks = 0;
            if (dy < 0) fallDistance += Math.abs(dy);
        }

        sprintTicks = currentSpeedH > 0.2806 ? sprintTicks + 1 : 0;
        movementPackets++;
    }

    /**
     * Called once per 20-tick window by {@code TimerA} to read and reset the counter.
     *
     * @param currentTick current server tick
     * @return packets received since last reset, or -1 if window not yet elapsed
     */
    public int pollMovementPackets(final long currentTick) {
        if (windowStartTick == 0) {
            windowStartTick = currentTick;
            return -1;
        }
        if (currentTick - windowStartTick < 20) return -1;
        final int count = movementPackets;
        movementPackets = 0;
        windowStartTick = currentTick;
        return count;
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public double getCurrentSpeedH()          { return currentSpeedH; }
    public double getCurrentSpeedY()          { return currentSpeedY; }
    public int    getAirTicks()               { return airTicks; }
    public int    getGroundTicks()            { return groundTicks; }
    public int    getSprintTicks()            { return sprintTicks; }
    public double getFallDistance()           { return fallDistance; }
    public RingBuffer.OfDouble getSpeedH()    { return speedH; }
    public RingBuffer.OfDouble getSpeedY()    { return speedY; }
    public RingBuffer.OfDouble getGroundHistory() { return groundHistory; }

        // Añade esto a tu MovementTracker
    private int pendingTeleportId = -1;
    private long lastTeleportTime;

    public void handleTeleport(int id) {
    this.pendingTeleportId = id;
    this.lastTeleportTime = System.currentTimeMillis();
    }

    public boolean isTeleportPending() {
        return pendingTeleportId != -1;
    }

    public void confirmTeleport(int id) {
        if (this.pendingTeleportId == id) {
            this.pendingTeleportId = -1;
        }
    }
    /** Resets all state. Call on respawn or teleport. */
    public void reset() {
        speedH.clear();
        speedY.clear();
        groundHistory.clear();
        currentSpeedH   = 0.0;
        currentSpeedY   = 0.0;
        airTicks        = 0;
        groundTicks     = 0;
        sprintTicks     = 0;
        fallDistance    = 0.0;
        movementPackets = 0;
        windowStartTick = 0L;
    }
}
