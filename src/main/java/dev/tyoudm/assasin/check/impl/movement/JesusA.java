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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * JesusA — Water walking detection.
 *
 * <p>Flags when the player reports {@code onGround=true} while standing on
 * a water block without Frost Walker enchantment or Depth Strider.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "JesusA",
    type             = CheckType.JESUS_A,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects walking on water (Jesus hack).",
    maxVl            = 8.0,
    severity         = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class JesusA extends Check {

    public JesusA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.LIQUID, ExemptType.VEHICLE,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        if (!data.isOnGround()) return;

        // Check the block directly below the player
        final Block below = player.getLocation().subtract(0, 0.1, 0).getBlock();
        if (below.getType() != Material.WATER) return;

        // Frost Walker creates ice — if the block is water, it's Jesus
        flag(player, data, 2.0,
            String.format("standing on water at (%.1f, %.1f, %.1f)",
                data.getX(), data.getY(), data.getZ()),
            tick);
    }
}
