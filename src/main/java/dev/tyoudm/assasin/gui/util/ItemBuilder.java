/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.gui.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fluent {@link ItemStack} builder with PersistentDataContainer support.
 *
 * <p>Used throughout the GUI system to construct items with names, lore,
 * enchant glow, and PDC metadata in a readable, chainable style.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * ItemStack item = new ItemBuilder(Material.BELL)
 *     .name(Component.text("Alertas"))
 *     .lore("§7Configura canales", "", "§eClick para configurar")
 *     .pdc(plugin, "assasin:gui_action", PersistentDataType.STRING, "OPEN_ALERTS")
 *     .build();
 * }</pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta  meta;

    /**
     * Creates a new builder for the given material (quantity = 1).
     *
     * @param material the item material
     */
    public ItemBuilder(final Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    // ─── Name ─────────────────────────────────────────────────────────────────

    /**
     * Sets the display name using an Adventure {@link Component}.
     * Italic decoration is removed by default (vanilla adds it automatically).
     *
     * @param name the display name component
     * @return {@code this}
     */
    public ItemBuilder name(final Component name) {
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        return this;
    }

    /**
     * Sets the display name using a legacy color-coded string.
     *
     * @param name the display name (supports {@code §} color codes)
     * @return {@code this}
     */
    public ItemBuilder name(final String name) {
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        return this;
    }

    // ─── Lore ─────────────────────────────────────────────────────────────────

    /**
     * Sets the lore from a list of Adventure components.
     *
     * @param lines lore lines
     * @return {@code this}
     */
    public ItemBuilder lore(final List<Component> lines) {
        final List<Component> noItalic = new ArrayList<>();
        for (final Component line : lines) {
            noItalic.add(line.decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(noItalic);
        return this;
    }

    /**
     * Sets the lore from legacy color-coded strings.
     *
     * @param lines lore lines (supports {@code §} color codes)
     * @return {@code this}
     */
    public ItemBuilder lore(final String... lines) {
        final List<Component> components = new ArrayList<>();
        for (final String line : lines) {
            components.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(components);
        return this;
    }

    // ─── Enchant glow ─────────────────────────────────────────────────────────

    /**
     * Adds an enchant glow effect (hidden enchantment).
     *
     * @return {@code this}
     */
    public ItemBuilder enchantGlow() {
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    // ─── Item flags ───────────────────────────────────────────────────────────

    /**
     * Adds item flags to hide attributes, enchants, etc.
     *
     * @param flags the flags to add
     * @return {@code this}
     */
    public ItemBuilder flags(final ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    // ─── PersistentDataContainer ──────────────────────────────────────────────

    /**
     * Stores a string value in the item's PersistentDataContainer.
     *
     * @param plugin the plugin (for namespace)
     * @param key    the key (e.g., {@code "assasin:gui_action"})
     * @param value  the string value
     * @return {@code this}
     */
    public ItemBuilder pdc(final Plugin plugin, final String key, final String value) {
        final String[] parts = key.split(":", 2);
        final NamespacedKey nk = parts.length == 2
            ? new NamespacedKey(parts[0], parts[1])
            : new NamespacedKey(plugin, key);
        meta.getPersistentDataContainer().set(nk, PersistentDataType.STRING, value);
        return this;
    }

    /**
     * Stores a typed value in the item's PersistentDataContainer.
     *
     * @param plugin the plugin (for namespace)
     * @param key    the key
     * @param type   the PDC type
     * @param value  the value
     * @param <T>    the primitive type
     * @param <Z>    the complex type
     * @return {@code this}
     */
    public <T, Z> ItemBuilder pdc(final Plugin plugin, final String key,
                                   final PersistentDataType<T, Z> type, final Z value) {
        final String[] parts = key.split(":", 2);
        final NamespacedKey nk = parts.length == 2
            ? new NamespacedKey(parts[0], parts[1])
            : new NamespacedKey(plugin, key);
        meta.getPersistentDataContainer().set(nk, type, value);
        return this;
    }

    // ─── Build ────────────────────────────────────────────────────────────────

    /**
     * Builds and returns the final {@link ItemStack}.
     *
     * @return the constructed item
     */
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
