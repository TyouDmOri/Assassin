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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Vanilla physics constants for Minecraft 1.21.11 "Mounts of Mayhem".
 *
 * <p>All values are sourced from Mojang's deobfuscated source and the
 * community-maintained wiki. Constants are {@code static final} primitives
 * to allow JIT inlining in hot paths.
 *
 * <h2>Mount physics</h2>
 * Each rideable entity type has a {@link MountPhysics} record describing
 * its vanilla speed, jump strength, and water behaviour. The map is
 * populated at class-load time and accessed via {@link #getMountPhysics(EntityType)}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class PhysicsConstants {

    // ─── Gravity ──────────────────────────────────────────────────────────────

    /** Vanilla gravity acceleration (blocks/tick²). Applied every tick as: motionY -= GRAVITY. */
    public static final double GRAVITY              = 0.08;

    /** Elytra gravity factor (reduced gravity while gliding). */
    public static final double GRAVITY_ELYTRA       = 0.08;

    // ─── Drag ─────────────────────────────────────────────────────────────────

    /** Vertical motion drag in air each tick: motionY *= DRAG_AIR. */
    public static final double DRAG_AIR             = 0.98;

    /** Horizontal motion drag in air each tick: motionH *= DRAG_H_AIR. */
    public static final double DRAG_H_AIR           = 0.91;

    /** Horizontal drag while on ground: motionH *= DRAG_H_GROUND * blockFriction. */
    public static final double DRAG_H_GROUND        = 0.6;

    /** Horizontal drag in water each tick. */
    public static final double DRAG_H_WATER         = 0.8;

    /** Vertical drag in water each tick. */
    public static final double DRAG_V_WATER         = 0.8;

    /** Elytra horizontal drag per tick. */
    public static final double DRAG_ELYTRA_H        = 0.99;

    /** Elytra vertical drag per tick. */
    public static final double DRAG_ELYTRA_V        = 0.98;

    // ─── Block friction ───────────────────────────────────────────────────────

    /** Default block friction (stone, dirt, etc.). */
    public static final double FRICTION_DEFAULT     = 0.6;

    /** Ice block friction. */
    public static final double FRICTION_ICE         = 0.98;

    /** Packed ice / blue ice friction. */
    public static final double FRICTION_PACKED_ICE  = 0.98;

    /** Slime block friction. */
    public static final double FRICTION_SLIME       = 0.8;

    // ─── Player movement ──────────────────────────────────────────────────────

    /** Base walk speed attribute value (blocks/tick). */
    public static final double SPEED_WALK           = 0.221;

    /** Base sprint speed (blocks/tick) — vanilla observed max ~0.46 b/t. */
    public static final double SPEED_SPRINT         = 0.46;

    /** Sneak speed multiplier applied to walk speed. */
    public static final double SPEED_SNEAK_MULT     = 0.3;

    /** Jump velocity (blocks/tick, upward). */
    public static final double JUMP_VELOCITY        = 0.42;

    /** Sprint-jump horizontal boost added to motionX/Z. */
    public static final double JUMP_SPRINT_BOOST    = 0.2;

    /** Step height (blocks) — player can step up this height without jumping. */
    public static final double STEP_HEIGHT          = 0.6;

    // ─── Elytra ───────────────────────────────────────────────────────────────

    /** Elytra gravity constant (same as GRAVITY but kept separate for clarity). */
    public static final double ELYTRA_GRAVITY       = 0.08;

    /** Elytra horizontal drag per tick. */
    public static final double ELYTRA_DRAG_H        = 0.99;

    /** Elytra vertical drag per tick. */
    public static final double ELYTRA_DRAG_V        = 0.98;

    /** Firework boost velocity added to elytra motion. */
    public static final double ELYTRA_FIREWORK_BOOST = 1.5;

    /** Ticks of boost exemption after a firework use. */
    public static final int    ELYTRA_BOOST_TICKS   = 20;

    /** Ticks of wall-bounce exemption after a lateral collision. */
    public static final int    ELYTRA_WALL_BOUNCE_TICKS = 5;

    /** Ticks of takeoff exemption (ground → air transition). */
    public static final int    ELYTRA_TAKEOFF_TICKS = 10;

    /** Ticks of landing exemption (air → ground transition). */
    public static final int    ELYTRA_LANDING_TICKS = 15;

    /** Minimum consecutive ticks of deviation before ElytraA flags. */
    public static final int    ELYTRA_FLAG_TICKS    = 12;

    // ─── Riptide ──────────────────────────────────────────────────────────────

    /** Ticks of riptide exemption after trident use. */
    public static final int    RIPTIDE_EXEMPT_TICKS = 20;

    // ─── Mount physics record ─────────────────────────────────────────────────

    /**
     * Vanilla physics parameters for a rideable entity type.
     *
     * @param maxSpeedLand    maximum horizontal speed on land (blocks/tick)
     * @param maxSpeedWater   maximum horizontal speed in water (blocks/tick)
     * @param jumpStrength    jump velocity (blocks/tick); 0 if not jumpable
     * @param canFly          whether this mount can sustain flight
     * @param isAquatic       whether this mount is primarily aquatic
     */
    public record MountPhysics(
        double  maxSpeedLand,
        double  maxSpeedWater,
        double  jumpStrength,
        boolean canFly,
        boolean isAquatic
    ) {}

    // ─── Mount physics map ────────────────────────────────────────────────────

    private static final Map<EntityType, MountPhysics> MOUNT_PHYSICS_MAP;

    static {
        final Map<EntityType, MountPhysics> m = new EnumMap<>(EntityType.class);

        // Horse variants — speed varies by attribute; use vanilla default
        m.put(EntityType.HORSE,          new MountPhysics(0.3375, 0.12, 0.7, false, false));
        m.put(EntityType.DONKEY,         new MountPhysics(0.175,  0.12, 0.5, false, false));
        m.put(EntityType.MULE,           new MountPhysics(0.175,  0.12, 0.5, false, false));
        m.put(EntityType.SKELETON_HORSE, new MountPhysics(0.3375, 0.12, 0.7, false, false));
        m.put(EntityType.ZOMBIE_HORSE,   new MountPhysics(0.3375, 0.12, 0.7, false, false)); // 1.21.11 rideable

        // Pig
        m.put(EntityType.PIG,            new MountPhysics(0.3,    0.12, 0.0, false, false));

        // Strider — land speed in lava; water speed is irrelevant (takes damage)
        m.put(EntityType.STRIDER,        new MountPhysics(0.175,  0.0,  0.0, false, false));

        // Boat — treated as aquatic; land speed is 0 (can't move on land)
        m.put(EntityType.OAK_BOAT,        new MountPhysics(0.0,    0.4,  0.0, false, true));
        m.put(EntityType.OAK_CHEST_BOAT,  new MountPhysics(0.0,    0.4,  0.0, false, true));

        // Llama / Trader Llama — not directly steerable but can be ridden via lead
        m.put(EntityType.LLAMA,          new MountPhysics(0.175,  0.12, 0.5, false, false));
        m.put(EntityType.TRADER_LLAMA,   new MountPhysics(0.175,  0.12, 0.5, false, false));

        // Camel — 1.20+ rideable with dash
        m.put(EntityType.CAMEL,          new MountPhysics(0.3,    0.12, 0.0, false, false));

        // Nautilus (aquatic mount — 1.21.11 "Mounts of Mayhem")
        // Speed in water is higher; on land it flops and is very slow
        m.put(EntityType.SQUID,          new MountPhysics(0.05,   0.35, 0.0, false, true));

        MOUNT_PHYSICS_MAP = Collections.unmodifiableMap(m);
    }

    /**
     * Returns the {@link MountPhysics} for the given entity type, or
     * a conservative default if the type is not in the map.
     *
     * @param type the entity type
     * @return mount physics (never {@code null})
     */
    public static MountPhysics getMountPhysics(final EntityType type) {
        return MOUNT_PHYSICS_MAP.getOrDefault(type,
            new MountPhysics(0.3, 0.12, 0.5, false, false));
    }

    /**
     * Returns an unmodifiable view of the full mount physics map.
     *
     * @return mount physics map
     */
    public static Map<EntityType, MountPhysics> getMountPhysicsMap() {
        return MOUNT_PHYSICS_MAP;
    }

    // ─── Constructor ──────────────────────────────────────────────────────────

    /** Constants class — no instantiation. */
    private PhysicsConstants() {
        throw new UnsupportedOperationException("PhysicsConstants is a constants class.");
    }
}
