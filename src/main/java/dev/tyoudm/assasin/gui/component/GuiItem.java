/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.gui.component;

import dev.tyoudm.assasin.gui.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Wrapper combining an {@link ItemStack} with a {@link GuiAction}.
 *
 * <p>Each slot in an ASSASIN GUI is backed by a {@code GuiItem} that
 * carries both the visual representation and the click handler.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * GuiItem item = new GuiItem(Material.IRON_SWORD)
 *     .name("§cKill Aura")
 *     .lore("§7Detecta ataques ilegales", "", "§eClick para abrir")
 *     .enchantGlow()
 *     .action(event -> openCategory(event.getWhoClicked(), CheckCategory.KILL_AURA))
 *     .pdc(plugin, "assasin:gui_action", "OPEN_CATEGORY");
 * }</pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class GuiItem {

    private final ItemBuilder builder;
    private GuiAction         action = GuiAction.NOOP;

    /**
     * Creates a new {@code GuiItem} for the given material.
     *
     * @param material the item material
     */
    public GuiItem(final Material material) {
        this.builder = new ItemBuilder(material);
    }

    // ─── Builder delegates ────────────────────────────────────────────────────

    public GuiItem name(final String name)              { builder.name(name);       return this; }
    public GuiItem name(final Component name)           { builder.name(name);       return this; }
    public GuiItem lore(final String... lines)          { builder.lore(lines);      return this; }
    public GuiItem lore(final List<Component> lines)    { builder.lore(lines);      return this; }
    public GuiItem enchantGlow()                        { builder.enchantGlow();    return this; }
    public GuiItem pdc(final Plugin p, final String k, final String v) {
        builder.pdc(p, k, v); return this;
    }

    // ─── Action ───────────────────────────────────────────────────────────────

    /**
     * Sets the click action for this item.
     *
     * @param action the action to execute on click
     * @return {@code this}
     */
    public GuiItem action(final GuiAction action) {
        this.action = action;
        return this;
    }

    // ─── Build ────────────────────────────────────────────────────────────────

    /**
     * Returns the built {@link ItemStack}.
     *
     * @return the item stack
     */
    public ItemStack toItemStack() {
        return builder.build();
    }

    /**
     * Returns the click action.
     *
     * @return the action
     */
    public GuiAction getAction() {
        return action;
    }
}
