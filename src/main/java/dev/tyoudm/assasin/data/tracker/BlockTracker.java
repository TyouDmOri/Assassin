/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.tracker;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * Tracks per-player block interaction state for world checks.
 *
 * <p>Records the last block place/break event and a per-tick collision
 * cache used by {@code ScaffoldA/B/C}, {@code FastBreakA}, and
 * {@code AirPlaceA}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class BlockTracker {

    // ─── Last place ───────────────────────────────────────────────────────────

    /** Material of the last placed block. */
    private Material lastPlacedMaterial = Material.AIR;

    /** Block face used for the last placement. */
    private BlockFace lastPlacedFace = BlockFace.UP;

    /** Server tick of the last block placement. */
    private long lastPlaceTick;

    /** Number of blocks placed this tick. */
    private int placedThisTick;

    // ─── Last break ───────────────────────────────────────────────────────────

    /** Material of the last broken block. */
    private Material lastBrokenMaterial = Material.AIR;

    /** Server tick of the last block break. */
    private long lastBreakTick;

    /** Number of blocks broken this tick. */
    private int brokenThisTick;

    // ─── Collision cache ──────────────────────────────────────────────────────

    /** Whether the player was touching a solid block on the left side last tick. */
    private boolean collidingLeft;
    /** Whether the player was touching a solid block on the right side last tick. */
    private boolean collidingRight;
    /** Whether the player was touching a solid block above last tick. */
    private boolean collidingAbove;

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Records a block placement event.
     *
     * @param material    the placed material
     * @param face        the block face used
     * @param currentTick current server tick
     */
    public void recordPlace(final Material material, final BlockFace face, final long currentTick) {
        lastPlacedMaterial = material;
        lastPlacedFace     = face;
        if (currentTick == lastPlaceTick) {
            placedThisTick++;
        } else {
            placedThisTick = 1;
            lastPlaceTick  = currentTick;
        }
    }

    /**
     * Records a block break event.
     *
     * @param material    the broken material
     * @param currentTick current server tick
     */
    public void recordBreak(final Material material, final long currentTick) {
        lastBrokenMaterial = material;
        if (currentTick == lastBreakTick) {
            brokenThisTick++;
        } else {
            brokenThisTick = 1;
            lastBreakTick  = currentTick;
        }
    }

    /**
     * Updates the collision cache for the current tick.
     *
     * @param left  colliding left
     * @param right colliding right
     * @param above colliding above
     */
    public void updateCollision(final boolean left, final boolean right, final boolean above) {
        collidingLeft  = left;
        collidingRight = right;
        collidingAbove = above;
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public Material  getLastPlacedMaterial() { return lastPlacedMaterial; }
    public BlockFace getLastPlacedFace()     { return lastPlacedFace; }
    public long      getLastPlaceTick()      { return lastPlaceTick; }
    public int       getPlacedThisTick()     { return placedThisTick; }
    public Material  getLastBrokenMaterial() { return lastBrokenMaterial; }
    public long      getLastBreakTick()      { return lastBreakTick; }
    public int       getBrokenThisTick()     { return brokenThisTick; }
    public boolean   isCollidingLeft()       { return collidingLeft; }
    public boolean   isCollidingRight()      { return collidingRight; }
    public boolean   isCollidingAbove()      { return collidingAbove; }

    /** Resets all state. Call on respawn. */
    public void reset() {
        lastPlacedMaterial = Material.AIR;
        lastPlacedFace     = BlockFace.UP;
        lastPlaceTick      = 0L;
        placedThisTick     = 0;
        lastBrokenMaterial = Material.AIR;
        lastBreakTick      = 0L;
        brokenThisTick     = 0;
        collidingLeft      = false;
        collidingRight     = false;
        collidingAbove     = false;
    }
}
