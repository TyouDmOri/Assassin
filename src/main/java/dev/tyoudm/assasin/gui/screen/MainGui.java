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
import dev.tyoudm.assasin.gui.component.GuiBorder;
import dev.tyoudm.assasin.gui.component.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class MainGui extends AssasinGui {

    private static final String ADMIN_PERMISSION = "assasin.admin";

    private final AssasinPlugin plugin;
    private final Player        viewer;

    public MainGui(final AssasinPlugin plugin, final Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        buildInventory();
    }

    @Override
    protected void buildInventory() {
        createInventory(6, Component.text("⚔ ASSASIN — Main Panel")
            .decoration(TextDecoration.ITALIC, false));
        applyBorder();
        populateCategories();
        populateUtilities();
        populateAdmin();
    }

    private void populateCategories() {
        setItem(10, new GuiItem(Material.IRON_SWORD)
            .name("§cKill Aura")
            .lore("§7Killaura and combat checks", "", "§eClick to open")
            .enchantGlow()
            .pdc(plugin, "assasin:gui_action", "OPEN_CATEGORY")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new CategoryGui(plugin, (Player) e.getWhoClicked(), "KILL_AURA"))));

        setItem(11, new GuiItem(Material.FEATHER)
            .name("§6Movement")
            .lore("§7Movement checks", "", "§eClick to open")
            .pdc(plugin, "assasin:gui_action", "OPEN_CATEGORY")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new CategoryGui(plugin, (Player) e.getWhoClicked(), "MOVEMENT"))));

        setItem(12, new GuiItem(Material.BOW)
            .name("§bAim Checks")
            .lore("§7Aim assist checks", "", "§eClick to open")
            .enchantGlow()
            .pdc(plugin, "assasin:gui_action", "OPEN_CATEGORY")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new CategoryGui(plugin, (Player) e.getWhoClicked(), "AIM"))));

        setItem(13, new GuiItem(Material.SCAFFOLDING)
            .name("§aScaffold / Place")
            .lore("§7Scaffold and placement checks", "", "§eClick to open")
            .pdc(plugin, "assasin:gui_action", "OPEN_CATEGORY")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new CategoryGui(plugin, (Player) e.getWhoClicked(), "SCAFFOLD"))));

        setItem(14, new GuiItem(Material.TRIDENT)
            .name("§5Combat+")
            .lore("§7Advanced combat checks", "", "§eClick to open")
            .pdc(plugin, "assasin:gui_action", "OPEN_CATEGORY")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new CategoryGui(plugin, (Player) e.getWhoClicked(), "COMBAT"))));
    }

    private void populateUtilities() {
        setItem(20, new GuiItem(Material.BELL)
            .name("§eAlerts")
            .lore("§7Configure alert channels", "", "§eClick to configure")
            .pdc(plugin, "assasin:gui_action", "OPEN_ALERTS")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new AlertsToggleGui(plugin, (Player) e.getWhoClicked()))));

        setItem(21, new GuiItem(Material.COMPARATOR)
            .name("§bServer Stats")
            .lore("§7TPS, memory, CPU, flags", "", "§eClick to view")
            .pdc(plugin, "assasin:gui_action", "OPEN_STATS")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new ServerStatsGui(plugin, (Player) e.getWhoClicked()))));

        setItem(22, new GuiItem(Material.PAPER)
            .name("§fRecent Flags")
            .lore("§7Recent violations", "", "§eClick to view")
            .pdc(plugin, "assasin:gui_action", "OPEN_FLAGS")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new RecentFlagsGui(plugin, (Player) e.getWhoClicked()))));

        setItem(23, new GuiItem(Material.NETHER_STAR)
            .name("§6About")
            .lore("§7Plugin information", "", "§eClick to view")
            .enchantGlow()
            .pdc(plugin, "assasin:gui_action", "OPEN_ABOUT")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new AboutGui(plugin, (Player) e.getWhoClicked()))));
    }

    private void populateAdmin() {
        final boolean isAdmin = viewer.hasPermission(ADMIN_PERMISSION);

        if (isAdmin) {
            setItem(29, new GuiItem(Material.COMMAND_BLOCK)
                .name("§cCheck Manager")
                .lore("§7Manage all checks", "", "§eClick to open")
                .pdc(plugin, "assasin:gui_action", "OPEN_CHECK_MANAGER")
                .action(e -> plugin.getServiceContainer().getGuiManager()
                    .open((Player) e.getWhoClicked(), new CheckManagerGui(plugin, (Player) e.getWhoClicked(), 0))));

            setItem(30, new GuiItem(Material.REDSTONE)
                .name("§cBulk Actions")
                .lore("§7Mass actions", "", "§eClick to open")
                .pdc(plugin, "assasin:gui_action", "OPEN_BULK")
                .action(e -> plugin.getServiceContainer().getGuiManager()
                    .open((Player) e.getWhoClicked(), new BulkActionsGui(plugin, (Player) e.getWhoClicked()))));

            setItem(31, new GuiItem(Material.WRITABLE_BOOK)
                .name("§cAlert Format")
                .lore("§7Edit alert format", "", "§eClick to open")
                .pdc(plugin, "assasin:gui_action", "OPEN_ALERT_FORMAT")
                .action(e -> plugin.getServiceContainer().getGuiManager()
                    .open((Player) e.getWhoClicked(), new AlertFormatGui(plugin, (Player) e.getWhoClicked()))));
        } else {
            inventory.setItem(29, GuiBorder.item());
            inventory.setItem(30, GuiBorder.item());
            inventory.setItem(31, GuiBorder.item());
        }
    }
}
