/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.mount;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.prediction.MountPredictor;
import dev.tyoudm.assasin.data.prediction.PhysicsConstants;
import dev.tyoudm.assasin.data.tracker.MountTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * ZombieHorseA — Untamed Zombie Horse exploit detection (1.21.11).
 *
 * <p>In Minecraft 1.21.11 "Mounts of Mayhem", Zombie Horses became rideable
 * without taming. This check detects two exploit vectors:
 *
 * <ol>
 *   <li><b>Speed exploit</b> — riding a Zombie Horse faster than its vanilla
 *       maximum speed (same as a regular horse: 0.3375 b/t).</li>
 *   <li><b>Fly exploit</b> — a Zombie Horse cannot fly; if the player is
 *       airborne on one for more than {@link #MAX_AIR_TICKS} ticks without
 *       a valid jump, it's suspicious.</li>
 * </ol>
 *
 * <h2>False-flag prevention</h2>
 * <ul>
 *   <li>Grace period of {@link #MOUNT_GRACE_TICKS} after mounting.</li>
 *   <li>Jump is allowed: {@link PhysicsConstants.MountPhysics#jumpStrength()} = 0.7.</li>
 *   <li>Requires {@link #MIN_OVER_TICKS} consecutive over-speed ticks.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "ZombieHorseA",
    type              = CheckType.ZOMBIE_HORSE_A,
    category          = CheckCategory.MOUNT,
    description       = "Detects untamed Zombie Horse exploits (speed + fly) in 1.21.11.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "mount"
)
public final class ZombieHorseA extends Check {

    /** Grace period after mounting (ticks). */
    private static final int MOUNT_GRACE_TICKS = 5;

    /** Consecutive over-speed ticks required before flagging. */
    private static final int MIN_OVER_TICKS = 3;

    /**
     * Maximum air ticks allowed on a Zombie Horse without a valid jump.
     * A normal horse jump lasts ~6 ticks; allow up to 10 for tolerance.
     */
    private static final int MAX_AIR_TICKS = 10;

    // Per-player state
    private int airTicksOnMount;

    public ZombieHorseA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (!data.isInVehicle()) {
            airTicksOnMount = 0;
            return;
        }
        if (isExemptAny(data, tick,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final MountTracker mt = data.getMountTracker();
        if (mt == null || !mt.isMounted()) return;

        // Only applies to ZOMBIE_HORSE
        if (mt.getVehicleType() != EntityType.ZOMBIE_HORSE) return;

        if (tick - mt.getMountTick() < MOUNT_GRACE_TICKS) return;

        // ── Speed check ──────────────────────────────────────────────────────
        final double speedH  = data.getVelocityH();
        final double maxSpeed = MountPredictor.maxExpectedSpeedH(
            EntityType.ZOMBIE_HORSE, mt.isInWater(), data.getPing());

        mt.updateSpeed(speedH, maxSpeed, mt.isInWater());

        if (mt.getOverSpeedTicks() >= MIN_OVER_TICKS) {
            final double excess = speedH - maxSpeed;
            flag(player, data, excess * 3.0,
                String.format("zombie_horse speed=%.4f max=%.4f excess=%.4f overTicks=%d",
                    speedH, maxSpeed, excess, mt.getOverSpeedTicks()),
                tick);
            return; // don't double-flag same tick
        }

        // ── Fly check ────────────────────────────────────────────────────────
        if (!data.isOnGround()) {
            airTicksOnMount++;
            // Allow normal jump arc (motionY > 0 = ascending, or brief descent)
            final double motionY = data.getVelocityY();
            final boolean validJump = motionY > -0.5; // still in jump arc
            if (!validJump && airTicksOnMount > MAX_AIR_TICKS) {
                flag(player, data, 1.5,
                    String.format("zombie_horse fly airTicks=%d motionY=%.4f",
                        airTicksOnMount, motionY),
                    tick);
            }
        } else {
            airTicksOnMount = 0;
        }
    }
}
