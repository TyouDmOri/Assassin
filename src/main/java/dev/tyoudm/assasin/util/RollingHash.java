/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.util;

/**
 * Rabin-Karp rolling hash for O(1) n-gram fingerprinting.
 *
 * <p>Used by {@code MacroSequenceA} to detect repeated input sequences
 * (trigrams / tetragrams) in the {@code ActionTracker} ring buffer.
 * Each call to {@link #roll(long, long)} updates the hash in O(1) by
 * removing the oldest element and adding the newest.
 *
 * <h2>Algorithm</h2>
 * <pre>
 *   hash = (hash - oldest * base^(n-1)) * base + newest   (mod prime)
 * </pre>
 *
 * <h2>Parameters</h2>
 * <ul>
 *   <li>{@code BASE}  = 1_000_003 (large prime, reduces collisions for long inputs)</li>
 *   <li>{@code MOD}   = 1_000_000_007 (standard competitive-programming prime)</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *   RollingHash rh = new RollingHash(4); // tetragrams
 *   // seed with first n-1 elements
 *   for (long action : firstThree) rh.seed(action);
 *   // then roll one element at a time
 *   long hash = rh.roll(outgoing, incoming);
 * }</pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class RollingHash {

    // ─── Constants ────────────────────────────────────────────────────────────

    /** Polynomial base — large prime to reduce collisions. */
    public static final long BASE = 1_000_003L;

    /** Modulus — standard 10^9+7 prime. */
    public static final long MOD  = 1_000_000_007L;

    // ─── State ────────────────────────────────────────────────────────────────

    /** Window size (n-gram length). */
    private final int  windowSize;

    /** Precomputed BASE^(windowSize-1) mod MOD. */
    private final long highPow;

    /** Current hash value. */
    private long hash;

    /** Number of elements seeded so far (capped at windowSize). */
    private int seeded;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new rolling hash for n-grams of the given window size.
     *
     * @param windowSize n-gram length (e.g., 3 for trigrams, 4 for tetragrams)
     * @throws IllegalArgumentException if windowSize &lt; 1
     */
    public RollingHash(final int windowSize) {
        if (windowSize < 1) {
            throw new IllegalArgumentException("windowSize must be >= 1, got: " + windowSize);
        }
        this.windowSize = windowSize;
        this.highPow    = pow(BASE, windowSize - 1, MOD);
        this.hash       = 0L;
        this.seeded     = 0;
    }

    // ─── API ──────────────────────────────────────────────────────────────────

    /**
     * Seeds the hash with one element during initialization.
     * Call this {@code windowSize - 1} times before the first {@link #roll}.
     *
     * @param value the element to add
     */
    public void seed(final long value) {
        hash = (hash * BASE + (value % MOD + MOD) % MOD) % MOD;
        if (seeded < windowSize) seeded++;
    }

    /**
     * Rolls the window by removing {@code outgoing} and adding {@code incoming}.
     * Returns the new hash for the current window.
     *
     * @param outgoing the element leaving the window (oldest)
     * @param incoming the element entering the window (newest)
     * @return updated hash value in [0, MOD)
     */
    public long roll(final long outgoing, final long incoming) {
        final long out = (outgoing % MOD + MOD) % MOD;
        final long in  = (incoming  % MOD + MOD) % MOD;
        hash = ((hash - out * highPow % MOD + MOD) * BASE + in) % MOD;
        return hash;
    }

    /**
     * Returns the current hash value without modifying state.
     *
     * @return current hash in [0, MOD)
     */
    public long current() {
        return hash;
    }

    /**
     * Resets the hash to its initial state.
     */
    public void reset() {
        hash   = 0L;
        seeded = 0;
    }

    /**
     * Returns the window size this hash was constructed with.
     *
     * @return window size
     */
    public int windowSize() {
        return windowSize;
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    /**
     * Modular fast exponentiation: {@code base^exp mod m}.
     *
     * @param base the base
     * @param exp  the exponent (≥ 0)
     * @param m    the modulus
     * @return {@code base^exp mod m}
     */
    private static long pow(long base, long exp, final long m) {
        long result = 1L;
        base %= m;
        while (exp > 0L) {
            if ((exp & 1L) == 1L) result = result * base % m;
            base = base * base % m;
            exp >>= 1;
        }
        return result;
    }
}
