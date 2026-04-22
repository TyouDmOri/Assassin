/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.prediction;

import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.World;

/**
 * Tick-accurate vanilla movement predictor for a single player.
 *
 * <p>Simulates one tick of Minecraft's player physics to produce an
 * <em>expected</em> position and velocity. Checks compare the client's
 * reported position against this prediction to detect anomalies.
 *
 * <h2>Simulation order (mirrors Minecraft source)</h2>
 * <ol>
 *   <li>Apply input acceleration (sprint/walk/sneak) to horizontal motion.</li>
 *   <li>Apply gravity to vertical motion.</li>
 *   <li>Apply drag to all axes.</li>
 *   <li>Apply block friction to horizontal motion (if on ground).</li>
 *   <li>Clamp vertical motion to terminal velocity.</li>
 * </ol>
 *
 * <h2>Limitations</h2>
 * <ul>
 *   <li>Does not simulate NMS collision resolution — uses
 *       {@link CollisionEngine#isOnGround} as a proxy.</li>
 *   <li>Does not account for status effects (Speed, Slowness, etc.) —
 *       checks add a tolerance margin for these.</li>
 *   <li>Elytra physics are handled by {@link ElytraPredictor}.</li>
 *   <li>Mount physics are handled by {@link MountPredictor}.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MovementPredictor {

    // ─── Prediction result ────────────────────────────────────────────────────

    /**
     * The result of one tick of movement prediction.
     *
     * @param motionX  predicted X velocity (blocks/tick)
     * @param motionY  predicted Y velocity (blocks/tick)
     * @param motionZ  predicted Z velocity (blocks/tick)
     * @param speedH   predicted horizontal speed (blocks/tick)
     * @param onGround predicted onGround state
     */
    public record PredictionResult(
        double  motionX,
        double  motionY,
        double  motionZ,
        double  speedH,
        boolean onGround
    ) {}

    // ─── Predict ──────────────────────────────────────────────────────────────

    /**
     * Predicts the player's motion for the next tick given their current state.
     *
     * @param data      the player's current data (position, velocity, flags)
     * @param world     the world (for ground/friction queries)
     * @param sprinting whether the player is sprinting this tick
     * @param sneaking  whether the player is sneaking this tick
     * @param jumping   whether the player pressed jump this tick
     * @return the predicted {@link PredictionResult}
     */
    public static PredictionResult predict(final PlayerData data,
                                           final World world,
                                           final boolean sprinting,
                                           final boolean sneaking,
                                           final boolean jumping) {
        final double x = data.getX();
        final double y = data.getY();
        final double z = data.getZ();

        double motionX = data.getVelocityH() == 0 ? 0 : data.getX() - data.getLastX();
        double motionY = data.getVelocityY();
        double motionZ = data.getVelocityH() == 0 ? 0 : data.getZ() - data.getLastZ();

        final boolean onGround = data.isOnGround();

        // ── 1. Input acceleration ────────────────────────────────────────────
        final double baseSpeed = computeBaseSpeed(sprinting, sneaking);
        // We don't know exact input direction, so we use the magnitude only.
        // Checks that need direction use the actual delta instead.
        final double inputAccel = baseSpeed * (onGround
            ? PhysicsConstants.DRAG_H_GROUND
            : 0.02); // air acceleration factor

        // ── 2. Gravity ───────────────────────────────────────────────────────
        if (!onGround) {
            motionY -= PhysicsConstants.GRAVITY;
        }

        // ── 3. Jump ──────────────────────────────────────────────────────────
        if (jumping && onGround) {
            motionY = PhysicsConstants.JUMP_VELOCITY;
            if (sprinting) {
                // Sprint-jump boost: add to horizontal in current facing direction
                // (direction unknown here — predictor adds magnitude only)
                final double boost = PhysicsConstants.JUMP_SPRINT_BOOST;
                final double hLen  = Math.sqrt(motionX * motionX + motionZ * motionZ);
                if (hLen > 1e-10) {
                    motionX += motionX / hLen * boost;
                    motionZ += motionZ / hLen * boost;
                }
            }
        }

        // ── 4. Drag ──────────────────────────────────────────────────────────
        motionY *= PhysicsConstants.DRAG_AIR;

        // ── 5. Ground friction ───────────────────────────────────────────────
        final boolean nextOnGround = CollisionEngine.isOnGround(world, x + motionX, y + motionY, z + motionZ);
        if (nextOnGround) {
            final double friction = CollisionEngine.getGroundFriction(world, x, y, z);
            motionX *= friction;
            motionZ *= friction;
        } else {
            motionX *= PhysicsConstants.DRAG_H_AIR;
            motionZ *= PhysicsConstants.DRAG_H_AIR;
        }

        // ── 6. Max horizontal speed cap ──────────────────────────────────────
        final double maxH = sprinting
            ? PhysicsConstants.SPEED_SPRINT + inputAccel + 0.05
            : PhysicsConstants.SPEED_WALK   + inputAccel + 0.05;

        final double speedH = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (speedH > maxH) {
            final double scale = maxH / speedH;
            motionX *= scale;
            motionZ *= scale;
        }

        return new PredictionResult(motionX, motionY, motionZ,
            Math.sqrt(motionX * motionX + motionZ * motionZ), nextOnGround);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    /**
     * Returns the base movement speed for the given input state.
     *
     * @param sprinting whether the player is sprinting
     * @param sneaking  whether the player is sneaking
     * @return base speed in blocks/tick
     */
    public static double computeBaseSpeed(final boolean sprinting, final boolean sneaking) {
        if (sprinting) return PhysicsConstants.SPEED_SPRINT;
        if (sneaking)  return PhysicsConstants.SPEED_WALK * PhysicsConstants.SPEED_SNEAK_MULT;
        return PhysicsConstants.SPEED_WALK;
    }

    /**
     * Returns the maximum expected horizontal speed for the given state,
     * including a small tolerance for floating-point and attribute variance.
     *
     * @param sprinting whether the player is sprinting
     * @param sneaking  whether the player is sneaking
     * @param pingMs    player ping in ms (adds positional tolerance)
     * @return maximum expected horizontal speed (blocks/tick)
     */
    public static double maxExpectedSpeedH(final boolean sprinting, final boolean sneaking,
                                           final int pingMs) {
        final double base = computeBaseSpeed(sprinting, sneaking);
        // Ping tolerance: ~0.5 block/s per 100ms ping
        final double pingTolerance = Math.min(0.1, pingMs / 1000.0 * 0.05);
        return base + 0.03 + pingTolerance; // 0.03 = floating-point + attribute margin
    }

    // ─── Constructor ──────────────────────────────────────────────────────────

    /** Utility class — no instantiation. */
    private MovementPredictor() {
        throw new UnsupportedOperationException("MovementPredictor is a utility class.");
    }
}
