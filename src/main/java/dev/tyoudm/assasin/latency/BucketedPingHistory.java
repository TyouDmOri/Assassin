/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.latency;

import dev.tyoudm.assasin.util.RingBuffer;

import java.util.Arrays;

/**
 * Bucketed ping history providing P50 / P95 / P99 percentiles over the
 * last 30 seconds (600 ticks at 20 TPS).
 *
 * <p>Ping samples are stored in a fixed-size {@link RingBuffer.OfInt} of
 * 600 slots (one per tick). Percentile queries sort a snapshot of the
 * current window — this is intentionally lazy (called infrequently by
 * {@link PingCompensator}) rather than maintained incrementally.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *   history.record(ping);          // called every tick
 *   int p99 = history.p99();       // used by checks
 * }</pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class BucketedPingHistory {

    /** Number of ticks in the history window (30s × 20 TPS). */
    public static final int WINDOW_TICKS = 600;

    /** Ring buffer storing raw ping samples (ms) per tick. */
    private final RingBuffer.OfInt samples = new RingBuffer.OfInt(WINDOW_TICKS);

    // ─── Record ───────────────────────────────────────────────────────────────

    /**
     * Records a new ping sample for the current tick.
     *
     * @param pingMs ping in milliseconds (clamped to [0, 5000])
     */
    public void record(final int pingMs) {
        samples.add(Math.max(0, Math.min(5000, pingMs)));
    }

    // ─── Percentiles ──────────────────────────────────────────────────────────

    /**
     * Returns the P50 (median) ping over the current window.
     *
     * @return P50 ping in ms, or 0 if no samples
     */
    public int p50() {
        return percentile(50);
    }

    /**
     * Returns the P95 ping over the current window.
     *
     * @return P95 ping in ms, or 0 if no samples
     */
    public int p95() {
        return percentile(95);
    }

    /**
     * Returns the P99 ping over the current window.
     *
     * @return P99 ping in ms, or 0 if no samples
     */
    public int p99() {
        return percentile(99);
    }

    /**
     * Returns the arithmetic mean ping over the current window.
     *
     * @return mean ping in ms, or 0 if no samples
     */
    public int mean() {
        final int n = samples.size();
        if (n == 0) return 0;
        long sum = 0;
        for (int i = 0; i < n; i++) sum += samples.get(i);
        return (int) (sum / n);
    }

    /**
     * Returns the most recently recorded ping sample.
     *
     * @return latest ping in ms, or 0 if no samples
     */
    public int latest() {
        return samples.isEmpty() ? 0 : samples.newest();
    }

    /**
     * Returns the number of samples currently stored.
     *
     * @return sample count (0–600)
     */
    public int sampleCount() {
        return samples.size();
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    /**
     * Computes the given percentile from a sorted snapshot of the window.
     *
     * @param pct percentile in [1, 100]
     * @return percentile value in ms
     */
    private int percentile(final int pct) {
        final int n = samples.size();
        if (n == 0) return 0;

        final int[] snapshot = new int[n];
        for (int i = 0; i < n; i++) snapshot[i] = samples.get(i);
        Arrays.sort(snapshot);

        // Nearest-rank method
        final int rank = (int) Math.ceil(pct / 100.0 * n);
        return snapshot[Math.min(rank, n) - 1];
    }
}
