/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.exempt;

/**
 * Enumeration of all exemption types recognized by ASSASIN.
 *
 * <p>Each constant represents a specific game state or event that should
 * suppress one or more checks for a bounded duration. Exemptions are
 * managed by {@link ExemptManager} and stored per-player in
 * {@link dev.tyoudm.assasin.data.PlayerData}.
 *
 * <h2>Naming convention</h2>
 * {@code CATEGORY_VARIANT} — e.g., {@code ELYTRA_BOOST}, {@code TELEPORT_PENDING}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public enum ExemptType {

    // ─── Teleport / Setback ───────────────────────────────────────────────────

    /**
     * Player has received a teleport packet and has not yet confirmed it.
     * Suppresses all movement and position checks.
     */
    TELEPORT_PENDING,

    /**
     * Player was set back by the mitigation engine.
     * Suppresses movement checks for 2 ticks post-setback.
     */
    SETBACK,

    // ─── Respawn / Join ───────────────────────────────────────────────────────

    /**
     * Player just joined or respawned.
     * Suppresses all checks for 20 ticks.
     */
    RESPAWN,

    /**
     * Player is in the process of changing worlds (dimension travel).
     * Suppresses all checks until the new world is confirmed.
     */
    WORLD_CHANGE,

    // ─── Velocity / Knockback ─────────────────────────────────────────────────

    /**
     * Server sent a velocity packet to the player.
     * Suppresses VelocityA/B/C for the expected response window.
     */
    VELOCITY,

    /**
     * Player received knockback from an explosion.
     * Suppresses VelocityA/B for 5 ticks.
     */
    EXPLOSION_KNOCKBACK,

    // ─── Elytra ───────────────────────────────────────────────────────────────

    /**
     * Player has elytra deployed.
     * Suppresses FlyA, FlyB, NoFallA during flight.
     */
    ELYTRA_ACTIVE,

    /**
     * Player used a firework rocket while gliding.
     * Suppresses ElytraA, SpeedA/B for 20 ticks post-boost.
     */
    ELYTRA_BOOST,

    /**
     * Player bounced off a wall while gliding.
     * Suppresses ElytraA for 5 ticks.
     */
    ELYTRA_WALL_BOUNCE,

    /**
     * Player just took off with elytra (ground → air transition).
     * Suppresses ElytraA, SpeedA for 10 ticks.
     */
    ELYTRA_TAKEOFF,

    /**
     * Player is landing from elytra flight (air → ground transition).
     * Suppresses NoFallA, ElytraA for 15 ticks.
     */
    ELYTRA_LANDING,

    // ─── Riptide ──────────────────────────────────────────────────────────────

    /**
     * Player used a riptide trident.
     * Suppresses SpeedA/B, FlyA for 20 ticks.
     */
    RIPTIDE,

    // ─── Pearl / Enderpearl ───────────────────────────────────────────────────

    /**
     * Player threw an enderpearl and is awaiting teleport.
     * Suppresses PhaseA for 3 seconds (60 ticks).
     */
    PEARL,

    // ─── Liquid ───────────────────────────────────────────────────────────────

    /**
     * Player is in or transitioning through liquid.
     * Suppresses JesusA, SpeedA for 3 ticks.
     */
    LIQUID,

    // ─── Climbable ────────────────────────────────────────────────────────────

    /**
     * Player is on a climbable surface (ladder, vine, scaffold).
     * Suppresses FlyA, NoFallA.
     */
    CLIMBABLE,

    // ─── Vehicle / Mount ──────────────────────────────────────────────────────

    /**
     * Player is riding a vehicle or mount.
     * Suppresses movement checks; mount checks take over.
     */
    VEHICLE,

    // ─── Combat ───────────────────────────────────────────────────────────────

    /**
     * Player performed a w-tap (sprint off → on around attack).
     * Suppresses VelocityA for 5 ticks.
     */
    WTAP,

    /**
     * Player performed a block-hit (shield USE_ITEM active during attack).
     * Reduces expected KB by 50% for VelocityA/B.
     */
    BLOCK_HIT,

    /**
     * Player swapped held item between attacks (attribute-swap).
     * Informs VelocityA, MaceDmgA, AutoClickerA to re-baseline.
     */
    ATTRIBUTE_SWAP,

    // ─── Inventory ────────────────────────────────────────────────────────────

    /**
     * Player has an inventory screen open.
     * Suppresses movement checks that require sprint/sneak input.
     */
    INVENTORY_OPEN,

    // ─── Lag / TPS ────────────────────────────────────────────────────────────

    /**
     * Server TPS dropped below 18 — lag spike detected.
     * Suppresses all checks for 5 ticks post-recovery.
     */
    LAG_SPIKE,

    /**
     * Player's ping exceeds the configured ceiling (default 300ms).
     * Suppresses MacroTimingA and other latency-sensitive checks.
     */
    HIGH_PING,

    // ─── Staff / Bypass ───────────────────────────────────────────────────────

    /**
     * Player has the {@code assasin.bypass} permission.
     * Suppresses all checks permanently while the permission is held.
     */
    BYPASS,

    /**
     * Player was manually exempted by staff via {@code /assasin exempt}.
     * Duration is configurable per invocation.
     */
    STAFF_EXEMPT,

    // ─── Misc ─────────────────────────────────────────────────────────────────

    /**
     * Player is in creative or spectator mode.
     * Suppresses all checks.
     */
    GAMEMODE,

    /**
     * Player is dead (awaiting respawn screen confirmation).
     * Suppresses all checks.
     */
    DEAD,

    /**
     * Player is sleeping in a bed.
     * Suppresses movement checks.
     */
    SLEEPING
}
