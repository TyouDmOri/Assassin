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
import dev.tyoudm.assasin.data.prediction.ElytraPredictor;
import dev.tyoudm.assasin.data.prediction.PhysicsConstants;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * ElytraA — Elytra physics deviation detection.
 *
 * <p>Uses {@link ElytraPredictor} to simulate expected elytra motion and
 * flags only when the observed velocity deviates persistently for
 * {@link PhysicsConstants#ELYTRA_FLAG_TICKS} or more consecutive ticks.
 *
 * <h2>Critical rule — NO absolute speed flagging</h2>
 * A player diving from altitude can reach 40+ blocks/tick in ~8 seconds.
 * This is vanilla behaviour. ElytraA NEVER flags based on absolute speed —
 * only on deviation from the predictor.
 *
 * <h2>RESET conditions</h2>
 * The predictor is reset (and deviation counter cleared) on:
 * takeoff, landing, firework use, wall collision.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "ElytraA",
    type             = CheckType.ELYTRA_A,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects elytra physics deviation (uses ElytraPredictor).",
    maxVl            = 8.0,
    severity         = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class ElytraA extends Check {

    /** Deviation threshold (blocks/tick) above which a tick is considered suspicious. */
    private static final double DEVIATION_THRESHOLD = 0.5;

    /** Consecutive suspicious ticks required before flagging. */
    private static final int FLAG_TICKS = PhysicsConstants.ELYTRA_FLAG_TICKS;

    private int suspiciousTicks;

    public ElytraA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        // Only active during elytra flight
        if (!data.isElytraActive()) {
            suspiciousTicks = 0;
            return;
        }

        if (isExemptAny(data, tick,
                ExemptType.ELYTRA_BOOST, ExemptType.ELYTRA_WALL_BOUNCE,
                ExemptType.ELYTRA_TAKEOFF, ExemptType.ELYTRA_LANDING,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) {
            suspiciousTicks = 0;
            return;
        }

        final ElytraPredictor predictor = data.getElytraPredictor();
        if (predictor == null) return;

        // Seed predictor on first tick after exempts clear
        if (!predictor.isInitialized()) {
            final double vx = data.getX() - data.getLastX();
            final double vy = data.getVelocityY();
            final double vz = data.getZ() - data.getLastZ();
            predictor.seed(vx, vy, vz);
            return;
        }

        // Advance predictor
        final var prediction = predictor.predict(data.getYaw(), data.getPitch());

        // Compute deviation
        final double vx = data.getX() - data.getLastX();
        final double vy = data.getVelocityY();
        final double vz = data.getZ() - data.getLastZ();
        final double deviation = predictor.computeDeviation(vx, vy, vz);

        if (deviation > DEVIATION_THRESHOLD) {
            suspiciousTicks++;
            if (suspiciousTicks >= FLAG_TICKS) {
                flag(player, data, 1.0,
                    String.format("deviation=%.3f threshold=%.3f ticks=%d",
                        deviation, DEVIATION_THRESHOLD, suspiciousTicks),
                    tick);
            }
        } else {
            suspiciousTicks = Math.max(0, suspiciousTicks - 1);
        }
    }
}
