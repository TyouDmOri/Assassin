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
import org.bukkit.util.Vector;

/**
 * Corrects the player's velocity by sending a server-authoritative
 * {@code SET_ENTITY_VELOCITY} packet.
 *
 * <p>Used when a player is moving too fast but a hard setback would be
 * too disruptive (e.g., minor speed violations). The corrected velocity
 * is clamped to the expected maximum.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class VelocityStrategy implements MitigationStrategy {

    /** The corrected velocity to apply. */
    private final Vector velocity;

    /**
     * Creates a velocity correction strategy.
     *
     * @param velocity the corrected velocity vector (blocks/tick)
     */
    public VelocityStrategy(final Vector velocity) {
        this.velocity = velocity.clone();
    }

    /** Zero-velocity correction (full stop). */
    public static VelocityStrategy zero() {
        return new VelocityStrategy(new Vector(0, 0, 0));
    }

    /** Corrects to the given speed. */
    public static VelocityStrategy of(final double x, final double y, final double z) {
        return new VelocityStrategy(new Vector(x, y, z));
    }

    @Override
    public MitigationResult execute(final MitigationContext ctx) {
        try {
            ctx.player().setVelocity(velocity);
            return MitigationResult.ok(String.format(
                "Velocity corrected to (%.3f, %.3f, %.3f)",
                velocity.getX(), velocity.getY(), velocity.getZ()));
        } catch (final Exception ex) {
            return MitigationResult.failure("VelocityStrategy failed: " + ex.getMessage());
        }
    }

    @Override public MitigationPriority priority() { return MitigationPriority.NORMAL; }
    @Override public String name() { return "VelocityCorrection"; }
}
