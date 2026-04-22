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
 * Tracks per-player input state (sprint, sneak, jump toggles) for
 * movement and macro checks.
 *
 * <p>Records toggle counts and timing used by {@code TimerA},
 * {@code JumpResetA/B}, {@code StrafeA}, and macro checks.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class InputTracker {

    // ─── Sprint ───────────────────────────────────────────────────────────────

    /** Whether the player is currently sprinting. */
    private boolean sprinting;
    /** Server tick of the last sprint toggle (on or off). */
    private long    lastSprintToggleTick;
    /** Number of sprint toggles in the last 20 ticks. */
    private int     sprintToggles20t;

    // ─── Sneak ────────────────────────────────────────────────────────────────

    /** Whether the player is currently sneaking. */
    private boolean sneaking;
    /** Server tick of the last sneak toggle. */
    private long    lastSneakToggleTick;
    /** Number of sneak toggles in the last 20 ticks. */
    private int     sneakToggles20t;

    // ─── Jump ─────────────────────────────────────────────────────────────────

    /** Server tick of the last jump (onGround false → true → false). */
    private long    lastJumpTick;
    /** Number of jumps in the last 20 ticks. */
    private int     jumps20t;

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Updates sprint state.
     *
     * @param nowSprinting new sprint state
     * @param currentTick  current server tick
     */
    public void updateSprint(final boolean nowSprinting, final long currentTick) {
        if (nowSprinting != sprinting) {
            sprinting            = nowSprinting;
            lastSprintToggleTick = currentTick;
            sprintToggles20t++;
        }
    }

    /**
     * Updates sneak state.
     *
     * @param nowSneaking new sneak state
     * @param currentTick current server tick
     */
    public void updateSneak(final boolean nowSneaking, final long currentTick) {
        if (nowSneaking != sneaking) {
            sneaking            = nowSneaking;
            lastSneakToggleTick = currentTick;
            sneakToggles20t++;
        }
    }

    /**
     * Records a jump event (transition from ground to air with positive Y velocity).
     *
     * @param currentTick current server tick
     */
    public void recordJump(final long currentTick) {
        lastJumpTick = currentTick;
        jumps20t++;
    }

    /**
     * Decays toggle counters. Call once per tick.
     *
     * @param currentTick current server tick
     */
    public void decayCounters(final long currentTick) {
        // Simple decay: reset counters every 20 ticks
        if ((currentTick % 20) == 0) {
            sprintToggles20t = 0;
            sneakToggles20t  = 0;
            jumps20t         = 0;
        }
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public boolean isSprinting()             { return sprinting; }
    public long    getLastSprintToggleTick() { return lastSprintToggleTick; }
    public int     getSprintToggles20t()     { return sprintToggles20t; }
    public boolean isSneaking()              { return sneaking; }
    public long    getLastSneakToggleTick()  { return lastSneakToggleTick; }
    public int     getSneakToggles20t()      { return sneakToggles20t; }
    public long    getLastJumpTick()         { return lastJumpTick; }
    public int     getJumps20t()             { return jumps20t; }

    /** Resets all state. Call on respawn. */
    public void reset() {
        sprinting            = false;
        lastSprintToggleTick = 0L;
        sprintToggles20t     = 0;
        sneaking             = false;
        lastSneakToggleTick  = 0L;
        sneakToggles20t      = 0;
        lastJumpTick         = 0L;
        jumps20t             = 0;
    }
}
