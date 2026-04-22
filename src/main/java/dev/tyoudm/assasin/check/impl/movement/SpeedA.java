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
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * SpeedA — Horizontal speed violation (absolute threshold).
 *
 * <p>Flags when the player's horizontal speed exceeds the maximum expected
 * value for their current input state (sprint/walk/sneak), accounting for
 * ping tolerance.
 *
 * <h2>False-flag prevention</h2>
 * <ul>
 *   <li>Exempt during elytra, riptide, vehicle, liquid transitions.</li>
 *   <li>Ping tolerance added via {@link MovementPredictor#maxExpectedSpeedH}.</li>
 *   <li>W-tap exempt: sprint toggle within 3t of attack suppresses for 5t.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "SpeedA",
    type             = CheckType.SPEED_A,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects horizontal speed exceeding vanilla maximum.",
    maxVl            = 10.0,
    severity         = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class SpeedA extends Check {

    public SpeedA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        // ── Exempts ──────────────────────────────────────────────────────────
        if (isExemptAny(data, tick,
                ExemptType.ELYTRA_ACTIVE, ExemptType.ELYTRA_BOOST,
                ExemptType.RIPTIDE, ExemptType.VEHICLE,
                ExemptType.LIQUID, ExemptType.TELEPORT_PENDING,
                ExemptType.SETBACK, ExemptType.WTAP)) return;

        final double speedH = data.getVelocityH();
        if (speedH < 0.001) return; // not moving — early exit

        final double maxSpeed = MovementPredictor.maxExpectedSpeedH(
            data.isSprinting(), data.isSneaking(), data.getPing());

        if (speedH > maxSpeed) {
            final double excess = speedH - maxSpeed;
            flag(player, data, excess * 2.0,
                String.format("speedH=%.4f max=%.4f excess=%.4f", speedH, maxSpeed, excess),
                tick);
        }
    }
}
