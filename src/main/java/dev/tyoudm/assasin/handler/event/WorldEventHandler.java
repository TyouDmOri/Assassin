/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.handler.event;

import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.check.CheckProcessor;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.data.tracker.BlockTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles world interaction events: block place, block break, and interact.
 *
 * <p>Feeds {@link BlockTracker} with material and face data for
 * {@code ScaffoldA/B/C}, {@code FastBreakA}, {@code NukerA}, and
 * {@code AirPlaceA}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class WorldEventHandler implements Listener {

    private final AssasinPlugin     plugin;
    private final PlayerDataManager dataManager;
    private final CheckProcessor    checkProcessor;

    public WorldEventHandler(final AssasinPlugin plugin,
                             final PlayerDataManager dataManager,
                             final CheckProcessor checkProcessor) {
        this.plugin          = plugin;
        this.dataManager     = dataManager;
        this.checkProcessor  = checkProcessor;
    }

    // ─── Block place ──────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Player     player = event.getPlayer();
        final PlayerData data   = dataManager.get(player);
        if (data == null) return;

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null) return;

        final long tick = plugin.getServer().getCurrentTick();
        bt.recordPlace(event.getBlockPlaced().getType(),
                       event.getBlockAgainst().getFace(event.getBlockPlaced()),
                       tick);

        checkProcessor.processBlock(player, data, tick);
    }

    // ─── Block break ──────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player     player = event.getPlayer();
        final PlayerData data   = dataManager.get(player);
        if (data == null) return;

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null) return;

        final long tick = plugin.getServer().getCurrentTick();
        bt.recordBreak(event.getBlock().getType(), tick);

        checkProcessor.processBlock(player, data, tick);
    }

    // ─── Interact ─────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
        // Reserved for future checks (FreecamA, InteractA)
        // No tracker update needed at this phase
    }
}
