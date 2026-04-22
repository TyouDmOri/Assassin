/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.tracker;

import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * Tracks per-player combat state for combat checks.
 *
 * <p>Records the last attack event, target history, and combat timing
 * used by KillauraA-D, ReachA/B, CriticalsA, and VelocityA/B/C.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CombatTracker {

    // ─── Last attack ──────────────────────────────────────────────────────────

    /** UUID of the last attacked entity. */
    private UUID   lastTargetUuid;

    /** Server tick of the last attack. */
    private long   lastAttackTick;

    /** System time (ms) of the last attack. */
    private long   lastAttackMs;

    /** Yaw at the moment of the last attack. */
    private float  attackYaw;

    /** Pitch at the moment of the last attack. */
    private float  attackPitch;

    /** Distance to target at the moment of the last attack. */
    private double attackDistance;

    // ─── Multi-target tracking ────────────────────────────────────────────────

    /** Number of distinct targets attacked in the last 20 ticks. */
    private int    recentTargetCount;

    /** Tick of the last target switch. */
    private long   lastTargetSwitchTick;

    // ─── Cooldown ─────────────────────────────────────────────────────────────

    /** Whether the player's attack cooldown was fully charged at last attack. */
    private boolean fullCooldown;

    /** Attack cooldown progress at last attack (0.0–1.0). */
    private float   cooldownProgress;

    // ─── Crit state ───────────────────────────────────────────────────────────

    /** Whether the last attack was a critical hit. */
    private boolean lastWasCrit;

    /** Y velocity at the moment of the last attack (for CriticalsA). */
    private double  attackMotionY;

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Records an attack event.
     *
     * @param target          the attacked entity
     * @param currentTick     current server tick
     * @param yaw             attacker yaw at attack time
     * @param pitch           attacker pitch at attack time
     * @param distance        distance to target
     * @param motionY         attacker Y velocity at attack time
     * @param cooldownProgress attack cooldown progress (0.0–1.0)
     * @param isCrit          whether this was a critical hit
     */
    public void recordAttack(final Entity target, final long currentTick,
                             final float yaw, final float pitch,
                             final double distance, final double motionY,
                             final float cooldownProgress, final boolean isCrit) {
        final UUID targetUuid = target.getUniqueId();
        if (!targetUuid.equals(lastTargetUuid)) {
            recentTargetCount++;
            lastTargetSwitchTick = currentTick;
        }

        lastTargetUuid    = targetUuid;
        lastAttackTick    = currentTick;
        lastAttackMs      = System.currentTimeMillis();
        attackYaw         = yaw;
        attackPitch       = pitch;
        attackDistance    = distance;
        attackMotionY     = motionY;
        this.cooldownProgress = cooldownProgress;
        fullCooldown      = cooldownProgress >= 0.9f;
        lastWasCrit       = isCrit;
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public UUID    getLastTargetUuid()       { return lastTargetUuid; }
    public long    getLastAttackTick()       { return lastAttackTick; }
    public long    getLastAttackMs()         { return lastAttackMs; }
    public float   getAttackYaw()            { return attackYaw; }
    public float   getAttackPitch()          { return attackPitch; }
    public double  getAttackDistance()       { return attackDistance; }
    public double  getAttackMotionY()        { return attackMotionY; }
    public float   getCooldownProgress()     { return cooldownProgress; }
    public boolean isFullCooldown()          { return fullCooldown; }
    public boolean isLastWasCrit()           { return lastWasCrit; }
    public int     getRecentTargetCount()    { return recentTargetCount; }
    public long    getLastTargetSwitchTick() { return lastTargetSwitchTick; }

    /** Resets all combat state. Call on death or respawn. */
    public void reset() {
        lastTargetUuid       = null;
        lastAttackTick       = 0L;
        lastAttackMs         = 0L;
        attackYaw            = 0f;
        attackPitch          = 0f;
        attackDistance       = 0.0;
        attackMotionY        = 0.0;
        cooldownProgress     = 0f;
        fullCooldown         = false;
        lastWasCrit          = false;
        recentTargetCount    = 0;
        lastTargetSwitchTick = 0L;
    }
}
