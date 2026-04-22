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
 * Cancels the triggering block action packet (world checks).
 *
 * <p>Used by ScaffoldA/B/C, FastBreakA, NukerA, AirPlaceA to prevent
 * illegal block placements or breaks from being processed.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CancelBlockActionStrategy implements MitigationStrategy {

    public static final CancelBlockActionStrategy INSTANCE = new CancelBlockActionStrategy();

    @Override
    public MitigationResult execute(final MitigationContext ctx) {
        if (!ctx.hasPacketEvent()) {
            return MitigationResult.noop();
        }
        ctx.cancelPacket();
        return MitigationResult.cancel();
    }

    @Override public MitigationPriority priority() { return MitigationPriority.IMMEDIATE; }
    @Override public String name() { return "CancelBlockAction"; }
}
