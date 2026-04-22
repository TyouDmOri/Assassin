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
 * Per-player transaction (keep-alive) manager.
 *
 * <p>Tracks outgoing transaction IDs and their send timestamps so that
 * round-trip time (RTT) can be measured precisely when the client echoes
 * the ID back. Uses a FIFO {@link ArrayDeque} of pending entries.
 *
 * <h2>Protocol</h2>
 * <ol>
 *   <li>Server sends {@code PING} (or {@code TRANSACTION}) packet with a
 *       short ID every {@code transactionIntervalTicks} ticks.</li>
 *   <li>Client echoes the ID in a {@code PONG} (or {@code TRANSACTION})
 *       packet.</li>
 *   <li>{@link #confirm(short)} matches the echo, computes RTT, and
 *       notifies {@link BucketedPingHistory}.</li>
 * </ol>
 *
 * <h2>Thread safety</h2>
 * All methods are called from the netty pipeline thread for this player.
 * No synchronization is needed.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class TransactionManager {

    /** Maximum number of unconfirmed transactions before the queue is trimmed. */
    private static final int MAX_PENDING = 20;

    // ─── Pending entry ────────────────────────────────────────────────────────

    /**
     * An unconfirmed outgoing transaction.
     *
     * @param id        the transaction ID sent to the client
     * @param sentMs    {@link System#currentTimeMillis()} when the packet was sent
     * @param sentTick  server tick when the packet was sent
     */
    public record PendingTransaction(short id, long sentMs, long sentTick) {}

    // ─── State ────────────────────────────────────────────────────────────────

    /** FIFO queue of unconfirmed transactions. */
    private final Deque<PendingTransaction> pending = new ArrayDeque<>(MAX_PENDING);

    /** Monotonically incrementing ID counter (wraps at Short.MAX_VALUE). */
    private short nextId = Short.MIN_VALUE;

    /** Ping history updated on each confirmed transaction. */
    private final BucketedPingHistory pingHistory;

    /** Most recently measured RTT in milliseconds. */
    private int lastRttMs = 0;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new {@code TransactionManager} backed by the given ping history.
     *
     * @param pingHistory the ping history to update on each confirmation
     */
    public TransactionManager(final BucketedPingHistory pingHistory) {
        this.pingHistory = pingHistory;
    }

    // ─── Send ─────────────────────────────────────────────────────────────────

    /**
     * Records a new outgoing transaction and returns its ID.
     *
     * <p>The caller is responsible for actually sending the packet with this ID.
     * If the pending queue is full, the oldest unconfirmed entry is discarded.
     *
     * @param currentTick the current server tick
     * @return the transaction ID to embed in the outgoing packet
     */
    public short send(final long currentTick) {
        final short id = nextId++;
        if (pending.size() >= MAX_PENDING) {
            pending.pollFirst(); // discard oldest if client is very slow
        }
        pending.addLast(new PendingTransaction(id, System.currentTimeMillis(), currentTick));
        return id;
    }

    // ─── Confirm ──────────────────────────────────────────────────────────────

    /**
     * Processes a transaction confirmation from the client.
     *
     * <p>Searches the pending queue for the matching ID. All entries older
     * than the matched one are also removed (they were implicitly confirmed
     * or lost). Updates {@link BucketedPingHistory} with the measured RTT.
     *
     * @param id          the ID echoed by the client
     * @param currentTick the current server tick
     * @return {@code true} if the ID was found and confirmed
     */
    public boolean confirm(final short id, final long currentTick) {
        final long nowMs = System.currentTimeMillis();

        // Walk the queue from oldest to newest
        while (!pending.isEmpty()) {
            final PendingTransaction tx = pending.pollFirst();
            if (tx.id() == id) {
                lastRttMs = (int) (nowMs - tx.sentMs());
                pingHistory.record(lastRttMs);
                return true;
            }
            // IDs older than the confirmed one are silently dropped
        }
        return false;
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    /**
     * Returns the most recently measured RTT in milliseconds.
     *
     * @return last RTT in ms (0 if no transaction has been confirmed yet)
     */
    public int getLastRttMs() {
        return lastRttMs;
    }

    /**
     * Returns the number of unconfirmed transactions currently pending.
     *
     * @return pending count
     */
    public int pendingCount() {
        return pending.size();
    }

    /**
     * Returns {@code true} if there are no unconfirmed transactions.
     *
     * @return {@code true} if the pending queue is empty
     */
    public boolean isIdle() {
        return pending.isEmpty();
    }

    /**
     * Clears all pending transactions.
     * Call on player disconnect or full reset.
     */
    public void clear() {
        pending.clear();
    }
}
