/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.prediction;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Lightweight AABB collision engine for server-side movement prediction.
 *
 * <p>Provides two complementary approaches:
 * <ol>
 *   <li><b>AABB slab test</b> — fast O(1) check whether a player AABB
 *       overlaps a solid block at a given position.</li>
 *   <li><b>Voxel traversal (DDA)</b> — Amanatides-Woo ray-march used by
 *       {@code KillauraC} (wall-attack detection) and {@code ScaffoldA/B}
 *       (raytrace placement validation). Iterates at most ~12 voxels.</li>
 * </ol>
 *
 * <h2>Player AABB</h2>
 * <pre>
 *   width  = 0.6  (±0.3 on X and Z)
 *   height = 1.8  (standing), 1.5 (sneaking)
 * </pre>
 *
 * <h2>Thread safety</h2>
 * All methods are stateless and safe to call from any thread, provided
 * the {@link World} reference is accessed safely (Bukkit world reads are
 * generally safe from async threads for block data).
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CollisionEngine {

    // ─── Player AABB dimensions ───────────────────────────────────────────────

    /** Half-width of the player AABB on X and Z axes. */
    public static final double HALF_WIDTH  = 0.3;

    /** Standing player height. */
    public static final double HEIGHT_STAND = 1.8;

    /** Sneaking player height. */
    public static final double HEIGHT_SNEAK = 1.5;

    // ─── AABB slab test ───────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the player AABB at {@code (x, y, z)} overlaps
     * any solid block in the given world.
     *
     * <p>Checks the 4 bottom corners and 4 top corners of the AABB.
     * This is a conservative approximation — sufficient for movement checks.
     *
     * @param world    the world to check in
     * @param x        player foot X
     * @param y        player foot Y (bottom of AABB)
     * @param z        player foot Z
     * @param sneaking whether the player is sneaking (affects height)
     * @return {@code true} if any corner overlaps a solid block
     */
    public static boolean overlapsBlock(final World world,
                                        final double x, final double y, final double z,
                                        final boolean sneaking) {
        final double height = sneaking ? HEIGHT_SNEAK : HEIGHT_STAND;
        final double minX = x - HALF_WIDTH;
        final double maxX = x + HALF_WIDTH;
        final double minZ = z - HALF_WIDTH;
        final double maxZ = z + HALF_WIDTH;

        // Check bottom and top layers
        for (final double checkY : new double[]{y, y + height - 0.01}) {
            if (isSolid(world, minX, checkY, minZ)) return true;
            if (isSolid(world, maxX, checkY, minZ)) return true;
            if (isSolid(world, minX, checkY, maxZ)) return true;
            if (isSolid(world, maxX, checkY, maxZ)) return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the block at the given world coordinates is solid
     * AND the point (x, y, z) is actually inside its bounding box.
     *
     * <p>This correctly handles partial blocks (slabs, stairs, fences, etc.)
     * by checking the real block bounding box instead of treating the full
     * 1×1×1 voxel as solid.
     *
     * @param world the world
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @return {@code true} if the point is inside a solid block's bounding box
     */
    public static boolean isSolid(final World world,
                                  final double x, final double y, final double z) {
        final int bx = (int) Math.floor(x);
        final int by = (int) Math.floor(y);
        final int bz = (int) Math.floor(z);
        final Block block = world.getBlockAt(bx, by, bz);
        if (!block.getType().isSolid()) return false;

        // Use the real bounding box to handle partial blocks (slabs, stairs, etc.)
        final var bb = block.getBoundingBox();
        if (bb.getVolume() == 0) return false;

        // Bounding box is relative to block origin — convert point to block-local coords
        final double lx = x - bx;
        final double ly = y - by;
        final double lz = z - bz;

        // Small epsilon to avoid flagging players standing exactly on the surface
        final double eps = 0.001;
        return lx >= bb.getMinX() - eps && lx <= bb.getMaxX() + eps
            && ly >  bb.getMinY() + eps && ly <  bb.getMaxY() - eps
            && lz >= bb.getMinZ() - eps && lz <= bb.getMaxZ() + eps;
    }

    /**
     * Returns {@code true} if the player at {@code (x, y, z)} is standing on
     * a solid block (i.e., the block directly below the feet is solid).
     *
     * @param world the world
     * @param x     player foot X
     * @param y     player foot Y
     * @param z     player foot Z
     * @return {@code true} if on ground
     */
    public static boolean isOnGround(final World world,
                                     final double x, final double y, final double z) {
        // Check slightly below the feet
        final double checkY = y - 0.001;
        final double minX   = x - HALF_WIDTH;
        final double maxX   = x + HALF_WIDTH;
        final double minZ   = z - HALF_WIDTH;
        final double maxZ   = z + HALF_WIDTH;

        return isSolid(world, minX, checkY, minZ)
            || isSolid(world, maxX, checkY, minZ)
            || isSolid(world, minX, checkY, maxZ)
            || isSolid(world, maxX, checkY, maxZ);
    }

    /**
     * Returns the friction of the block the player is standing on.
     * Used by {@link MovementPredictor} to compute expected horizontal speed.
     *
     * @param world the world
     * @param x     player foot X
     * @param y     player foot Y
     * @param z     player foot Z
     * @return block friction in [0, 1]
     */
    public static double getGroundFriction(final World world,
                                           final double x, final double y, final double z) {
        final Block below = world.getBlockAt(
            (int) Math.floor(x),
            (int) Math.floor(y - 0.5),
            (int) Math.floor(z)
        );
        return getBlockFriction(below.getType());
    }

    // ─── Voxel traversal (Amanatides-Woo DDA) ────────────────────────────────

    /**
     * Result of a voxel ray-march.
     *
     * @param hit      whether the ray hit a solid block before reaching {@code maxDist}
     * @param hitX     X coordinate of the hit block (if {@code hit})
     * @param hitY     Y coordinate of the hit block (if {@code hit})
     * @param hitZ     Z coordinate of the hit block (if {@code hit})
     * @param distance distance from origin to the hit point (blocks)
     */
    public record RaycastResult(boolean hit, int hitX, int hitY, int hitZ, double distance) {
        /** Convenience: a miss result. */
        public static RaycastResult miss() {
            return new RaycastResult(false, 0, 0, 0, Double.MAX_VALUE);
        }
    }

    /**
     * Performs an Amanatides-Woo DDA voxel traversal from {@code origin}
     * in direction {@code (dirX, dirY, dirZ)}, up to {@code maxDist} blocks.
     *
     * <p>Iterates at most {@code ceil(maxDist * 3)} voxels — for a 6-block
     * reach this is ~18 iterations maximum.
     *
     * @param world   the world to check in
     * @param ox      ray origin X
     * @param oy      ray origin Y
     * @param oz      ray origin Z
     * @param dirX    ray direction X (need not be normalized)
     * @param dirY    ray direction Y
     * @param dirZ    ray direction Z
     * @param maxDist maximum ray distance in blocks
     * @return {@link RaycastResult} describing the first solid block hit
     */
    public static RaycastResult raycast(final World world,
                                        final double ox, final double oy, final double oz,
                                        final double dirX, final double dirY, final double dirZ,
                                        final double maxDist) {
        // Normalize direction
        final double len = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (len < 1e-10) return RaycastResult.miss();
        final double dx = dirX / len;
        final double dy = dirY / len;
        final double dz = dirZ / len;

        // Current voxel
        int vx = (int) Math.floor(ox);
        int vy = (int) Math.floor(oy);
        int vz = (int) Math.floor(oz);

        // Step direction
        final int stepX = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
        final int stepY = dy > 0 ? 1 : (dy < 0 ? -1 : 0);
        final int stepZ = dz > 0 ? 1 : (dz < 0 ? -1 : 0);

        // tMax: distance to next voxel boundary on each axis
        final double tDeltaX = stepX != 0 ? Math.abs(1.0 / dx) : Double.MAX_VALUE;
        final double tDeltaY = stepY != 0 ? Math.abs(1.0 / dy) : Double.MAX_VALUE;
        final double tDeltaZ = stepZ != 0 ? Math.abs(1.0 / dz) : Double.MAX_VALUE;

        double tMaxX = stepX > 0 ? (vx + 1 - ox) / dx : (ox - vx) / (-dx);
        double tMaxY = stepY > 0 ? (vy + 1 - oy) / dy : (oy - vy) / (-dy);
        double tMaxZ = stepZ > 0 ? (vz + 1 - oz) / dz : (oz - vz) / (-dz);

        if (stepX == 0) tMaxX = Double.MAX_VALUE;
        if (stepY == 0) tMaxY = Double.MAX_VALUE;
        if (stepZ == 0) tMaxZ = Double.MAX_VALUE;

        double t = 0.0;
        final int maxIter = (int) Math.ceil(maxDist * 3) + 1;

        for (int i = 0; i < maxIter; i++) {
            // Check current voxel (skip origin voxel on first iteration)
            if (i > 0) {
                final Block block = world.getBlockAt(vx, vy, vz);
                if (block.getType().isSolid()) {
                    return new RaycastResult(true, vx, vy, vz, t);
                }
            }

            // Advance to next voxel boundary
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                t = tMaxX;
                if (t > maxDist) break;
                vx    += stepX;
                tMaxX += tDeltaX;
            } else if (tMaxY < tMaxZ) {
                t = tMaxY;
                if (t > maxDist) break;
                vy    += stepY;
                tMaxY += tDeltaY;
            } else {
                t = tMaxZ;
                if (t > maxDist) break;
                vz    += stepZ;
                tMaxZ += tDeltaZ;
            }
        }

        return RaycastResult.miss();
    }

    /**
     * Convenience overload: performs a raycast from a Bukkit {@link Location}
     * in the direction the location is facing.
     *
     * @param origin  the origin location (yaw/pitch define direction)
     * @param maxDist maximum ray distance in blocks
     * @return {@link RaycastResult}
     */
    public static RaycastResult raycastFromLocation(final Location origin, final double maxDist) {
        final double yawRad   = Math.toRadians(origin.getYaw());
        final double pitchRad = Math.toRadians(origin.getPitch());
        final double cosP     = Math.cos(-pitchRad);
        final double dirX     = -Math.sin(yawRad) * cosP;
        final double dirY     =  Math.sin(-pitchRad);
        final double dirZ     =  Math.cos(yawRad)  * cosP;

        return raycast(origin.getWorld(),
                       origin.getX(), origin.getY() + 1.62, origin.getZ(),
                       dirX, dirY, dirZ, maxDist);
    }

    // ─── Block friction lookup ────────────────────────────────────────────────

    /**
     * Returns the friction coefficient for the given material.
     *
     * @param material the block material
     * @return friction in [0, 1]
     */
    public static double getBlockFriction(final Material material) {
        return switch (material) {
            case ICE, FROSTED_ICE, PACKED_ICE, BLUE_ICE -> PhysicsConstants.FRICTION_ICE;
            case SLIME_BLOCK                             -> PhysicsConstants.FRICTION_SLIME;
            default                                      -> PhysicsConstants.FRICTION_DEFAULT;
        };
    }

    // ─── Constructor ──────────────────────────────────────────────────────────

    /** Utility class — no instantiation. */
    private CollisionEngine() {
        throw new UnsupportedOperationException("CollisionEngine is a utility class.");
    }
}
