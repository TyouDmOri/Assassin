/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.movement;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.MathUtil;
import org.bukkit.entity.Player;

/**
 * StrafeA — Illegal strafe pattern detection.
 *
 * <p>Detects when the player's movement direction deviates significantly
 * from their yaw direction in a way that is physically impossible without
 * a strafe hack. Legitimate A/D-tap oscillation is tolerated via
 * {@link #ADTAP_TOLERANCE}.
 *
 * <h2>Algorithm</h2>
 * Computes the angle between the movement vector and the player's facing
 * direction. If the deviation exceeds the threshold for multiple consecutive
 * ticks, it flags.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "StrafeA",
    type             = CheckType.STRAFE_A,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects illegal strafe patterns.",
    maxVl            = 8.0,
    severity         = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "soft"
)
public final class StrafeA extends Check {

    /** Maximum allowed deviation between movement direction and yaw (degrees). */
    private static final double MAX_DEVIATION  = 90.0 + 15.0; // 90° strafe + 15° tolerance
    /** A/D-tap tolerance — lateral oscillation is allowed up to this Δ. */
    private static final double ADTAP_TOLERANCE = 0.15;
    /** Minimum speed to run this check (avoid false positives at near-zero speed). */
    private static final double MIN_SPEED       = 0.08;

    public StrafeA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.ELYTRA_ACTIVE, ExemptType.VEHICLE,
                ExemptType.LIQUID, ExemptType.TELEPORT_PENDING,
                ExemptType.SETBACK)) return;

        final double speedH = data.getVelocityH();
        if (speedH < MIN_SPEED) return;

        final double dx = data.getX() - data.getLastX();
        final double dz = data.getZ() - data.getLastZ();
        if (Math.abs(dx) < 1e-6 && Math.abs(dz) < 1e-6) return;

        // Movement direction angle
        final double moveAngle = Math.toDegrees(Math.atan2(-dx, dz));
        // Player facing angle (yaw → world direction)
        final double yaw       = data.getYaw();

        final double deviation = MathUtil.angleDiff(moveAngle, yaw);

        if (deviation > MAX_DEVIATION) {
            flag(player, data, 0.5,
                String.format("deviation=%.1f° max=%.1f° yaw=%.1f moveAngle=%.1f",
                    deviation, MAX_DEVIATION, yaw, moveAngle),
                tick);
        }
    }
}
