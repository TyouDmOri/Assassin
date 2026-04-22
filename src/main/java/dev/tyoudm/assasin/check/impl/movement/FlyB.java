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
import org.bukkit.entity.Player;

/**
 * FlyB — Hover detection.
 *
 * <p>Flags when the player remains airborne for an extended period with
 * near-zero vertical velocity (hovering). Requires {@link #MIN_AIR_TICKS}
 * consecutive air ticks with |motionY| below the hover threshold.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "FlyB",
    type             = CheckType.FLY_B,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects hovering (near-zero vertical velocity while airborne).",
    maxVl            = 8.0,
    severity         = CheckInfo.Severity.HIGH,
    mitigationProfile = "hard"
)
public final class FlyB extends Check {

    /** Minimum consecutive air ticks before hover check activates. */
    private static final int    MIN_AIR_TICKS   = 10;
    /** Maximum |motionY| to be considered hovering. */
    private static final double HOVER_THRESHOLD = 0.03;

    public FlyB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.ELYTRA_ACTIVE, ExemptType.RIPTIDE,
                ExemptType.VEHICLE, ExemptType.CLIMBABLE,
                ExemptType.LIQUID, ExemptType.TELEPORT_PENDING,
                ExemptType.SETBACK)) return;

        if (data.isOnGround()) return;

        final var mt = data.getMovementTracker();
        if (mt == null || mt.getAirTicks() < MIN_AIR_TICKS) return;

        final double motionY = data.getVelocityY();
        if (Math.abs(motionY) < HOVER_THRESHOLD) {
            flag(player, data, 1.0,
                String.format("motionY=%.4f airTicks=%d", motionY, mt.getAirTicks()),
                tick);
        }
    }
}
