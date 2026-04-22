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
import dev.tyoudm.assasin.data.prediction.MovementPredictor;
import dev.tyoudm.assasin.data.prediction.PhysicsConstants;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * SpeedB — Friction-based movement prediction.
 *
 * <p>Compares the player's observed horizontal speed against the value
 * predicted by the vanilla friction model. Catches speed hacks that stay
 * just below the absolute threshold but ignore ground friction.
 *
 * <h2>Algorithm</h2>
 * Expected speed = previous speed × friction × drag.
 * If observed > expected + tolerance → flag.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "SpeedB",
    type             = CheckType.SPEED_B,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects speed violations using friction-based prediction.",
    maxVl            = 10.0,
    severity         = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class SpeedB extends Check {

    /** Tolerance added to the predicted speed to absorb floating-point variance. */
    private static final double TOLERANCE = 0.04;

    public SpeedB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.ELYTRA_ACTIVE, ExemptType.ELYTRA_BOOST,
                ExemptType.RIPTIDE, ExemptType.VEHICLE,
                ExemptType.LIQUID, ExemptType.TELEPORT_PENDING,
                ExemptType.SETBACK, ExemptType.WTAP)) return;

        final double speedH     = data.getVelocityH();
        final var    mt         = data.getMovementTracker();
        if (mt == null || mt.getSpeedH().size() < 2) return;

        final double prevSpeedH = mt.getSpeedH().get(mt.getSpeedH().size() - 2);
        if (prevSpeedH < 0.001) return;

        // Predicted: previous speed decayed by friction
        final double friction   = data.isOnGround()
            ? PhysicsConstants.FRICTION_DEFAULT * PhysicsConstants.DRAG_H_GROUND
            : PhysicsConstants.DRAG_H_AIR;
        final double predicted  = prevSpeedH * friction;
        final double maxAllowed = Math.max(predicted,
            MovementPredictor.maxExpectedSpeedH(data.isSprinting(), data.isSneaking(), data.getPing()))
            + TOLERANCE;

        if (speedH > maxAllowed) {
            flag(player, data, 1.0,
                String.format("speedH=%.4f predicted=%.4f max=%.4f", speedH, predicted, maxAllowed),
                tick);
        }
    }
}
