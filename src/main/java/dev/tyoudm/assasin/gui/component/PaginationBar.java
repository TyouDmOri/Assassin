/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.gui.component;

import dev.tyoudm.assasin.gui.AssasinGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Renders previous/next page navigation buttons into an inventory.
 *
 * <p>Buttons are disabled (replaced with a gray glass pane) when there
 * is no previous or next page respectively.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class PaginationBar {

    private final int       currentPage;  // 0-indexed
    private final int       totalPages;
    private final int       prevSlot;
    private final int       nextSlot;
    private final GuiAction onPrev;
    private final GuiAction onNext;

    /**
     * Creates a pagination bar.
     *
     * @param currentPage 0-indexed current page
     * @param totalPages  total number of pages
     * @param prevSlot    inventory slot for the "previous" button
     * @param nextSlot    inventory slot for the "next" button
     * @param onPrev      action when previous is clicked
     * @param onNext      action when next is clicked
     */
    public PaginationBar(final int currentPage, final int totalPages,
                         final int prevSlot, final int nextSlot,
                         final GuiAction onPrev, final GuiAction onNext) {
        this.currentPage = currentPage;
        this.totalPages  = totalPages;
        this.prevSlot    = prevSlot;
        this.nextSlot    = nextSlot;
        this.onPrev      = onPrev;
        this.onNext      = onNext;
    }

    // ─── Apply ────────────────────────────────────────────────────────────────

    /**
     * Applies the pagination buttons to the given GUI (registers items + actions).
     *
     * @param gui the GUI to apply to
     * @return {@code [prevItem, nextItem]}
     */
    public GuiItem[] apply(final AssasinGui gui) {
        final GuiItem prevItem = buildButton(
            currentPage > 0,
            "§7◀ Previous",
            "§7Page " + currentPage + "/" + totalPages,
            onPrev
        );
        final GuiItem nextItem = buildButton(
            currentPage < totalPages - 1,
            "§7Next ▶",
            "§7Page " + (currentPage + 2) + "/" + totalPages,
            onNext
        );

        gui.setItem(prevSlot, prevItem);
        gui.setItem(nextSlot, nextItem);

        return new GuiItem[]{prevItem, nextItem};
    }

    /**
     * @deprecated Use {@link #apply(AssasinGui)} to ensure actions are registered.
     */
    @Deprecated
    public GuiItem[] apply(final Inventory inventory) {
        final GuiItem prevItem = buildButton(
            currentPage > 0,
            "§7◀ Previous",
            "§7Page " + currentPage + "/" + totalPages,
            onPrev
        );
        final GuiItem nextItem = buildButton(
            currentPage < totalPages - 1,
            "§7Next ▶",
            "§7Page " + (currentPage + 2) + "/" + totalPages,
            onNext
        );

        inventory.setItem(prevSlot, prevItem.toItemStack());
        inventory.setItem(nextSlot, nextItem.toItemStack());

        return new GuiItem[]{prevItem, nextItem};
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static GuiItem buildButton(final boolean active, final String name,
                                       final String loreLine, final GuiAction action) {
        if (active) {
            return new GuiItem(Material.ARROW)
                .name(name)
                .lore(loreLine, "", "§eClick to navigate")
                .action(action);
        } else {
            // Disabled — gray glass pane, no action
            return new GuiItem(Material.GRAY_STAINED_GLASS_PANE)
                .name("§8" + name.replaceAll("§.", ""))
                .action(GuiAction.NOOP);
        }
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public int getCurrentPage() { return currentPage; }
    public int getTotalPages()  { return totalPages; }
}
