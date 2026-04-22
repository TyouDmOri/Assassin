/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.gui.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Fills the border slots of any inventory with Red Stained Glass Panes.
 *
 * <p>Border slots are: top row, bottom row, leftmost column, rightmost column.
 * The formula for a 9-wide inventory:
 * <ul>
 *   <li>Top row: slots 0–8</li>
 *   <li>Bottom row: slots {@code (rows-1)*9} to {@code rows*9 - 1}</li>
 *   <li>Left column: slots {@code 0, 9, 18, ...}</li>
 *   <li>Right column: slots {@code 8, 17, 26, ...}</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class GuiBorder {

    /** The border item — Red Stained Glass Pane with a single space name. */
    private static final ItemStack BORDER_ITEM;

    static {
        BORDER_ITEM = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        final ItemMeta meta = BORDER_ITEM.getItemMeta();
        meta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        BORDER_ITEM.setItemMeta(meta);
    }

    /** Utility class — no instantiation. */
    private GuiBorder() {
        throw new UnsupportedOperationException("GuiBorder is a utility class.");
    }

    // ─── Apply ────────────────────────────────────────────────────────────────

    /**
     * Fills all border slots of the given inventory with the border item.
     *
     * @param inventory the inventory to fill (must be 9 columns wide)
     */
    public static void apply(final Inventory inventory) {
        final int size = inventory.getSize();
        final int cols = 9;
        final int rows = size / cols;

        for (int slot = 0; slot < size; slot++) {
            if (isBorder(slot, rows, cols)) {
                inventory.setItem(slot, BORDER_ITEM.clone());
            }
        }
    }

    /**
     * Returns {@code true} if the given slot is a border slot.
     *
     * @param slot the slot index
     * @param rows number of rows
     * @param cols number of columns (always 9)
     * @return {@code true} if border
     */
    public static boolean isBorder(final int slot, final int rows, final int cols) {
        final int row = slot / cols;
        final int col = slot % cols;
        return row == 0 || row == rows - 1 || col == 0 || col == cols - 1;
    }

    /**
     * Returns a fresh clone of the border item.
     *
     * @return border item clone
     */
    public static ItemStack item() {
        return BORDER_ITEM.clone();
    }
}
