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
 * Cancels the triggering damage packet (combat checks).
 *
 * <p>Marks the packet as cancelled so the server does not apply the
 * damage to the target. Used by KillauraA-D, ReachA/B, CriticalsA.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CancelDamageStrategy implements MitigationStrategy {

    public static final CancelDamageStrategy INSTANCE = new CancelDamageStrategy();

    @Override
    public MitigationResult execute(final MitigationContext ctx) {
        if (!ctx.hasPacketEvent()) {
            return MitigationResult.noop();
        }
        ctx.cancelPacket();
        return MitigationResult.cancel();
    }

    @Override public MitigationPriority priority() { return MitigationPriority.IMMEDIATE; }
    @Override public String name() { return "CancelDamage"; }
}
