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
import dev.tyoudm.assasin.util.WelfordStats;

/**
 * Tracks per-player attack timing for AutoClicker and CPS checks.
 *
 * <p>Records inter-attack intervals (ms) in a ring buffer and maintains
 * a {@link WelfordStats} accumulator for online variance. Used by
 * {@code AutoClickerA/B/C} and {@code MacroClickerA}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class AttackTracker {

    /** Number of attack interval samples to retain. */
    public static final int HISTORY = 64;

    // ─── Interval history ─────────────────────────────────────────────────────

    /** Ring buffer of inter-attack intervals in milliseconds. */
    private final RingBuffer.OfLong intervals = new RingBuffer.OfLong(HISTORY);

    /** Online variance stats for click intervals. */
    private final WelfordStats intervalStats = new WelfordStats();

    // ─── State ────────────────────────────────────────────────────────────────

    /** System time (ms) of the last attack. */
    private long lastAttackMs;

    /** Number of attacks in the current second window. */
    private int  attacksThisSecond;

    /** System time (ms) when the current second window started. */
    private long secondWindowStart;

    /** Peak CPS observed in any 1-second window. */
    private int  peakCps;

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Records a new attack event.
     *
     * @param nowMs current system time in milliseconds
     */
    public void recordAttack(final long nowMs) {
        if (lastAttackMs > 0) {
            final long interval = nowMs - lastAttackMs;
            if (interval > 0 && interval < 5000) { // sanity: ignore gaps > 5s
                intervals.add(interval);
                intervalStats.add(interval);
            }
        }
        lastAttackMs = nowMs;

        // Rolling 1-second CPS window
        if (nowMs - secondWindowStart >= 1000L) {
            if (attacksThisSecond > peakCps) peakCps = attacksThisSecond;
            attacksThisSecond  = 1;
            secondWindowStart  = nowMs;
        } else {
            attacksThisSecond++;
        }
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public RingBuffer.OfLong getIntervals()   { return intervals; }
    public WelfordStats      getIntervalStats(){ return intervalStats; }
    public long              getLastAttackMs() { return lastAttackMs; }
    public int               getAttacksThisSecond() { return attacksThisSecond; }
    public int               getPeakCps()     { return peakCps; }

    /**
     * Returns the current CPS estimate based on the rolling window.
     *
     * @param nowMs current system time in ms
     * @return estimated CPS
     */
    public double getCurrentCps(final long nowMs) {
        final long elapsed = nowMs - secondWindowStart;
        if (elapsed <= 0) return 0.0;
        return attacksThisSecond * 1000.0 / elapsed;
    }

    /** Resets all state. Call on death or respawn. */
    public void reset() {
        intervals.clear();
        intervalStats.reset();
        lastAttackMs      = 0L;
        attacksThisSecond = 0;
        secondWindowStart = 0L;
        peakCps           = 0;
    }
}
