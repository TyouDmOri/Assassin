/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.latency;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Transaction barrier for setback confirmation.
 *
 * <p>When the mitigation engine issues a setback (teleport), it must wait
 * for the client to confirm the teleport before resuming check processing.
 * This class tracks the "pre-setback" and "post-setback" transaction IDs
 * and signals when the client has caught up.
 *
 * <h2>Protocol</h2>
 * <ol>
 *   <li>Mitigation engine calls {@link #openBarrier(short, long)} with the
 *       transaction ID sent just before the teleport packet.</li>
 *   <li>All movement packets received while the barrier is open are
 *       suppressed by checks (they are pre-teleport data).</li>
 *   <li>When {@link TransactionManager} confirms the barrier ID,
 *       {@link #onTransactionConfirmed(short)} is called.</li>
 *   <li>The barrier closes and check processing resumes.</li>
 * </ol>
 *
 * <h2>Stale barrier protection</h2>
 * If the client never confirms within {@link #MAX_BARRIER_TICKS} ticks,
 * the barrier is force-closed to prevent permanent check suppression.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class TransactionBarrier {

    /** Maximum ticks a barrier can remain open before being force-closed. */
    public static final int MAX_BARRIER_TICKS = 40; // 2 seconds

    // ─── Barrier entry ────────────────────────────────────────────────────────

    /**
     * An open barrier waiting for a specific transaction confirmation.
     *
     * @param barrierTxId  the transaction ID that closes this barrier
     * @param openedTick   the server tick when the barrier was opened
     * @param expiryTick   the tick after which the barrier is force-closed
     */
    private record Barrier(short barrierTxId, long openedTick, long expiryTick) {}

    // ─── State ────────────────────────────────────────────────────────────────

    /** Queue of open barriers (usually 0 or 1; rarely more). */
    private final Deque<Barrier> barriers = new ArrayDeque<>(4);

    // ─── Open ─────────────────────────────────────────────────────────────────

    /**
     * Opens a new barrier that will close when {@code barrierTxId} is confirmed.
     *
     * @param barrierTxId the transaction ID to wait for
     * @param currentTick the current server tick
     */
    public void openBarrier(final short barrierTxId, final long currentTick) {
        barriers.addLast(new Barrier(barrierTxId, currentTick, currentTick + MAX_BARRIER_TICKS));
    }

    // ─── Confirm ──────────────────────────────────────────────────────────────

    /**
     * Notifies the barrier that a transaction has been confirmed by the client.
     *
     * <p>Removes all barriers whose ID matches or is older than {@code txId}.
     *
     * @param txId the confirmed transaction ID
     */
    public void onTransactionConfirmed(final short txId) {
        barriers.removeIf(b -> b.barrierTxId() == txId || isOlderOrEqual(b.barrierTxId(), txId));
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if any barrier is currently open (checks should
     * suppress themselves).
     *
     * <p>Also purges any barriers that have exceeded {@link #MAX_BARRIER_TICKS}.
     *
     * @param currentTick the current server tick
     * @return {@code true} if at least one barrier is open
     */
    public boolean isBlocked(final long currentTick) {
        // Purge expired barriers
        barriers.removeIf(b -> currentTick > b.expiryTick());
        return !barriers.isEmpty();
    }

    /**
     * Returns the number of currently open barriers.
     *
     * @return open barrier count
     */
    public int openCount() {
        return barriers.size();
    }

    /**
     * Clears all barriers unconditionally.
     * Call on player disconnect or full reset.
     */
    public void clear() {
        barriers.clear();
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if {@code a} is "older or equal" to {@code b}
     * in the short ID space (handles wrap-around).
     *
     * @param a candidate ID
     * @param b reference ID
     * @return {@code true} if a ≤ b in the circular short space
     */
    private static boolean isOlderOrEqual(final short a, final short b) {
        // Signed subtraction handles wrap-around correctly for short IDs
        return (short) (b - a) >= 0;
    }
}
