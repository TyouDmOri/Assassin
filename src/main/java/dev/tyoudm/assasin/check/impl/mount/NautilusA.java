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
import dev.tyoudm.assasin.data.prediction.PhysicsConstants;
import dev.tyoudm.assasin.data.tracker.MountTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * NautilusA — Aquatic mount speed abuse detection.
 *
 * <p>Detects players exploiting the Nautilus (aquatic mount introduced in
 * 1.21.11 "Mounts of Mayhem") by moving at land speed while in water, or
 * at water speed while on land.
 *
 * <h2>Nautilus physics (1.21.11)</h2>
 * <ul>
 *   <li>In water: max speed ≈ 0.35 b/t (fast aquatic mount)</li>
 *   <li>On land: max speed ≈ 0.05 b/t (flops slowly)</li>
 * </ul>
 *
 * <h2>Detection logic</h2>
 * <ol>
 *   <li>If in water and speed &gt; {@link #MAX_WATER_SPEED} → flag (speed hack).</li>
 *   <li>If on land and speed &gt; {@link #MAX_LAND_SPEED} → flag (land speed exploit).</li>
 * </ol>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "NautilusA",
    type              = CheckType.NAUTILUS_A,
    category          = CheckCategory.MOUNT,
    description       = "Detects aquatic mount (Nautilus) speed abuse in 1.21.11.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "mount"
)
public final class NautilusA extends Check {

    /** Max speed in water (blocks/tick) — from PhysicsConstants SQUID entry + tolerance. */
    private static final double MAX_WATER_SPEED =
        PhysicsConstants.getMountPhysics(EntityType.SQUID).maxSpeedWater()
        + 0.05; // tolerance

    /** Max speed on land (blocks/tick) — Nautilus flops very slowly. */
    private static final double MAX_LAND_SPEED =
        PhysicsConstants.getMountPhysics(EntityType.SQUID).maxSpeedLand()
        + 0.03;

    /** Grace period after mounting (ticks). */
    private static final int MOUNT_GRACE_TICKS = 5;

    public NautilusA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (!data.isInVehicle()) return;
        if (isExemptAny(data, tick,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final MountTracker mt = data.getMountTracker();
        if (mt == null || !mt.isMounted()) return;

        // Only applies to SQUID (Nautilus) mounts
        if (mt.getVehicleType() != EntityType.SQUID) return;

        if (tick - mt.getMountTick() < MOUNT_GRACE_TICKS) return;

        final double speedH  = data.getVelocityH();
        final boolean inWater = mt.isInWater();
        final double maxSpeed = inWater ? MAX_WATER_SPEED : MAX_LAND_SPEED;

        if (speedH > maxSpeed) {
            final double excess = speedH - maxSpeed;
            flag(player, data, excess * 3.0,
                String.format("nautilus speedH=%.4f max=%.4f excess=%.4f inWater=%b",
                    speedH, maxSpeed, excess, inWater),
                tick);
        }
    }
}
