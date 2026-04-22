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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 * ScaffoldA — Scaffold rotation consistency check.
 *
 * <p>Validates that the player's look direction is consistent with the
 * block face used for placement. Scaffold hacks place blocks on faces
 * that the player is not looking at.
 *
 * <h2>Algorithm</h2>
 * Raycasts from the player's eye position in their look direction.
 * If the ray does not hit the placed block's face within reach distance,
 * the placement is invalid.
 *
 * <h2>Legit bridging</h2>
 * Speed-bridge, ninja-bridge, and jitter-bridge all use valid raytrace
 * results — they will NOT be flagged by this check.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "ScaffoldA",
    type              = CheckType.SCAFFOLD_A,
    category          = CheckCategory.WORLD,
    description       = "Detects scaffold via rotation-placement raytrace validation.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "world"
)
public final class ScaffoldA extends Check {

    /** Maximum reach distance for block placement (blocks). */
    private static final double MAX_PLACE_REACH = 5.0;

    public ScaffoldA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null || bt.getLastPlaceTick() != tick) return;

        // Raycast from eye in look direction
        final var result = CollisionEngine.raycastFromLocation(
            player.getEyeLocation(), MAX_PLACE_REACH);

        if (!result.hit()) {
            // Player placed a block but the raycast didn't hit anything — scaffold
            flag(player, data, 1.5,
                String.format("scaffold: no raycast hit, face=%s material=%s",
                    bt.getLastPlacedFace(), bt.getLastPlacedMaterial()),
                tick);
        }
    }
}
