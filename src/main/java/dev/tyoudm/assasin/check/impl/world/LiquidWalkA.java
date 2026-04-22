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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 * LiquidWalkA — Block placement on liquid detection.
 *
 * <p>Detects when a player places a block directly on top of a liquid
 * (water or lava) without a valid support block. This is a common
 * scaffold/liquid-walk exploit.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "LiquidWalkA",
    type              = CheckType.LIQUID_WALK_A,
    category          = CheckCategory.WORLD,
    description       = "Detects block placement on liquid (liquid-walk exploit).",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "world"
)
public final class LiquidWalkA extends Check {

    public LiquidWalkA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null || bt.getLastPlaceTick() != tick) return;

        // Only check placements on the top face of a block
        if (bt.getLastPlacedFace() != BlockFace.UP) return;

        // Check the block below the placed block
        final Block below = player.getTargetBlockExact(5) != null
            ? player.getTargetBlockExact(5)
            : null;
        if (below == null) return;

        final Block support = below.getRelative(BlockFace.DOWN);
        final Material supportType = support.getType();

        if (supportType == Material.WATER || supportType == Material.LAVA) {
            flag(player, data, 2.0,
                String.format("liquid place: support=%s at (%d,%d,%d)",
                    supportType, support.getX(), support.getY(), support.getZ()),
                tick);
        }
    }
}
