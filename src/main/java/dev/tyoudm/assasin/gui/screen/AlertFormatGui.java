/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.gui.screen;

import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.gui.AssasinGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

/**
 * Alert Format GUI — opens a Written Book for the player to edit the alert format.
 *
 * <p>Per gui.md, this screen is NOT a Bukkit inventory — it opens a Written Book
 * directly via {@code player.openBook(ItemStack)}. The {@link AssasinGui} base
 * class is used only for lifecycle management; the inventory is a 1-slot dummy.
 *
 * <p>Requires {@code assasin.admin} permission.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class AlertFormatGui extends AssasinGui {

    private final AssasinPlugin plugin;
    private final Player        viewer;

    public AlertFormatGui(final AssasinPlugin plugin, final Player viewer) {
        this.plugin  = plugin;
        this.viewer  = viewer;
        buildInventory();
    }

    @Override
    protected void buildInventory() {
        // Minimal 1-row inventory (required by AssasinGui base)
        createInventory(1, Component.text("Alert Format")
            .decoration(TextDecoration.ITALIC, false));
    }

    @Override
    public void open(final Player player) {
        // Open a Written Book instead of the inventory
        final ItemStack book = buildBook();
        player.openBook(book);
    }

    // ─── Book builder ─────────────────────────────────────────────────────────

    private ItemStack buildBook() {
        final ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        final BookMeta  meta = (BookMeta) book.getItemMeta();

        meta.setTitle("Alert Format");
        meta.setAuthor("TyouDm");

        final String currentFormat = plugin.getConfig()
            .getString("alerts.format", "{player} failed {check} (VL: {vl})");

        meta.pages(
            Component.text()
                .append(Component.text("§6§lASSASSIN Alert Format\n\n"))
                .append(Component.text("§7Current format:\n"))
                .append(Component.text("§f" + currentFormat + "\n\n"))
                .append(Component.text("§7Placeholders:\n"))
                .append(Component.text("§e{player} §7- Player name\n"))
                .append(Component.text("§e{check} §7- Check name\n"))
                .append(Component.text("§e{vl} §7- Current VL\n"))
                .append(Component.text("§e{maxvl} §7- Max VL\n"))
                .append(Component.text("§e{ping} §7- Ping\n"))
                .append(Component.text("§e{world} §7- World\n"))
                .append(Component.text("§e{x},{y},{z} §7- Coords\n"))
                .append(Component.text("§e{time} §7- Hora\n"))
                .build()
        );

        book.setItemMeta(meta);
        return book;
    }
}
