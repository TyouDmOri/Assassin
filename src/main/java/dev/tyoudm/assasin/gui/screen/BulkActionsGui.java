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
import dev.tyoudm.assasin.gui.component.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class BulkActionsGui extends AssasinGui {

    private final AssasinPlugin plugin;
    private final Player        viewer;

    public BulkActionsGui(final AssasinPlugin plugin, final Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        buildInventory();
    }

    @Override
    protected void buildInventory() {
        createInventory(3, Component.text("⚙ ASSASIN — Bulk Actions")
            .decoration(TextDecoration.ITALIC, false));
        applyBorder();
        populateActions();
        populateNavigation();
    }

    private void populateActions() {
        setItem(10, new GuiItem(Material.LIME_CONCRETE)
            .name("§aEnable ALL")
            .lore("§7Enable all checks", "", "§eClick to enable")
            .pdc(plugin, "assasin:gui_action", "BULK_ENABLE_ALL")
            .action(e -> ((Player) e.getWhoClicked())
                .sendMessage("§a[ASSASIN] All checks enabled.")));

        setItem(11, new GuiItem(Material.RED_CONCRETE)
            .name("§cDisable ALL")
            .lore("§7Disable all checks", "", "§cClick to disable")
            .pdc(plugin, "assasin:gui_action", "BULK_DISABLE_ALL")
            .action(e -> ((Player) e.getWhoClicked())
                .sendMessage("§c[ASSASIN] All checks disabled.")));

        setItem(13, new GuiItem(Material.CLOCK)
            .name("§eReset ALL VLs")
            .lore("§7Reset all violation levels", "", "§eClick to reset")
            .pdc(plugin, "assasin:gui_action", "RESET_ALL_VL")
            .action(e -> ((Player) e.getWhoClicked())
                .sendMessage("§a[ASSASIN] All VLs reset.")));

        setItem(15, new GuiItem(Material.ENDER_CHEST)
            .name("§bSave Config")
            .lore("§7Save current configuration", "", "§eClick to save")
            .pdc(plugin, "assasin:gui_action", "CONFIG_SAVE")
            .action(e -> {
                plugin.saveConfig();
                ((Player) e.getWhoClicked()).sendMessage("§a[ASSASIN] Configuration saved.");
            }));

        setItem(16, new GuiItem(Material.RECOVERY_COMPASS)
            .name("§bReload Config")
            .lore("§7Reload configuration", "", "§eClick to reload")
            .pdc(plugin, "assasin:gui_action", "CONFIG_RELOAD")
            .action(e -> {
                plugin.reloadConfig();
                ((Player) e.getWhoClicked()).sendMessage("§a[ASSASIN] Configuration reloaded.");
            }));
    }

    private void populateNavigation() {
        setItem(19, new GuiItem(Material.ARROW)
            .name("§7Back")
            .lore("§7Return to main menu")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new MainGui(plugin, (Player) e.getWhoClicked()))));
    }
}
