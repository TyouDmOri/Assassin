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
 * Ping compensation formulas for ASSASIN checks.
 *
 * <p>Each check category has a different compensation strategy because
 * the relationship between ping and the observable effect varies:
 * <ul>
 *   <li><b>Reach</b> — target position is delayed by half-RTT; compensate
 *       by rewinding the target's position by {@code ping/2} ms.</li>
 *   <li><b>Velocity</b> — knockback is applied server-side; the client
 *       sees it after one RTT. Add {@code ping/50} ticks of tolerance.</li>
 *   <li><b>Movement</b> — position packets are delayed by half-RTT;
 *       add a small positional tolerance proportional to ping.</li>
 *   <li><b>Timer</b> — packet rate is unaffected by ping; no compensation.</li>
 * </ul>
 *
 * <h2>Ceiling</h2>
 * All compensation is capped at {@link #MAX_COMPENSATED_PING_MS} (default 300ms).
 * Above this threshold, latency-sensitive checks are suppressed via
 * {@link dev.tyoudm.assasin.exempt.ExemptType#HIGH_PING}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class PingCompensator {

    /** Ping ceiling above which latency-sensitive checks are suppressed (ms). */
    public static final int MAX_COMPENSATED_PING_MS = 300;

    /** Milliseconds per server tick (50ms at 20 TPS). */
    private static final double MS_PER_TICK = 50.0;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /** Utility class — no instantiation. */
    private PingCompensator() {
        throw new UnsupportedOperationException("PingCompensator is a utility class.");
    }

    // ─── Compensation formulas ────────────────────────────────────────────────

    /**
     * Returns the reach compensation distance (blocks) for the given ping.
     *
     * <p>Formula: {@code ping_ms / 2 / 1000 * BASE_SPRINT_SPEED * 2}
     * (half-RTT × max target movement speed, doubled for safety margin).
     * Capped at 1.5 blocks.
     *
     * @param pingMs ping in milliseconds
     * @return additional reach tolerance in blocks
     */
    public static double reachCompensation(final int pingMs) {
        final int capped = Math.min(pingMs, MAX_COMPENSATED_PING_MS);
        // half-RTT in seconds × max entity speed (≈0.56 b/s sprint) × 2 safety
        return Math.min(1.5, (capped / 2.0 / 1000.0) * 0.56 * 2.0);
    }

    /**
     * Returns the velocity compensation in ticks for the given ping.
     *
     * <p>Formula: {@code ceil(ping_ms / MS_PER_TICK)}, capped at 6 ticks.
     * Used by VelocityA/B/C to extend the window in which a knockback
     * response is considered valid.
     *
     * @param pingMs ping in milliseconds
     * @return additional velocity response window in ticks
     */
    public static int velocityCompensationTicks(final int pingMs) {
        final int capped = Math.min(pingMs, MAX_COMPENSATED_PING_MS);
        return Math.min(6, (int) Math.ceil(capped / MS_PER_TICK));
    }

    /**
     * Returns the movement position tolerance (blocks) for the given ping.
     *
     * <p>Formula: {@code ping_ms / 2 / 1000 * BASE_SPRINT_SPEED}, capped at 0.5.
     * Used by SpeedA/B to widen the acceptable position delta.
     *
     * @param pingMs ping in milliseconds
     * @return additional position tolerance in blocks
     */
    public static double movementTolerance(final int pingMs) {
        final int capped = Math.min(pingMs, MAX_COMPENSATED_PING_MS);
        return Math.min(0.5, (capped / 2.0 / 1000.0) * 0.2806);
    }

    /**
     * Returns the transaction confirmation window in ticks for the given ping.
     *
     * <p>Formula: {@code ceil(ping_ms / MS_PER_TICK) + 2}, capped at 8 ticks.
     * Used by {@link TransactionBarrier} to decide when a setback is confirmed.
     *
     * @param pingMs ping in milliseconds
     * @return transaction confirmation window in ticks
     */
    public static int transactionWindowTicks(final int pingMs) {
        final int capped = Math.min(pingMs, MAX_COMPENSATED_PING_MS);
        return Math.min(8, (int) Math.ceil(capped / MS_PER_TICK) + 2);
    }

    /**
     * Returns {@code true} if the given ping exceeds the compensation ceiling.
     *
     * <p>Checks that use this should suppress themselves via
     * {@link dev.tyoudm.assasin.exempt.ExemptType#HIGH_PING}.
     *
     * @param pingMs ping in milliseconds
     * @return {@code true} if ping is above {@link #MAX_COMPENSATED_PING_MS}
     */
    public static boolean isHighPing(final int pingMs) {
        return pingMs > MAX_COMPENSATED_PING_MS;
    }
}
