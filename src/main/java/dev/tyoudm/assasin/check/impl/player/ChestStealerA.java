/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.player;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.InventoryTracker;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * ChestStealerA — Chest item theft speed detection.
 *
 * <p>Detects chest-stealer hacks that click inventory slots faster than
 * humanly possible. Legitimate players cannot click more than
 * {@link #MAX_CLICKS_PER_SECOND} slots per second.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "ChestStealerA",
    type              = CheckType.CHEST_STEALER_A,
    category          = CheckCategory.PLAYER,
    description       = "Detects chest-stealer via click rate.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class ChestStealerA extends Check {

    /** Maximum clicks per second a human can perform. */
    private static final int MAX_CLICKS_PER_SECOND = 20;

    /** Window size in ticks for click rate measurement. */
    private static final int WINDOW_TICKS = 20;

    private int  clicksInWindow;
    private long windowStartTick;

    public ChestStealerA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final InventoryTracker it = data.getInventoryTracker();
        if (it == null || !it.isWindowOpen()) return;

        if (windowStartTick == 0) windowStartTick = tick;

        clicksInWindow = it.getClicksThisWindow();

        final long elapsed = tick - windowStartTick;
        if (elapsed < WINDOW_TICKS) return;

        final double clicksPerSecond = clicksInWindow * 20.0 / elapsed;

        if (clicksPerSecond > MAX_CLICKS_PER_SECOND) {
            flag(player, data, 1.5,
                String.format("chest stealer: %.1f clicks/s max=%d",
                    clicksPerSecond, MAX_CLICKS_PER_SECOND),
                tick);
        }

        // Reset window
        clicksInWindow  = 0;
        windowStartTick = tick;
    }
}
