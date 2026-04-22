/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.world;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.BlockTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 * ScaffoldC — Backward block placement detection.
 *
 * <p>Detects scaffold hacks that place blocks on the face directly behind
 * the player (opposite to their movement direction) without the player
 * turning around. Legitimate backward bridging requires the player to
 * face backward.
 *
 * <h2>Algorithm</h2>
 * If the placed block face is {@link BlockFace#NORTH}, {@link BlockFace#SOUTH},
 * {@link BlockFace#EAST}, or {@link BlockFace#WEST}, and the face direction
 * is opposite to the player's yaw direction, it's suspicious.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "ScaffoldC",
    type              = CheckType.SCAFFOLD_C,
    category          = CheckCategory.WORLD,
    description       = "Detects backward block placement (scaffold).",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "world"
)
public final class ScaffoldC extends Check {

    /** Maximum allowed angle (degrees) between look direction and placement face. */
    private static final double MAX_FACE_ANGLE = 135.0;

    public ScaffoldC(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null || bt.getLastPlaceTick() != tick) return;

        final BlockFace face = bt.getLastPlacedFace();

        // Only check horizontal faces
        if (face == BlockFace.UP || face == BlockFace.DOWN) return;

        // Convert face to angle
        final double faceAngle = switch (face) {
            case NORTH -> 180.0;
            case SOUTH -> 0.0;
            case EAST  -> -90.0;
            case WEST  -> 90.0;
            default    -> Double.NaN;
        };
        if (Double.isNaN(faceAngle)) return;

        // Player yaw (0 = south, 90 = west, 180/-180 = north, -90 = east)
        final double yaw = data.getYaw();

        // Angle between player look and face direction
        double diff = Math.abs(yaw - faceAngle);
        if (diff > 180.0) diff = 360.0 - diff;

        if (diff > MAX_FACE_ANGLE) {
            flag(player, data, 1.0,
                String.format("backward place: face=%s yaw=%.1f diff=%.1f max=%.1f",
                    face, yaw, diff, MAX_FACE_ANGLE),
                tick);
        }
    }
}
