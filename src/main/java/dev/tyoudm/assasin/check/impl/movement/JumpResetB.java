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
 * JumpResetB — Jump-reset cheat detection (impossible timing).
 *
 * <p>Complements {@link JumpResetA} by detecting jump-resets that occur
 * in physically impossible timing: the player jumps and lands within a
 * single tick, or jumps with a Y velocity that doesn't match the vanilla
 * jump velocity.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "JumpResetB",
    type             = CheckType.JUMP_RESET_B,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects impossible jump-reset timing.",
    maxVl            = 8.0,
    severity         = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "soft"
)
public final class JumpResetB extends Check {

    /** Tolerance around the expected jump velocity. */
    private static final double JUMP_VEL_TOLERANCE = 0.05;

    public JumpResetB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.VEHICLE, ExemptType.LIQUID,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        // Detect jump: ground → air transition
        if (data.isOnGround() || !data.wasOnGround()) return;

        final double motionY = data.getVelocityY();
        final double expectedJump = PhysicsConstants.JUMP_VELOCITY;

        // If the player "jumped" but their Y velocity is significantly higher
        // than the vanilla jump velocity, it's suspicious
        if (motionY > expectedJump + JUMP_VEL_TOLERANCE) {
            flag(player, data, 1.5,
                String.format("motionY=%.4f expectedJump=%.4f excess=%.4f",
                    motionY, expectedJump, motionY - expectedJump),
                tick);
        }
    }
}
