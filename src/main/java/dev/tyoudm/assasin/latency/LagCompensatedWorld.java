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
 * Lag-compensated position history for a single player.
 *
 * <p>Stores the last {@link #HISTORY_TICKS} position snapshots in a
 * fixed-size circular array (zero autoboxing). Used by {@code ReachB}
 * (target rewind) to look up where the target <em>was</em> when the
 * attacker's packet was sent, accounting for the attacker's ping.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *   // Every movement tick:
 *   world.record(currentTick, x, y, z);
 *
 *   // In ReachB, when processing an attack from an attacker with pingMs:
 *   int rewindTicks = PingCompensator.velocityCompensationTicks(pingMs);
 *   LagCompensatedWorld.Snapshot snap = world.getAtTick(currentTick - rewindTicks);
 * }</pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class LagCompensatedWorld {

    /** Number of ticks of position history to retain (2 seconds at 20 TPS). */
    public static final int HISTORY_TICKS = 40;

    // ─── Snapshot ─────────────────────────────────────────────────────────────

    /**
     * Immutable position snapshot at a specific server tick.
     *
     * @param tick the server tick this snapshot was recorded at
     * @param x    X coordinate
     * @param y    Y coordinate
     * @param z    Z coordinate
     */
    public record Snapshot(long tick, double x, double y, double z) {}

    // ─── Storage (parallel primitive arrays — zero autoboxing) ────────────────

    private final long[]   ticks = new long[HISTORY_TICKS];
    private final double[] xs    = new double[HISTORY_TICKS];
    private final double[] ys    = new double[HISTORY_TICKS];
    private final double[] zs    = new double[HISTORY_TICKS];

    /** Write index (circular). */
    private int  writeIdx = 0;
    /** Number of snapshots stored (capped at HISTORY_TICKS). */
    private int  size     = 0;

    // ─── Record ───────────────────────────────────────────────────────────────

    /**
     * Records a position snapshot for the given tick.
     * Overwrites the oldest entry when the buffer is full.
     *
     * @param tick the current server tick
     * @param x    X coordinate
     * @param y    Y coordinate
     * @param z    Z coordinate
     */
    public void record(final long tick, final double x, final double y, final double z) {
        ticks[writeIdx] = tick;
        xs[writeIdx]    = x;
        ys[writeIdx]    = y;
        zs[writeIdx]    = z;
        writeIdx = (writeIdx + 1) % HISTORY_TICKS;
        if (size < HISTORY_TICKS) size++;
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    /**
     * Returns the snapshot closest to the requested tick, or {@code null}
     * if no history is available.
     *
     * <p>Searches backwards from the newest entry for the first snapshot
     * whose tick is ≤ {@code targetTick}. This is O(n) but n ≤ 40.
     *
     * @param targetTick the tick to rewind to
     * @return the closest snapshot, or {@code null}
     */
    public Snapshot getAtTick(final long targetTick) {
        if (size == 0) return null;

        long   bestDiff = Long.MAX_VALUE;
        int    bestIdx  = -1;

        for (int i = 0; i < size; i++) {
            // Logical index i → physical index
            final int physIdx = (writeIdx - size + i + HISTORY_TICKS) % HISTORY_TICKS;
            final long diff   = Math.abs(ticks[physIdx] - targetTick);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestIdx  = physIdx;
            }
        }

        if (bestIdx < 0) return null;
        return new Snapshot(ticks[bestIdx], xs[bestIdx], ys[bestIdx], zs[bestIdx]);
    }

    /**
     * Returns the most recently recorded snapshot, or {@code null} if empty.
     *
     * @return newest snapshot
     */
    public Snapshot newest() {
        if (size == 0) return null;
        final int idx = (writeIdx - 1 + HISTORY_TICKS) % HISTORY_TICKS;
        return new Snapshot(ticks[idx], xs[idx], ys[idx], zs[idx]);
    }

    /**
     * Returns the number of snapshots currently stored.
     *
     * @return snapshot count (0–40)
     */
    public int size() {
        return size;
    }

    /**
     * Clears all stored snapshots.
     * Call on player respawn or teleport.
     */
    public void clear() {
        writeIdx = 0;
        size     = 0;
    }
}
