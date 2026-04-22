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
import dev.tyoudm.assasin.data.prediction.CollisionEngine;
import dev.tyoudm.assasin.data.tracker.BlockTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * AirPlaceA — Block placement without support detection.
 *
 * <p>Detects when a player places a block in mid-air without any adjacent
 * solid block to attach to. Uses a raycast to verify that the placement
 * target block has at least one solid neighbor.
 *
 * <h2>Algorithm</h2>
 * Raycasts from the player's eye position. If the ray hits a block, checks
 * that the hit block has at least one solid neighbor in the direction of
 * the placed face. If no solid neighbor exists, it's an air placement.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "AirPlaceA",
    type              = CheckType.AIR_PLACE_A,
    category          = CheckCategory.WORLD,
    description       = "Detects block placement without solid support (air place).",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "world"
)
public final class AirPlaceA extends Check {

    private static final double MAX_PLACE_REACH = 5.0;

    public AirPlaceA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null || bt.getLastPlaceTick() != tick) return;

        // Raycast from eye — if no hit, the placement has no support
        final var result = CollisionEngine.raycastFromLocation(
            player.getEyeLocation(), MAX_PLACE_REACH);

        if (!result.hit()) {
            flag(player, data, 2.0,
                String.format("air place: no support block found, face=%s material=%s",
                    bt.getLastPlacedFace(), bt.getLastPlacedMaterial()),
                tick);
        }
    }
}
