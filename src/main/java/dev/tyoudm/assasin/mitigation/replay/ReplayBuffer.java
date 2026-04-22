/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation.replay;

/**
 * Per-player replay buffer storing the last {@link #CAPACITY} ticks of
 * position/rotation snapshots for staff review via {@code /assasin replay}.
 *
 * <p>Uses parallel primitive arrays (zero autoboxing) in a circular layout.
 * One snapshot is recorded per movement packet (approximately one per tick).
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class ReplayBuffer {

    /** Number of ticks of replay history to retain (200 ticks = 10 seconds). */
    public static final int CAPACITY = 200;

    // ─── Snapshot record ──────────────────────────────────────────────────────

    /**
     * A single tick snapshot of player state.
     *
     * @param tick      server tick
     * @param x         player X
     * @param y         player Y
     * @param z         player Z
     * @param yaw       player yaw (degrees)
     * @param pitch     player pitch (degrees)
     * @param onGround  client-reported onGround flag
     * @param speedH    horizontal speed (blocks/tick)
     * @param speedY    vertical speed (blocks/tick)
     */
    public record Snapshot(
        long    tick,
        double  x,
        double  y,
        double  z,
        float   yaw,
        float   pitch,
        boolean onGround,
        double  speedH,
        double  speedY
    ) {}

    // ─── Parallel primitive arrays ────────────────────────────────────────────

    private final long[]    ticks     = new long[CAPACITY];
    private final double[]  xs        = new double[CAPACITY];
    private final double[]  ys        = new double[CAPACITY];
    private final double[]  zs        = new double[CAPACITY];
    private final float[]   yaws      = new float[CAPACITY];
    private final float[]   pitches   = new float[CAPACITY];
    private final boolean[] grounds   = new boolean[CAPACITY];
    private final double[]  speedsH   = new double[CAPACITY];
    private final double[]  speedsY   = new double[CAPACITY];

    private int  writeIdx = 0;
    private int  size     = 0;

    // ─── Record ───────────────────────────────────────────────────────────────

    /**
     * Records a new snapshot, overwriting the oldest when full.
     *
     * @param tick     server tick
     * @param x        player X
     * @param y        player Y
     * @param z        player Z
     * @param yaw      player yaw
     * @param pitch    player pitch
     * @param onGround onGround flag
     * @param speedH   horizontal speed
     * @param speedY   vertical speed
     */
    public void record(final long tick,
                       final double x, final double y, final double z,
                       final float yaw, final float pitch,
                       final boolean onGround,
                       final double speedH, final double speedY) {
        ticks[writeIdx]   = tick;
        xs[writeIdx]      = x;
        ys[writeIdx]      = y;
        zs[writeIdx]      = z;
        yaws[writeIdx]    = yaw;
        pitches[writeIdx] = pitch;
        grounds[writeIdx] = onGround;
        speedsH[writeIdx] = speedH;
        speedsY[writeIdx] = speedY;

        writeIdx = (writeIdx + 1) % CAPACITY;
        if (size < CAPACITY) size++;
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    /**
     * Returns the snapshot at logical index {@code i} (0 = oldest, size-1 = newest).
     *
     * @param i logical index
     * @return snapshot at index {@code i}
     * @throws IndexOutOfBoundsException if {@code i} is out of range
     */
    public Snapshot get(final int i) {
        if (i < 0 || i >= size) {
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for size " + size);
        }
        final int idx = (writeIdx - size + i + CAPACITY) % CAPACITY;
        return new Snapshot(ticks[idx], xs[idx], ys[idx], zs[idx],
                            yaws[idx], pitches[idx], grounds[idx],
                            speedsH[idx], speedsY[idx]);
    }

    /**
     * Returns the most recent snapshot, or {@code null} if empty.
     *
     * @return newest snapshot
     */
    public Snapshot newest() {
        if (size == 0) return null;
        return get(size - 1);
    }

    /**
     * Returns all snapshots as an array, oldest first.
     *
     * @return snapshot array of length {@link #size()}
     */
    public Snapshot[] toArray() {
        final Snapshot[] arr = new Snapshot[size];
        for (int i = 0; i < size; i++) arr[i] = get(i);
        return arr;
    }

    /** Returns the number of snapshots currently stored. */
    public int size()        { return size; }

    /** Returns {@code true} if no snapshots are stored. */
    public boolean isEmpty() { return size == 0; }

    /** Clears all snapshots. Call on respawn or teleport. */
    public void clear() {
        writeIdx = 0;
        size     = 0;
    }
}
