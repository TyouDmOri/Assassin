/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.tracker;

import org.bukkit.entity.EntityType;

import java.util.UUID;

/**
 * Tracks per-player mount state for mount checks.
 *
 * <p>Records the current vehicle entity type, mount/dismount timing,
 * and speed history used by {@code MountSpeedA}, {@code NautilusA},
 * {@code ZombieHorseA}, and {@code MountFlyA}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MountTracker {

    // ─── Current mount ────────────────────────────────────────────────────────

    /** UUID of the current vehicle entity, or {@code null} if not mounted. */
    private UUID       vehicleUuid;

    /** Entity type of the current vehicle, or {@code null} if not mounted. */
    private EntityType vehicleType;

    /** Server tick when the player mounted the current vehicle. */
    private long       mountTick;

    /** Server tick when the player last dismounted. */
    private long       dismountTick;

    // ─── Speed tracking ───────────────────────────────────────────────────────

    /** Horizontal speed of the mount this tick (blocks/tick). */
    private double currentMountSpeedH;

    /** Number of consecutive ticks the mount has exceeded its vanilla speed. */
    private int    overSpeedTicks;

    // ─── Aquatic state ────────────────────────────────────────────────────────

    /** Whether the mount is currently in water. */
    private boolean inWater;

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Called when the player mounts a vehicle.
     *
     * @param vehicleUuid UUID of the vehicle entity
     * @param vehicleType entity type of the vehicle
     * @param currentTick current server tick
     */
    public void onMount(final UUID vehicleUuid, final EntityType vehicleType,
                        final long currentTick) {
        this.vehicleUuid  = vehicleUuid;
        this.vehicleType  = vehicleType;
        this.mountTick    = currentTick;
        this.overSpeedTicks = 0;
        this.currentMountSpeedH = 0.0;
    }

    /**
     * Called when the player dismounts.
     *
     * @param currentTick current server tick
     */
    public void onDismount(final long currentTick) {
        this.dismountTick = currentTick;
        this.vehicleUuid  = null;
        this.vehicleType  = null;
        this.overSpeedTicks = 0;
    }

    /**
     * Updates mount speed state each movement tick.
     *
     * @param speedH      horizontal speed this tick (blocks/tick)
     * @param maxSpeed    vanilla max speed for this mount type (blocks/tick)
     * @param inWater     whether the mount is in water
     */
    public void updateSpeed(final double speedH, final double maxSpeed, final boolean inWater) {
        this.currentMountSpeedH = speedH;
        this.inWater            = inWater;
        overSpeedTicks = speedH > maxSpeed + 0.01 ? overSpeedTicks + 1 : 0;
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public boolean    isMounted()              { return vehicleUuid != null; }
    public UUID       getVehicleUuid()         { return vehicleUuid; }
    public EntityType getVehicleType()         { return vehicleType; }
    public long       getMountTick()           { return mountTick; }
    public long       getDismountTick()        { return dismountTick; }
    public double     getCurrentMountSpeedH()  { return currentMountSpeedH; }
    public int        getOverSpeedTicks()      { return overSpeedTicks; }
    public boolean    isInWater()              { return inWater; }

    /** Resets all state. Call on respawn. */
    public void reset() {
        vehicleUuid         = null;
        vehicleType         = null;
        mountTick           = 0L;
        dismountTick        = 0L;
        currentMountSpeedH  = 0.0;
        overSpeedTicks      = 0;
        inWater             = false;
    }
}
