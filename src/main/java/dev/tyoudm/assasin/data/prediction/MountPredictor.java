/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.prediction;

import org.bukkit.entity.EntityType;

/**
 * Vanilla mount physics predictor.
 *
 * <p>Computes the expected maximum horizontal speed for a player riding
 * a specific entity type, accounting for terrain (land vs. water) and
 * the mount's vanilla physics parameters from {@link PhysicsConstants}.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *   double maxSpeed = MountPredictor.maxExpectedSpeedH(
 *       EntityType.HORSE, inWater, pingMs);
 *   if (observedSpeed > maxSpeed) {
 *       // flag MountSpeedA
 *   }
 * }</pre>
 *
 * <h2>1.21.11 specifics</h2>
 * <ul>
 *   <li>{@code ZOMBIE_HORSE} is now rideable (untamed) — same physics as
 *       {@code HORSE}.</li>
 *   <li>Nautilus (aquatic mount) has high water speed and very low land speed.</li>
 *   <li>Camel dash is not yet modelled — treated as a brief speed burst
 *       with a 3-tick tolerance window.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MountPredictor {

    // ─── Tolerance ────────────────────────────────────────────────────────────

    /**
     * Flat tolerance added to all mount speed thresholds to account for
     * floating-point variance and attribute modifiers.
     */
    public static final double BASE_TOLERANCE = 0.05;

    // ─── Predict ──────────────────────────────────────────────────────────────

    /**
     * Returns the maximum expected horizontal speed (blocks/tick) for a
     * player riding the given entity type.
     *
     * @param mountType the entity type being ridden
     * @param inWater   whether the mount is currently in water
     * @param pingMs    player ping in ms (adds a small tolerance)
     * @return maximum expected horizontal speed (blocks/tick)
     */
    public static double maxExpectedSpeedH(final EntityType mountType,
                                           final boolean inWater,
                                           final int pingMs) {
        final PhysicsConstants.MountPhysics physics = PhysicsConstants.getMountPhysics(mountType);
        final double base = inWater ? physics.maxSpeedWater() : physics.maxSpeedLand();
        final double pingTolerance = Math.min(0.05, pingMs / 1000.0 * 0.03);
        return base + BASE_TOLERANCE + pingTolerance;
    }

    /**
     * Returns the maximum expected vertical speed (blocks/tick) for a
     * jumping mount.
     *
     * @param mountType the entity type being ridden
     * @return maximum expected jump velocity (blocks/tick), or 0 if not jumpable
     */
    public static double maxExpectedJumpVelocity(final EntityType mountType) {
        final PhysicsConstants.MountPhysics physics = PhysicsConstants.getMountPhysics(mountType);
        return physics.jumpStrength() + BASE_TOLERANCE;
    }

    /**
     * Returns {@code true} if the given mount type can sustain flight.
     *
     * @param mountType the entity type
     * @return {@code true} if the mount can fly
     */
    public static boolean canFly(final EntityType mountType) {
        return PhysicsConstants.getMountPhysics(mountType).canFly();
    }

    /**
     * Returns {@code true} if the given mount type is primarily aquatic.
     *
     * @param mountType the entity type
     * @return {@code true} if aquatic
     */
    public static boolean isAquatic(final EntityType mountType) {
        return PhysicsConstants.getMountPhysics(mountType).isAquatic();
    }

    /**
     * Returns the {@link PhysicsConstants.MountPhysics} for the given type.
     *
     * @param mountType the entity type
     * @return mount physics record
     */
    public static PhysicsConstants.MountPhysics getPhysics(final EntityType mountType) {
        return PhysicsConstants.getMountPhysics(mountType);
    }

    // ─── Constructor ──────────────────────────────────────────────────────────

    /** Utility class — no instantiation. */
    private MountPredictor() {
        throw new UnsupportedOperationException("MountPredictor is a utility class.");
    }
}
