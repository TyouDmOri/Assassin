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
import dev.tyoudm.assasin.data.tracker.MountTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * MountSpeedA — Mount horizontal speed violation.
 *
 * <p>Flags when the player's horizontal speed while riding a mount exceeds
 * the vanilla maximum for that entity type, accounting for terrain (land vs.
 * water) and ping tolerance.
 *
 * <h2>Per-entity-type thresholds</h2>
 * All thresholds come from {@link MountPredictor#maxExpectedSpeedH}, which
 * reads from {@link dev.tyoudm.assasin.data.prediction.PhysicsConstants#getMountPhysics}.
 * The check uses a {@code switch} on {@link EntityType} for JIT-friendly dispatch.
 *
 * <h2>False-flag prevention</h2>
 * <ul>
 *   <li>Exempt for the first {@link #MOUNT_GRACE_TICKS} ticks after mounting
 *       (acceleration ramp-up).</li>
 *   <li>Ping tolerance included in {@link MountPredictor#maxExpectedSpeedH}.</li>
 *   <li>Requires {@link #MIN_OVER_TICKS} consecutive over-speed ticks before
 *       flagging to absorb brief bursts.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "MountSpeedA",
    type              = CheckType.MOUNT_SPEED_A,
    category          = CheckCategory.MOUNT,
    description       = "Detects horizontal speed violation while riding a mount.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "mount"
)
public final class MountSpeedA extends Check {

    /** Grace period after mounting before the check activates (ticks). */
    private static final int MOUNT_GRACE_TICKS = 5;

    /** Consecutive over-speed ticks required before flagging. */
    private static final int MIN_OVER_TICKS = 3;

    public MountSpeedA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        // ── Early exits ──────────────────────────────────────────────────────
        if (!data.isInVehicle()) return;
        if (isExemptAny(data, tick,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final MountTracker mt = data.getMountTracker();
        if (mt == null || !mt.isMounted()) return;

        // Grace period after mounting
        if (tick - mt.getMountTick() < MOUNT_GRACE_TICKS) return;

        final EntityType mountType = mt.getVehicleType();
        if (mountType == null) return;

        final double speedH  = data.getVelocityH();
        final boolean inWater = mt.isInWater();
        final double maxSpeed = MountPredictor.maxExpectedSpeedH(mountType, inWater, data.getPing());

        // Update tracker
        mt.updateSpeed(speedH, maxSpeed, inWater);

        if (mt.getOverSpeedTicks() >= MIN_OVER_TICKS) {
            final double excess = speedH - maxSpeed;
            flag(player, data, excess * 3.0,
                String.format("mount=%s speedH=%.4f max=%.4f excess=%.4f inWater=%b overTicks=%d",
                    mountType.name(), speedH, maxSpeed, excess, inWater, mt.getOverSpeedTicks()),
                tick);
        }
    }
}
