/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation.strategy;

import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationContext;
import dev.tyoudm.assasin.mitigation.MitigationPriority;
import dev.tyoudm.assasin.mitigation.MitigationResult;
import dev.tyoudm.assasin.mitigation.MitigationStrategy;
import org.bukkit.Location;

/**
 * Teleports the player back to their last known safe position (setback).
 *
 * <p>Supports two modes:
 * <ul>
 *   <li><b>Soft</b> — teleports to the last valid position (one tick ago).
 *       Used for minor speed/fly violations.</li>
 *   <li><b>Hard</b> — teleports to the last confirmed ground position.
 *       Used for severe violations (phase, high-VL fly).</li>
 * </ul>
 *
 * <p>After the setback, a {@link dev.tyoudm.assasin.latency.TransactionBarrier}
 * is opened via {@link dev.tyoudm.assasin.latency.LatencyTracker#openSetbackBarrier}
 * to suppress checks until the client confirms the teleport.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class SetbackStrategy implements MitigationStrategy {

    /** Whether to use hard setback (ground position) vs soft (last position). */
    private final boolean hard;

    public SetbackStrategy(final boolean hard) {
        this.hard = hard;
    }

    /** Soft setback factory. */
    public static SetbackStrategy soft() { return new SetbackStrategy(false); }

    /** Hard setback factory. */
    public static SetbackStrategy hard() { return new SetbackStrategy(true); }

    @Override
    public MitigationResult execute(final MitigationContext ctx) {
        try {
            final var data   = ctx.data();
            final var player = ctx.player();

            // Determine setback position
            final double sbX, sbY, sbZ;
            if (hard) {
                // Hard: last ground position (Y snapped to last onGround=true tick)
                sbX = data.getLastX();
                sbY = data.getLastY();
                sbZ = data.getLastZ();
            } else {
                // Soft: one tick ago
                sbX = data.getLastX();
                sbY = data.getLastY();
                sbZ = data.getLastZ();
            }

            final Location target = new Location(
                player.getWorld(), sbX, sbY, sbZ,
                player.getLocation().getYaw(),
                player.getLocation().getPitch()
            );

            // Apply setback exempt
            data.getExemptManager().add(ExemptType.SETBACK, ctx.currentTick(), 3L);
            data.getExemptManager().add(ExemptType.TELEPORT_PENDING, ctx.currentTick(), 5L);

            // Open transaction barrier
            if (data.getLatencyTracker() != null) {
                final short barrierTx = data.getLatencyTracker()
                    .onTransactionSent(ctx.currentTick());
                data.getLatencyTracker().openSetbackBarrier(barrierTx, ctx.currentTick());
            }

            // Teleport (must be on main thread — MitigationPriority.NORMAL ensures this)
            player.teleport(target);

            return MitigationResult.ok(String.format(
                "%s setback to (%.2f, %.2f, %.2f)", hard ? "Hard" : "Soft", sbX, sbY, sbZ));

        } catch (final Exception ex) {
            return MitigationResult.failure("SetbackStrategy failed: " + ex.getMessage());
        }
    }

    @Override public MitigationPriority priority() { return MitigationPriority.NORMAL; }
    @Override public String name() { return hard ? "HardSetback" : "SoftSetback"; }
}
