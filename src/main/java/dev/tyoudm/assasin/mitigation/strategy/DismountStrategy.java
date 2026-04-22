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

/**
 * Forces the player to dismount their current vehicle.
 *
 * <p>Used by mount checks (MountSpeedA, MountFlyA) when the player
 * is exploiting their mount. Clears the {@link ExemptType#VEHICLE} exempt
 * after dismounting.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class DismountStrategy implements MitigationStrategy {

    public static final DismountStrategy INSTANCE = new DismountStrategy();

    @Override
    public MitigationResult execute(final MitigationContext ctx) {
        try {
            final var player = ctx.player();
            if (player.getVehicle() == null) {
                return MitigationResult.noop();
            }
            player.leaveVehicle();
            ctx.data().setInVehicle(false);
            ctx.data().getExemptManager().clear(ExemptType.VEHICLE);
            return MitigationResult.ok("Player dismounted");
        } catch (final Exception ex) {
            return MitigationResult.failure("DismountStrategy failed: " + ex.getMessage());
        }
    }

    @Override public MitigationPriority priority() { return MitigationPriority.NORMAL; }
    @Override public String name() { return "Dismount"; }
}
