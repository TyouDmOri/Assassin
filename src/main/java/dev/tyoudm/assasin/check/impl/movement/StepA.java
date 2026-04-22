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
 * StepA — Illegal step height detection.
 *
 * <p>Flags when the player ascends more than {@link PhysicsConstants#STEP_HEIGHT}
 * blocks in a single tick while on the ground (without jumping). Catches
 * step hacks that allow climbing full blocks without jumping.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "StepA",
    type             = CheckType.STEP_A,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects illegal step height (step hack).",
    maxVl            = 8.0,
    severity         = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class StepA extends Check {

    /** Maximum allowed upward Y delta without a jump (vanilla step height + tolerance). */
    private static final double MAX_STEP = PhysicsConstants.STEP_HEIGHT + 0.05;

    public StepA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.VEHICLE, ExemptType.LIQUID,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        // Only flag when transitioning from ground to ground (no jump)
        if (!data.isOnGround() || !data.wasOnGround()) return;

        final double dy = data.getY() - data.getLastY();
        if (dy > MAX_STEP) {
            flag(player, data, 2.0,
                String.format("dy=%.4f maxStep=%.4f", dy, MAX_STEP),
                tick);
        }
    }
}
