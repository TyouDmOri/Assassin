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
import dev.tyoudm.assasin.data.prediction.PhysicsConstants;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * FlyA — Gravity bypass detection.
 *
 * <p>Flags when the player is airborne and their vertical motion does not
 * decrease by the expected gravity amount. Catches fly hacks that maintain
 * constant Y or ascend without a jump.
 *
 * <h2>Algorithm</h2>
 * Expected motionY(t) = motionY(t-1) - GRAVITY × DRAG_AIR.
 * If observed motionY > expected + tolerance → flag.
 *
 * <h2>Disabled during elytra</h2>
 * FlyA is completely disabled when {@link ExemptType#ELYTRA_ACTIVE} is set —
 * elytra flight is legitimate fly.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "FlyA",
    type             = CheckType.FLY_A,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects gravity bypass (fly hack).",
    maxVl            = 10.0,
    severity         = CheckInfo.Severity.CRITICAL,
    mitigationProfile = "hard"
)
public final class FlyA extends Check {

    private static final double TOLERANCE = 0.03;

    public FlyA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        // Disabled entirely during elytra, riptide, vehicle, climbable
        if (isExemptAny(data, tick,
                ExemptType.ELYTRA_ACTIVE, ExemptType.RIPTIDE,
                ExemptType.VEHICLE, ExemptType.CLIMBABLE,
                ExemptType.LIQUID, ExemptType.TELEPORT_PENDING,
                ExemptType.SETBACK)) return;

        // Only check when airborne
        if (data.isOnGround()) return;

        final var mt = data.getMovementTracker();
        if (mt == null || mt.getAirTicks() < 2) return; // need at least 2 air ticks

        final double motionY     = data.getVelocityY();
        final double prevMotionY = mt.getSpeedY().size() >= 2
            ? mt.getSpeedY().get(mt.getSpeedY().size() - 2)
            : motionY;

        // Expected: previous Y velocity after gravity + drag
        final double expectedY = (prevMotionY - PhysicsConstants.GRAVITY) * PhysicsConstants.DRAG_AIR;

        if (motionY > expectedY + TOLERANCE) {
            flag(player, data, 1.5,
                String.format("motionY=%.4f expected=%.4f airTicks=%d",
                    motionY, expectedY, mt.getAirTicks()),
                tick);
        }
    }
}
