/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation.buffer;

/**
 * Per-check violation level (VL) buffer with automatic exponential decay.
 *
 * <p>Accumulates violation points when a check flags and decays them
 * passively each tick. This prevents false positives from brief anomalies
 * while still catching persistent cheating.
 *
 * <h2>Decay formula</h2>
 * <pre>
 *   vl = max(0, vl - decayRate)   // called once per tick
 * </pre>
 *
 * <h2>Thread safety</h2>
 * Not thread-safe. Each check instance owns one buffer per player;
 * access is serialized by the packet-processing model.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class ViolationBuffer {

    /** Default VL decay per tick (configurable via checks.yml in FASE 18). */
    public static final double DEFAULT_DECAY = 0.05;

    // ─── State ────────────────────────────────────────────────────────────────

    /** Current violation level. */
    private double vl;

    /** Maximum VL before the buffer is considered "maxed out". */
    private final double maxVl;

    /** VL decay applied per tick. */
    private final double decayRate;

    /** Total number of flags accumulated (never decays — for statistics). */
    private long totalFlags;

    /** Server tick of the last flag. */
    private long lastFlagTick;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new violation buffer.
     *
     * @param maxVl     maximum violation level (e.g., 10.0)
     * @param decayRate VL decay per tick (e.g., 0.05)
     */
    public ViolationBuffer(final double maxVl, final double decayRate) {
        this.maxVl     = maxVl;
        this.decayRate = decayRate;
    }

    /**
     * Creates a new violation buffer with the default decay rate.
     *
     * @param maxVl maximum violation level
     */
    public ViolationBuffer(final double maxVl) {
        this(maxVl, DEFAULT_DECAY);
    }

    // ─── Flag ─────────────────────────────────────────────────────────────────

    /**
     * Adds {@code points} to the violation level and returns the new VL.
     *
     * @param points      violation points to add (must be &gt; 0)
     * @param currentTick current server tick
     * @return new violation level after adding points (capped at maxVl)
     */
    public double flag(final double points, final long currentTick) {
        vl = Math.min(maxVl, vl + points);
        totalFlags++;
        lastFlagTick = currentTick;
        return vl;
    }

    // ─── Decay ────────────────────────────────────────────────────────────────

    /**
     * Applies one tick of decay to the violation level.
     * Call once per server tick for each active player.
     */
    public void decay() {
        vl = Math.max(0.0, vl - decayRate);
    }

    /**
     * Applies {@code ticks} worth of decay at once.
     * Useful when a player was offline or exempt for multiple ticks.
     *
     * @param ticks number of ticks of decay to apply
     */
    public void decayTicks(final int ticks) {
        vl = Math.max(0.0, vl - decayRate * ticks);
    }

    // ─── Reset ────────────────────────────────────────────────────────────────

    /**
     * Resets the violation level to zero.
     * Does not reset {@link #totalFlags}.
     */
    public void reset() {
        vl = 0.0;
    }

    /**
     * Resets both the violation level and the total flag counter.
     */
    public void resetAll() {
        vl         = 0.0;
        totalFlags = 0L;
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    /** Returns the current violation level. */
    public double getVl()           { return vl; }

    /** Returns the maximum violation level. */
    public double getMaxVl()        { return maxVl; }

    /** Returns the decay rate per tick. */
    public double getDecayRate()    { return decayRate; }

    /** Returns the total number of flags accumulated. */
    public long   getTotalFlags()   { return totalFlags; }

    /** Returns the server tick of the last flag. */
    public long   getLastFlagTick() { return lastFlagTick; }

    /**
     * Returns {@code true} if the current VL meets or exceeds the given threshold.
     *
     * @param threshold the VL threshold to check
     * @return {@code true} if vl &ge; threshold
     */
    public boolean exceeds(final double threshold) {
        return vl >= threshold;
    }
}
