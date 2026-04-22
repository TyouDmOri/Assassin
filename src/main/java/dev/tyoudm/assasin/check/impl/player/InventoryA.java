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
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * InventoryA — Movement while inventory is open.
 *
 * <p>Detects when a player moves while an inventory window is open.
 * Vanilla clients cannot move while an inventory is open — the server
 * stops processing movement packets until the window is closed.
 *
 * <h2>False-flag prevention</h2>
 * Only flags when the player is actually moving (speedH &gt; threshold)
 * AND the inventory has been open for at least {@link #MIN_OPEN_TICKS}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "InventoryA",
    type              = CheckType.INVENTORY_A,
    category          = CheckCategory.PLAYER,
    description       = "Detects movement while inventory window is open.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "medium"
)
public final class InventoryA extends Check {

    /** Minimum ticks the inventory must be open before flagging. */
    private static final int    MIN_OPEN_TICKS = 3;
    /** Minimum horizontal speed to consider the player "moving". */
    private static final double MIN_SPEED_H    = 0.05;

    public InventoryA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.INVENTORY_OPEN,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final InventoryTracker it = data.getInventoryTracker();
        if (it == null || !it.isWindowOpen()) return;

        // Grace period after opening
        if (tick - it.getWindowOpenTick() < MIN_OPEN_TICKS) return;

        final double speedH = data.getVelocityH();
        if (speedH < MIN_SPEED_H) return;

        flag(player, data, 1.0,
            String.format("moving with inv open: speedH=%.4f windowId=%d openTicks=%d",
                speedH, it.getOpenWindowId(), tick - it.getWindowOpenTick()),
            tick);
    }
}
