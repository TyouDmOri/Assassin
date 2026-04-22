/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.prediction;

import dev.tyoudm.assasin.util.MathUtil;
import dev.tyoudm.assasin.util.RingBuffer;

/**
 * Per-player elytra physics predictor.
 *
 * <p>Simulates Mojang's elytra motion model tick-by-tick to produce an
 * expected velocity vector. {@code ElytraA} flags only when the observed
 * velocity deviates from the prediction for {@link PhysicsConstants#ELYTRA_FLAG_TICKS}
 * or more consecutive ticks.
 *
 * <h2>Critical rule</h2>
 * Elytra has <em>no terminal velocity</em>. A player diving from high altitude
 * can reach 40+ blocks/tick in ~8 seconds — this is vanilla behaviour and must
 * <strong>never</strong> be flagged. {@code ElytraA} only flags persistent
 * deviation from the predictor, not absolute speed.
 *
 * <h2>Mojang simplified formula (per tick)</h2>
 * <pre>
 *   lookVec = dirVector(pitch, yaw)
 *   hLookLen = sqrt(lookVec.x² + lookVec.z²)
 *
 *   // Gravity component
 *   motionY -= GRAVITY * (0.5 + 0.5 * clamp(1 - (-lookVec.y * 10), 0, 1))
 *
 *   // Fall bonus: if descending and has horizontal look
 *   if (motionY < 0 && hLookLen > 0):
 *       fallBonus = -motionY * 0.1 * hLookLen
 *       motionY  += fallBonus
 *       motionX  -= lookVec.x * fallBonus / hLookLen
 *       motionZ  -= lookVec.z * fallBonus / hLookLen
 *
 *   // Dive bonus: if looking down
 *   if (lookVec.y < 0 && hLookLen > 0):
 *       diveBonus = hLookLen * (-0.1) * lookVec.y
 *       motionX  += lookVec.x * diveBonus
 *       motionY  += diveBonus
 *       motionZ  += lookVec.z * diveBonus
 *
 *   // Redirect motion toward look direction
 *   speed = sqrt(motionX² + motionY² + motionZ²)
 *   motionX += (lookVec.x * speed - motionX) * 0.1
 *   motionY += (lookVec.y * speed - motionY) * 0.1
 *   motionZ += (lookVec.z * speed - motionZ) * 0.1
 *
 *   // Drag
 *   motionX *= DRAG_H (0.99)
 *   motionY *= DRAG_V (0.98)
 *   motionZ *= DRAG_H (0.99)
 * </pre>
 *
 * <h2>RESET conditions</h2>
 * The predictor state must be reset on: takeoff, landing, firework use,
 * wall collision. After reset, {@code ElytraA} suppresses itself for the
 * appropriate exempt duration before resuming comparison.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class ElytraPredictor {

    // ─── State ────────────────────────────────────────────────────────────────

    /** Predicted X velocity (blocks/tick). */
    private double motionX;
    /** Predicted Y velocity (blocks/tick). */
    private double motionY;
    /** Predicted Z velocity (blocks/tick). */
    private double motionZ;

    /** Whether the predictor has been seeded with at least one tick. */
    private boolean initialized;

    /** Number of ticks since the last reset. */
    private int ticksSinceReset;

    /** Ring buffer of the last 20 predicted horizontal speeds (for trend analysis). */
    private final RingBuffer.OfDouble speedHistory = new RingBuffer.OfDouble(20);

    // ─── Prediction result ────────────────────────────────────────────────────

    /**
     * The result of one tick of elytra prediction.
     *
     * @param motionX   predicted X velocity (blocks/tick)
     * @param motionY   predicted Y velocity (blocks/tick)
     * @param motionZ   predicted Z velocity (blocks/tick)
     * @param speedH    predicted horizontal speed (blocks/tick)
     * @param speedTotal predicted total speed (blocks/tick)
     */
    public record ElytraPrediction(
        double motionX,
        double motionY,
        double motionZ,
        double speedH,
        double speedTotal
    ) {}

    // ─── Seed ─────────────────────────────────────────────────────────────────

    /**
     * Seeds the predictor with the player's current observed velocity.
     * Call once when elytra is first deployed (after takeoff exempt expires).
     *
     * @param vx observed X velocity (blocks/tick)
     * @param vy observed Y velocity (blocks/tick)
     * @param vz observed Z velocity (blocks/tick)
     */
    public void seed(final double vx, final double vy, final double vz) {
        motionX     = vx;
        motionY     = vy;
        motionZ     = vz;
        initialized = true;
        ticksSinceReset = 0;
        speedHistory.clear();
    }

    // ─── Predict ──────────────────────────────────────────────────────────────

    /**
     * Advances the predictor by one tick and returns the expected velocity.
     *
     * <p>If the predictor has not been seeded, returns a zero-velocity result.
     *
     * @param yaw   player yaw in degrees
     * @param pitch player pitch in degrees
     * @return the predicted {@link ElytraPrediction} for this tick
     */
    public ElytraPrediction predict(final float yaw, final float pitch) {
        if (!initialized) {
            return new ElytraPrediction(0, 0, 0, 0, 0);
        }

        // ── Look vector ──────────────────────────────────────────────────────
        final double yawRad   = yaw   * MathUtil.DEG_TO_RAD;
        final double pitchRad = pitch * MathUtil.DEG_TO_RAD;
        final double cosP     = Math.cos(-pitchRad);
        final double lookX    = -Math.sin(yawRad) * cosP;
        final double lookY    =  Math.sin(-pitchRad);
        final double lookZ    =  Math.cos(yawRad)  * cosP;
        final double hLookLen = Math.sqrt(lookX * lookX + lookZ * lookZ);

        // ── Gravity ──────────────────────────────────────────────────────────
        final double gravFactor = 0.5 + 0.5 * MathUtil.clamp(1.0 - (-lookY * 10.0), 0.0, 1.0);
        motionY -= PhysicsConstants.ELYTRA_GRAVITY * gravFactor;

        // ── Fall bonus ───────────────────────────────────────────────────────
        if (motionY < 0.0 && hLookLen > 0.0) {
            final double fallBonus = -motionY * 0.1 * hLookLen;
            motionY += fallBonus;
            motionX -= lookX * fallBonus / hLookLen;
            motionZ -= lookZ * fallBonus / hLookLen;
        }

        // ── Dive bonus ───────────────────────────────────────────────────────
        if (lookY < 0.0 && hLookLen > 0.0) {
            final double diveBonus = hLookLen * (-0.1) * lookY;
            motionX += lookX * diveBonus;
            motionY += diveBonus;
            motionZ += lookZ * diveBonus;
        }

        // ── Redirect toward look direction ───────────────────────────────────
        final double speed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        if (speed > 0.0) {
            motionX += (lookX * speed - motionX) * 0.1;
            motionY += (lookY * speed - motionY) * 0.1;
            motionZ += (lookZ * speed - motionZ) * 0.1;
        }

        // ── Drag ─────────────────────────────────────────────────────────────
        motionX *= PhysicsConstants.ELYTRA_DRAG_H;
        motionY *= PhysicsConstants.ELYTRA_DRAG_V;
        motionZ *= PhysicsConstants.ELYTRA_DRAG_H;

        ticksSinceReset++;

        final double speedH = Math.sqrt(motionX * motionX + motionZ * motionZ);
        final double speedT = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        speedHistory.add(speedH);

        return new ElytraPrediction(motionX, motionY, motionZ, speedH, speedT);
    }

    // ─── Firework boost ───────────────────────────────────────────────────────

    /**
     * Applies a firework boost to the current predicted motion.
     * Call when the server detects a firework use while gliding.
     *
     * @param yaw   player yaw in degrees (boost direction)
     * @param pitch player pitch in degrees
     */
    public void applyFireworkBoost(final float yaw, final float pitch) {
        final double yawRad   = yaw   * MathUtil.DEG_TO_RAD;
        final double pitchRad = pitch * MathUtil.DEG_TO_RAD;
        final double cosP     = Math.cos(-pitchRad);
        final double lookX    = -Math.sin(yawRad) * cosP;
        final double lookY    =  Math.sin(-pitchRad);
        final double lookZ    =  Math.cos(yawRad)  * cosP;

        motionX += lookX * PhysicsConstants.ELYTRA_FIREWORK_BOOST;
        motionY += lookY * PhysicsConstants.ELYTRA_FIREWORK_BOOST;
        motionZ += lookZ * PhysicsConstants.ELYTRA_FIREWORK_BOOST;
    }

    // ─── Deviation check ──────────────────────────────────────────────────────

    /**
     * Returns the deviation between the observed velocity and the last
     * prediction as a total speed difference (blocks/tick).
     *
     * @param observedVx observed X velocity
     * @param observedVy observed Y velocity
     * @param observedVz observed Z velocity
     * @return absolute speed deviation (blocks/tick)
     */
    public double computeDeviation(final double observedVx,
                                   final double observedVy,
                                   final double observedVz) {
        final double dx = observedVx - motionX;
        final double dy = observedVy - motionY;
        final double dz = observedVz - motionZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // ─── Reset ────────────────────────────────────────────────────────────────

    /**
     * Resets the predictor state.
     * Must be called on: takeoff, landing, firework use, wall collision.
     */
    public void reset() {
        motionX         = 0.0;
        motionY         = 0.0;
        motionZ         = 0.0;
        initialized     = false;
        ticksSinceReset = 0;
        speedHistory.clear();
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /** Returns the current predicted X velocity. */
    public double getMotionX()          { return motionX; }
    /** Returns the current predicted Y velocity. */
    public double getMotionY()          { return motionY; }
    /** Returns the current predicted Z velocity. */
    public double getMotionZ()          { return motionZ; }
    /** Returns {@code true} if the predictor has been seeded. */
    public boolean isInitialized()      { return initialized; }
    /** Returns the number of ticks since the last reset. */
    public int getTicksSinceReset()     { return ticksSinceReset; }
    /** Returns the speed history ring buffer. */
    public RingBuffer.OfDouble getSpeedHistory() { return speedHistory; }
}
