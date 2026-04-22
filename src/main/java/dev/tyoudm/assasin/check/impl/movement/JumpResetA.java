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
import dev.tyoudm.assasin.core.LegitTechniqueRegistry;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.prediction.PhysicsConstants;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.WelfordStats;
import org.bukkit.entity.Player;

/**
 * JumpResetA — Jump-reset cheat detection (timing variance).
 *
 * <p>Detects automated jump-reset by measuring the variance (σ) of
 * jump intervals. Legitimate players have high variance; macros have
 * near-zero variance.
 *
 * <h2>Legit PvP — false-flag prevention</h2>
 * Only flags when:
 * <ul>
 *   <li>σ &lt; {@link LegitTechniqueRegistry.Tolerance#sigmaThreshold()} (default 1.5)</li>
 *   <li>n ≥ {@link LegitTechniqueRegistry.Tolerance#minSamples()} (default 8)</li>
 *   <li>Success rate ≥ {@link LegitTechniqueRegistry.Tolerance#successRateLimit()} (default 95%)</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "JumpResetA",
    type             = CheckType.JUMP_RESET_A,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects automated jump-reset via timing variance (σ).",
    maxVl            = 8.0,
    severity         = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "soft"
)
public final class JumpResetA extends Check {

    private final LegitTechniqueRegistry legitRegistry;

    // Per-player state
    private final WelfordStats jumpIntervalStats = new WelfordStats();
    private long lastJumpTick;
    private int  successfulResets;
    private int  totalResets;

    public JumpResetA(final MitigationEngine engine,
                      final LegitTechniqueRegistry legitRegistry) {
        super(engine);
        this.legitRegistry = legitRegistry;
    }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.VEHICLE, ExemptType.LIQUID,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final var it = data.getInputTracker();
        if (it == null) return;

        // Detect jump: ground → air transition with positive Y velocity
        final boolean justJumped = !data.isOnGround() && data.wasOnGround()
            && data.getVelocityY() > PhysicsConstants.JUMP_VELOCITY - 0.05;

        if (!justJumped) return;

        if (lastJumpTick > 0) {
            final long interval = tick - lastJumpTick;
            jumpIntervalStats.add(interval);
            totalResets++;

            // Count as "successful reset" if jump happened within 3t of an attack
            final var ct = data.getCombatTracker();
            if (ct != null && Math.abs(tick - ct.getLastAttackTick()) <= 3) {
                successfulResets++;
            }
        }
        lastJumpTick = tick;

        // Check thresholds
        final var tolerance = legitRegistry.get(LegitTechniqueRegistry.Technique.JUMP_RESET);
        final int minSamples = tolerance.minSamples();
        if (jumpIntervalStats.count() < minSamples) return;

        final double sigma       = jumpIntervalStats.stdDev();
        final double successRate = totalResets > 0 ? (double) successfulResets / totalResets : 0.0;

        if (sigma < tolerance.sigmaThreshold() && successRate >= tolerance.successRateLimit()) {
            flag(player, data, 1.0,
                String.format("σ=%.3f threshold=%.3f successRate=%.2f n=%d",
                    sigma, tolerance.sigmaThreshold(), successRate, jumpIntervalStats.count()),
                tick);
        }
    }
}
