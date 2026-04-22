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
import org.bukkit.entity.Player;

/**
 * NukerA — Multi-block break detection.
 *
 * <p>Detects nuker hacks that break more than one non-adjacent block per
 * tick. Vanilla mining can only break one block per tick.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "NukerA",
    type              = CheckType.NUKER_A,
    category          = CheckCategory.WORLD,
    description       = "Detects nuker (>1 block broken per tick).",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "world"
)
public final class NukerA extends Check {

    /** Maximum blocks allowed to be broken in a single tick. */
    private static final int MAX_BREAKS_PER_TICK = 1;

    public NukerA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null || bt.getLastBreakTick() != tick) return;

        final int breaks = bt.getBrokenThisTick();
        if (breaks > MAX_BREAKS_PER_TICK) {
            flag(player, data, (breaks - MAX_BREAKS_PER_TICK) * 2.0,
                String.format("nuker: %d blocks broken this tick (max %d)",
                    breaks, MAX_BREAKS_PER_TICK),
                tick);
        }
    }
}
