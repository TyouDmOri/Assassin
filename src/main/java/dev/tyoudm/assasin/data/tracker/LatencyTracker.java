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
 * Tracker-layer view of per-player latency state.
 *
 * <p>This class lives in the {@code data.tracker} package and provides
 * a lightweight, check-friendly interface over the raw latency data
 * maintained by {@link dev.tyoudm.assasin.latency.LatencyTracker}.
 *
 * <p>Checks should prefer reading from this tracker rather than reaching
 * directly into the latency subsystem, keeping the dependency graph clean.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class LatencyTracker {

    /** Number of RTT samples to retain for trend analysis. */
    public static final int HISTORY = 20;

    // ─── RTT history ──────────────────────────────────────────────────────────

    /** Ring buffer of recent RTT samples (ms). */
    private final RingBuffer.OfInt rttHistory = new RingBuffer.OfInt(HISTORY);

    // ─── Current state ────────────────────────────────────────────────────────

    /** Most recent RTT in ms. */
    private int currentRttMs;

    /** Smoothed RTT (exponential moving average, α=0.2). */
    private double smoothedRttMs;

    /** Number of consecutive ticks with RTT > 300ms. */
    private int highPingTicks;

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Called each tick with the latest RTT measurement.
     *
     * @param rttMs current round-trip time in milliseconds
     */
    public void update(final int rttMs) {
        currentRttMs  = rttMs;
        smoothedRttMs = smoothedRttMs == 0.0
                ? rttMs
                : 0.8 * smoothedRttMs + 0.2 * rttMs;
        rttHistory.add(rttMs);
        highPingTicks = rttMs > 300 ? highPingTicks + 1 : 0;
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /** Returns the most recent RTT in ms. */
    public int getCurrentRttMs()       { return currentRttMs; }

    /** Returns the exponentially smoothed RTT in ms. */
    public double getSmoothedRttMs()   { return smoothedRttMs; }

    /** Returns the number of consecutive high-ping ticks (RTT > 300ms). */
    public int getHighPingTicks()      { return highPingTicks; }

    /** Returns the RTT history ring buffer. */
    public RingBuffer.OfInt getRttHistory() { return rttHistory; }

    /** Resets all state. Call on respawn or disconnect. */
    public void reset() {
        rttHistory.clear();
        currentRttMs  = 0;
        smoothedRttMs = 0.0;
        highPingTicks = 0;
    }
}
