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
 * NoFallA — Fall damage bypass detection.
 *
 * <p>Flags when the player lands (onGround transitions from false to true)
 * after a significant fall distance but reports {@code onGround=true} without
 * the expected fall damage being applied.
 *
 * <p>Specifically: if the player accumulated {@link #MIN_FALL_DISTANCE} blocks
 * of fall distance and the client reports landing, but the server-side fall
 * distance is near zero, the client is spoofing the onGround flag.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "NoFallA",
    type             = CheckType.NO_FALL_A,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects fall damage bypass.",
    maxVl            = 8.0,
    severity         = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class NoFallA extends Check {

    /** Minimum fall distance (blocks) before this check activates. */
    private static final double MIN_FALL_DISTANCE = 3.0;

    public NoFallA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.ELYTRA_ACTIVE, ExemptType.ELYTRA_LANDING,
                ExemptType.RIPTIDE, ExemptType.VEHICLE,
                ExemptType.CLIMBABLE, ExemptType.LIQUID,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        // Only trigger on landing transition
        if (!data.isOnGround() || data.wasOnGround()) return;

        final var mt = data.getMovementTracker();
        if (mt == null) return;

        final double fallDist = mt.getFallDistance();
        if (fallDist < MIN_FALL_DISTANCE) return;

        // Server-side fall distance should match; if player.getFallDistance() ≈ 0
        // while our tracker says they fell, they're bypassing fall damage.
        if (player.getFallDistance() < 0.5f && fallDist > MIN_FALL_DISTANCE) {
            flag(player, data, 2.0,
                String.format("trackerFall=%.2f serverFall=%.2f", fallDist, player.getFallDistance()),
                tick);
        }
    }
}
