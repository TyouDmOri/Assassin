/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation.strategy;

import dev.tyoudm.assasin.mitigation.MitigationContext;
import dev.tyoudm.assasin.mitigation.MitigationPriority;
import dev.tyoudm.assasin.mitigation.MitigationResult;
import dev.tyoudm.assasin.mitigation.MitigationStrategy;

/**
 * Cancels the triggering packet, preventing it from being processed.
 *
 * <p>Only effective when the {@link MitigationContext} contains a non-null
 * {@link com.github.retrooper.packetevents.event.PacketReceiveEvent}.
 * If no packet event is available, this strategy is a no-op.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CancelPacketStrategy implements MitigationStrategy {

    public static final CancelPacketStrategy INSTANCE = new CancelPacketStrategy();

    @Override
    public MitigationResult execute(final MitigationContext ctx) {
        if (!ctx.hasPacketEvent()) {
            return MitigationResult.noop();
        }
        ctx.cancelPacket();
        return MitigationResult.cancel();
    }

    @Override public MitigationPriority priority() { return MitigationPriority.IMMEDIATE; }
    @Override public String name() { return "CancelPacket"; }
}
