/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.util;

/**
 * Mathematical utility constants and methods used across all checks.
 *
 * <p>All constants are precomputed at class-load time to avoid repeated
 * computation in hot paths. Methods are static and branch-prediction-friendly
 * (common case first).
 *
 * <h2>Design rules</h2>
 * <ul>
 *   <li>No autoboxing — all parameters and return types are primitives.</li>
 *   <li>Use {@link Math} intrinsics where possible (JIT-optimized).</li>
 *   <li>Keep methods short; complex logic belongs in dedicated utility classes.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MathUtil {

    // ─── Precomputed constants ────────────────────────────────────────────────

    /** π (pi). */
    public static final double PI          = Math.PI;

    /** 2π — full circle in radians. */
    public static final double TWO_PI      = 2.0 * Math.PI;

    /** π/2 — quarter circle in radians. */
    public static final double HALF_PI     = Math.PI / 2.0;

    /** Degrees-to-radians conversion factor. */
    public static final double DEG_TO_RAD  = Math.PI / 180.0;

    /** Radians-to-degrees conversion factor. */
    public static final double RAD_TO_DEG  = 180.0 / Math.PI;

    /** Vanilla gravity constant (blocks per tick²). */
    public static final double GRAVITY     = 0.08;

    /** Vanilla air drag applied to vertical motion each tick. */
    public static final double DRAG_AIR    = 0.98;

    /** Vanilla ground friction multiplier. */
    public static final double FRICTION_GROUND = 0.6;

    /** Vanilla base walk speed (blocks per tick). */
    public static final double BASE_WALK_SPEED = 0.221;

    /** Vanilla base sprint speed (blocks per tick). */
    public static final double BASE_SPRINT_SPEED = 0.2806;

    /** Vanilla jump velocity (blocks per tick). */
    public static final double JUMP_VELOCITY = 0.42;

    /** Squared reach distance for melee (vanilla: 3.0 blocks → 9.0 sq). */
    public static final double REACH_SQ    = 9.0;

    /** Epsilon for floating-point comparisons. */
    public static final double EPSILON     = 1.0E-6;

    // ─── Clamp ────────────────────────────────────────────────────────────────

    /**
     * Clamps {@code value} to [{@code min}, {@code max}].
     *
     * @param value the value to clamp
     * @param min   lower bound (inclusive)
     * @param max   upper bound (inclusive)
     * @return clamped value
     */
    public static double clamp(final double value, final double min, final double max) {
        return value < min ? min : (value > max ? max : value);
    }

    /**
     * Clamps {@code value} to [{@code min}, {@code max}].
     *
     * @param value the value to clamp
     * @param min   lower bound (inclusive)
     * @param max   upper bound (inclusive)
     * @return clamped value
     */
    public static int clamp(final int value, final int min, final int max) {
        return value < min ? min : (value > max ? max : value);
    }

    // ─── Angle utilities ──────────────────────────────────────────────────────

    /**
     * Wraps an angle (in degrees) to the range [-180, 180).
     *
     * @param angle angle in degrees
     * @return wrapped angle
     */
    public static double wrapDegrees(double angle) {
        angle %= 360.0;
        if (angle >= 180.0)  angle -= 360.0;
        if (angle < -180.0)  angle += 360.0;
        return angle;
    }

    /**
     * Returns the absolute angular difference between two yaw/pitch values
     * (in degrees), always in [0, 180].
     *
     * @param a first angle in degrees
     * @param b second angle in degrees
     * @return angular difference in [0, 180]
     */
    public static double angleDiff(final double a, final double b) {
        double diff = Math.abs(wrapDegrees(a - b));
        return diff > 180.0 ? 360.0 - diff : diff;
    }

    // ─── Distance ─────────────────────────────────────────────────────────────

    /**
     * Returns the squared 3D distance between two points.
     * Avoids {@link Math#sqrt} — use for threshold comparisons.
     *
     * @param x1 x of point 1
     * @param y1 y of point 1
     * @param z1 z of point 1
     * @param x2 x of point 2
     * @param y2 y of point 2
     * @param z2 z of point 2
     * @return squared distance
     */
    public static double distanceSq(
            final double x1, final double y1, final double z1,
            final double x2, final double y2, final double z2) {
        final double dx = x1 - x2;
        final double dy = y1 - y2;
        final double dz = z1 - z2;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Returns the squared 2D (horizontal) distance between two points.
     *
     * @param x1 x of point 1
     * @param z1 z of point 1
     * @param x2 x of point 2
     * @param z2 z of point 2
     * @return squared horizontal distance
     */
    public static double distanceSqHorizontal(
            final double x1, final double z1,
            final double x2, final double z2) {
        final double dx = x1 - x2;
        final double dz = z1 - z2;
        return dx * dx + dz * dz;
    }

    // ─── GCD ──────────────────────────────────────────────────────────────────

    /**
     * Euclidean GCD for two non-negative longs.
     * Used by AimA to detect mouse sensitivity patterns.
     *
     * @param a first value (≥ 0)
     * @param b second value (≥ 0)
     * @return GCD of {@code a} and {@code b}
     */
    public static long gcd(long a, long b) {
        while (b != 0L) {
            final long t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    // ─── Lerp ─────────────────────────────────────────────────────────────────

    /**
     * Linear interpolation between {@code a} and {@code b} by factor {@code t}.
     *
     * @param a start value
     * @param b end value
     * @param t interpolation factor in [0, 1]
     * @return interpolated value
     */
    public static double lerp(final double a, final double b, final double t) {
        return a + (b - a) * t;
    }

    // ─── Misc ─────────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if {@code value} is finite (not NaN, not Infinity).
     *
     * @param value the value to check
     * @return {@code true} if finite
     */
    public static boolean isFinite(final double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /**
     * Returns {@code true} if {@code value} is within {@code epsilon} of zero.
     *
     * @param value   the value to check
     * @param epsilon tolerance
     * @return {@code true} if near zero
     */
    public static boolean nearZero(final double value, final double epsilon) {
        return Math.abs(value) < epsilon;
    }

    // ─── Constructor ──────────────────────────────────────────────────────────

    /** Utility class — no instantiation. */
    private MathUtil() {
        throw new UnsupportedOperationException("MathUtil is a utility class.");
    }
}
