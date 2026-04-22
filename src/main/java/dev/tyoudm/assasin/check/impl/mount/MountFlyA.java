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
 * MountFlyA — Mount fly bypass detection.
 *
 * <p>Detects when a player sustains flight on a non-flying mount. Most
 * mounts (horses, pigs, striders, etc.) cannot fly — if the player remains
 * airborne for more than {@link #MAX_AIR_TICKS} consecutive ticks without
 * a valid jump arc, it's a fly exploit.
 *
 * <h2>Exemptions</h2>
 * <ul>
 *   <li>Mounts with {@link PhysicsConstants.MountPhysics#canFly()} = true
 *       are completely exempt.</li>
 *   <li>Normal jump arcs (motionY &gt; {@link #JUMP_DESCENT_THRESHOLD}) are
 *       allowed up to {@link #MAX_AIR_TICKS} ticks.</li>
 *   <li>Grace period of {@link #MOUNT_GRACE_TICKS} after mounting.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "MountFlyA",
    type              = CheckType.MOUNT_FLY_A,
    category          = CheckCategory.MOUNT,
    description       = "Detects fly bypass while riding a non-flying mount.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.CRITICAL,
    mitigationProfile = "mount"
)
public final class MountFlyA extends Check {

    /** Grace period after mounting (ticks). */
    private static final int MOUNT_GRACE_TICKS = 5;

    /**
     * Maximum consecutive air ticks allowed on a non-flying mount.
     * A horse jump lasts ~6 ticks; allow 12 for tolerance + lag.
     */
    private static final int MAX_AIR_TICKS = 12;

    /**
     * motionY threshold below which the player is considered to be in
     * a normal descent (not hovering). Below this = suspicious hover.
     */
    private static final double JUMP_DESCENT_THRESHOLD = -0.4;

    // Per-player state
    private int airTicksOnMount;

    public MountFlyA(final MitigationEngine engine) { super(engine); }

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

        final EntityType mountType = mt.getVehicleType();
        if (mountType == null) return;

        // Skip flying mounts entirely
        if (MountPredictor.canFly(mountType)) return;

        if (tick - mt.getMountTick() < MOUNT_GRACE_TICKS) return;

        if (!data.isOnGround()) {
            airTicksOnMount++;

            final double motionY = data.getVelocityY();

            // Flag if airborne too long AND descending too slowly (hover)
            if (airTicksOnMount > MAX_AIR_TICKS && motionY > JUMP_DESCENT_THRESHOLD) {
                flag(player, data, 1.5,
                    String.format("mount=%s airTicks=%d motionY=%.4f threshold=%.4f",
                        mountType.name(), airTicksOnMount, motionY, JUMP_DESCENT_THRESHOLD),
                    tick);
            }
        } else {
            airTicksOnMount = 0;
        }
    }
}
