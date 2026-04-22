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
 * FastPlaceA — Block placement rate detection.
 *
 * <p>Detects when a player places blocks faster than the vanilla placement
 * rate allows. Vanilla allows one block placement per 4 ticks (250ms).
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "FastPlaceA",
    type              = CheckType.FAST_PLACE_A,
    category          = CheckCategory.WORLD,
    description       = "Detects block placement rate violation.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "world"
)
public final class FastPlaceA extends Check {

    /** Minimum ticks between block placements (vanilla = 4 ticks). */
    private static final int MIN_PLACE_INTERVAL_TICKS = 4;

    /** Tolerance: allow placing up to this many ticks faster. */
    private static final int TOLERANCE_TICKS = 1;

    private long lastPlaceTick;

    public FastPlaceA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null || bt.getLastPlaceTick() != tick) return;

        if (lastPlaceTick > 0) {
            final long interval = tick - lastPlaceTick;
            final int  minInterval = MIN_PLACE_INTERVAL_TICKS - TOLERANCE_TICKS;

            if (interval < minInterval) {
                flag(player, data, 1.0,
                    String.format("fastplace: interval=%dt min=%dt",
                        interval, MIN_PLACE_INTERVAL_TICKS),
                    tick);
            }
        }

        lastPlaceTick = tick;
    }
}
