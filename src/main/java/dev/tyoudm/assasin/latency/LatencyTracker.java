/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.latency;

import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptManager;
import dev.tyoudm.assasin.exempt.ExemptType;

/**
 * Per-player latency tracker — the single entry point for all latency
 * subsystem operations.
 *
 * <p>Aggregates {@link TransactionManager}, {@link BucketedPingHistory},
 * {@link LagCompensatedWorld}, {@link KnockbackValidator}, and
 * {@link TransactionBarrier} into one cohesive object owned by
 * {@link PlayerData}.
 *
 * <h2>Tick integration</h2>
 * Call {@link #onTick(long)} once per server tick to:
 * <ul>
 *   <li>Record the current ping sample into {@link BucketedPingHistory}.</li>
 *   <li>Update the {@link ExemptManager} HIGH_PING exempt.</li>
 *   <li>Purge stale {@link TransactionBarrier} entries.</li>
 * </ul>
 *
 * <h2>Packet integration</h2>
 * <ul>
 *   <li>{@link #onPositionPacket(long, double, double, double)} — record
 *       position into {@link LagCompensatedWorld}.</li>
 *   <li>{@link #onTransactionSent(long)} — register outgoing transaction.</li>
 *   <li>{@link #onTransactionConfirmed(short, long)} — confirm echo from client.</li>
 *   <li>{@link #onVelocitySent(double, double, double, long)} — record
 *       outgoing knockback into {@link KnockbackValidator}.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class LatencyTracker {

    // ─── Sub-components ───────────────────────────────────────────────────────

    private final BucketedPingHistory  pingHistory;
    private final TransactionManager   transactionManager;
    private final LagCompensatedWorld  lagCompensatedWorld;
    private final KnockbackValidator   knockbackValidator;
    private final TransactionBarrier   transactionBarrier;

    /** Back-reference to the owning PlayerData (for ping and exempt access). */
    private final PlayerData playerData;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new {@code LatencyTracker} for the given player.
     *
     * @param playerData the owning player data
     */
    public LatencyTracker(final PlayerData playerData) {
        this.playerData          = playerData;
        this.pingHistory         = new BucketedPingHistory();
        this.transactionManager  = new TransactionManager(pingHistory);
        this.lagCompensatedWorld = new LagCompensatedWorld();
        this.knockbackValidator  = new KnockbackValidator();
        this.transactionBarrier  = new TransactionBarrier();
    }

    // ─── Tick integration ─────────────────────────────────────────────────────

    /**
     * Called once per server tick to update ping history, exemptions,
     * and purge stale barriers.
     *
     * @param currentTick the current server tick
     */
    public void onTick(final long currentTick) {
        final int pingMs = playerData.getPing();

        // Record current ping into history
        pingHistory.record(pingMs);

        // Update HIGH_PING exempt
        final ExemptManager exempt = playerData.getExemptManager();
        if (PingCompensator.isHighPing(pingMs)) {
            exempt.addPermanent(ExemptType.HIGH_PING);
        } else {
            exempt.clear(ExemptType.HIGH_PING);
        }

        // Purge stale barriers (isBlocked does this internally)
        transactionBarrier.isBlocked(currentTick);
    }

    // ─── Packet integration ───────────────────────────────────────────────────

    /**
     * Records the player's current position into the lag-compensated world.
     * Call from the movement packet handler.
     *
     * @param currentTick the current server tick
     * @param x           player X
     * @param y           player Y
     * @param z           player Z
     */
    public void onPositionPacket(final long currentTick,
                                 final double x, final double y, final double z) {
        lagCompensatedWorld.record(currentTick, x, y, z);
    }

    /**
     * Registers an outgoing transaction packet.
     * Call from the transaction/keep-alive packet handler (server → client).
     *
     * @param currentTick the current server tick
     * @return the transaction ID embedded in the outgoing packet
     */
    public short onTransactionSent(final long currentTick) {
        final short id = transactionManager.send(currentTick);
        playerData.setLastTransactionTick(currentTick);
        return id;
    }

    /**
     * Processes a transaction confirmation from the client.
     * Call from the transaction/keep-alive packet handler (client → server).
     *
     * @param id          the ID echoed by the client
     * @param currentTick the current server tick
     */
    public void onTransactionConfirmed(final short id, final long currentTick) {
        transactionManager.confirm(id, currentTick);
        transactionBarrier.onTransactionConfirmed(id);
        playerData.setLastTransactionConfirmTick(currentTick);
        // Sync the atomic ping field on PlayerData
        playerData.setPing(transactionManager.getLastRttMs());
    }

    /**
     * Records an outgoing knockback velocity packet.
     * Call from the velocity packet handler (server → client).
     *
     * @param velX        X velocity component (blocks/tick)
     * @param velY        Y velocity component (blocks/tick)
     * @param velZ        Z velocity component (blocks/tick)
     * @param currentTick the current server tick
     */
    public void onVelocitySent(final double velX, final double velY, final double velZ,
                               final long currentTick) {
        knockbackValidator.record(velX, velY, velZ, currentTick, playerData.getPing());
    }

    /**
     * Opens a transaction barrier for a setback teleport.
     * Call from the mitigation engine just before sending the teleport packet.
     *
     * @param barrierTxId the transaction ID to wait for
     * @param currentTick the current server tick
     */
    public void openSetbackBarrier(final short barrierTxId, final long currentTick) {
        transactionBarrier.openBarrier(barrierTxId, currentTick);
    }

    // ─── Reset ────────────────────────────────────────────────────────────────

    /**
     * Resets all latency state.
     * Call on player death, respawn, or teleport.
     */
    public void reset() {
        lagCompensatedWorld.clear();
        knockbackValidator.clear();
        transactionBarrier.clear();
        transactionManager.clear();
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /**
     * Returns the {@link BucketedPingHistory} for percentile queries.
     *
     * @return ping history
     */
    public BucketedPingHistory getPingHistory() {
        return pingHistory;
    }

    /**
     * Returns the {@link TransactionManager}.
     *
     * @return transaction manager
     */
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Returns the {@link LagCompensatedWorld} for target rewind.
     *
     * @return lag-compensated world
     */
    public LagCompensatedWorld getLagCompensatedWorld() {
        return lagCompensatedWorld;
    }

    /**
     * Returns the {@link KnockbackValidator}.
     *
     * @return knockback validator
     */
    public KnockbackValidator getKnockbackValidator() {
        return knockbackValidator;
    }

    /**
     * Returns the {@link TransactionBarrier}.
     *
     * @return transaction barrier
     */
    public TransactionBarrier getTransactionBarrier() {
        return transactionBarrier;
    }

    /**
     * Convenience: returns the P99 ping from {@link BucketedPingHistory}.
     *
     * @return P99 ping in ms
     */
    public int getPingP99() {
        return pingHistory.p99();
    }

    /**
     * Convenience: returns {@code true} if a setback barrier is currently open.
     *
     * @param currentTick the current server tick
     * @return {@code true} if checks should be suppressed
     */
    public boolean isSetbackBlocked(final long currentTick) {
        return transactionBarrier.isBlocked(currentTick);
    }
}
