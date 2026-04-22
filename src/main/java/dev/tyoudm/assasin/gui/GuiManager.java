/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.gui;

import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.gui.screen.MainGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central GUI manager — owns all open {@link AssasinGui} instances and
 * routes Bukkit inventory events to the correct GUI.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class GuiManager implements Listener {

    /** UUID → currently open GUI. */
    private final ConcurrentHashMap<UUID, AssasinGui> openGuis = new ConcurrentHashMap<>();

    /**
     * UUIDs of players currently transitioning between GUIs.
     * While in this set, InventoryCloseEvent will NOT remove the GUI entry.
     */
    private final Set<UUID> transitioning = ConcurrentHashMap.newKeySet();

    private final AssasinPlugin plugin;

    public GuiManager(final AssasinPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // ─── Open ─────────────────────────────────────────────────────────────────

    public void openMain(final Player player) {
        open(player, new MainGui(plugin, player));
    }

    /**
     * Opens the given GUI for the given player.
     *
     * <p>Marks the player as "transitioning" before opening so that the
     * InventoryCloseEvent fired by the previous inventory does not remove
     * the new GUI from the map.
     */
    public void open(final Player player, final AssasinGui gui) {
        final UUID uuid = player.getUniqueId();

        // Mark as transitioning so the close event doesn't clear the new GUI
        transitioning.add(uuid);

        openGuis.put(uuid, gui);
        gui.open(player);

        // Schedule removal of the transitioning flag on the next tick,
        // after Bukkit has processed the open/close events.
        plugin.getServer().getScheduler().runTask(plugin, () -> transitioning.remove(uuid));
    }

    // ─── Events ───────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        if (!(event.getInventory().getHolder() instanceof AssasinGui)) return;

        final AssasinGui gui = openGuis.get(player.getUniqueId());
        if (gui == null) return;

        gui.handleClick(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof final Player player)) return;
        if (!(event.getInventory().getHolder() instanceof AssasinGui)) return;

        final UUID uuid = player.getUniqueId();

        // Don't remove if the player is transitioning to another ASSASIN GUI
        if (transitioning.contains(uuid)) return;

        openGuis.remove(uuid);
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    public AssasinGui getGui(final UUID uuid) { return openGuis.get(uuid); }
    public boolean    hasGui(final UUID uuid) { return openGuis.containsKey(uuid); }
    public int        openCount()             { return openGuis.size(); }
}
