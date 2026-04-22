/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.gui;

import dev.tyoudm.assasin.gui.component.GuiAction;
import dev.tyoudm.assasin.gui.component.GuiBorder;
import dev.tyoudm.assasin.gui.component.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for all ASSASIN GUI screens.
 *
 * <p>Implements {@link InventoryHolder} so that Bukkit can identify
 * ASSASIN inventories in {@code InventoryClickEvent} and
 * {@code InventoryCloseEvent} handlers.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>Subclass constructor calls {@link #buildInventory()} to create
 *       the Bukkit inventory and populate slots.</li>
 *   <li>{@link GuiManager} calls {@link #open(Player)} to show the GUI.</li>
 *   <li>On click, {@link GuiManager} calls {@link #handleClick(InventoryClickEvent)}.</li>
 *   <li>On close, {@link GuiManager} removes the GUI from its map.</li>
 * </ol>
 *
 * <h2>Slot registration</h2>
 * Subclasses register items via {@link #setItem(int, GuiItem)}.
 * The action map is keyed by slot index.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public abstract class AssasinGui implements InventoryHolder {

    /** The Bukkit inventory backing this GUI. */
    protected Inventory inventory;

    /** Slot → action map. */
    private final Map<Integer, GuiAction> actions = new HashMap<>();

    // ─── Abstract ─────────────────────────────────────────────────────────────

    /**
     * Builds the inventory: creates it, applies border, and populates all slots.
     * Called once during construction.
     */
    protected abstract void buildInventory();

    // ─── InventoryHolder ──────────────────────────────────────────────────────

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    // ─── Open ─────────────────────────────────────────────────────────────────

    /**
     * Opens this GUI for the given player.
     *
     * @param player the player to show the GUI to
     */
    public void open(final Player player) {
        player.openInventory(inventory);
    }

    // ─── Click handling ───────────────────────────────────────────────────────

    /**
     * Handles a click event on this GUI.
     *
     * <p>Always cancels the event, plays the click sound, then dispatches
     * to the registered action for the clicked slot.
     *
     * @param event the click event
     */
    public void handleClick(final InventoryClickEvent event) {
        event.setCancelled(true);

        final int slot = event.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize()) return;

        final GuiAction action = actions.get(slot);
        if (action == null || action == GuiAction.NOOP) return;

        // Play click sound
        if (event.getWhoClicked() instanceof final Player player) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }

        action.accept(event);
    }

    // ─── Slot management ──────────────────────────────────────────────────────

    /**
     * Places a {@link GuiItem} in the given slot and registers its action.
     *
     * @param slot the inventory slot
     * @param item the GUI item
     */
    public void setItem(final int slot, final GuiItem item) {
        inventory.setItem(slot, item.toItemStack());
        actions.put(slot, item.getAction());
    }

    /**
     * Registers an action for a slot without changing the item.
     *
     * @param slot   the slot
     * @param action the action
     */
    protected void setAction(final int slot, final GuiAction action) {
        actions.put(slot, action);
    }

    /**
     * Clears the action for a slot.
     *
     * @param slot the slot
     */
    protected void clearAction(final int slot) {
        actions.remove(slot);
    }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    /**
     * Refreshes only the slots that have changed (differential update).
     * Subclasses override this to update dynamic content without rebuilding
     * the entire inventory.
     */
    public void refresh() {
        // Default: no-op. Subclasses override for dynamic content.
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Creates a Bukkit inventory with this GUI as the holder.
     *
     * @param rows  number of rows (1–6)
     * @param title the inventory title
     * @return the created inventory
     */
    protected Inventory createInventory(final int rows, final Component title) {
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
        return inventory;
    }

    /**
     * Applies the border to the current inventory.
     */
    protected void applyBorder() {
        GuiBorder.apply(inventory);
    }
}
