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

public final class ServerStatsGui extends AssasinGui {

    private final AssasinPlugin plugin;
    private final Player        viewer;

    public ServerStatsGui(final AssasinPlugin plugin, final Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        buildInventory();
    }

    @Override
    protected void buildInventory() {
        createInventory(4, Component.text("📊 ASSASIN — Server Stats")
            .decoration(TextDecoration.ITALIC, false));
        applyBorder();
        populateStats();
        populateNavigation();
    }

    private void populateStats() {
        final Runtime rt     = Runtime.getRuntime();
        final long usedMb    = (rt.totalMemory() - rt.freeMemory()) / 1_048_576;
        final long maxMb     = rt.maxMemory() / 1_048_576;
        final double tps     = plugin.getServer().getTPS()[0];

        setItem(10, new GuiItem(Material.COMPARATOR)
            .name("§aTPS")
            .lore(
                String.format("§71m: §a%.2f", tps),
                String.format("§75m: §a%.2f", plugin.getServer().getTPS()[1]),
                String.format("§715m: §a%.2f", plugin.getServer().getTPS()[2])
            ));

        setItem(11, new GuiItem(Material.ENDER_CHEST)
            .name("§bMemory")
            .lore(
                String.format("§7Used: §e%d MB", usedMb),
                String.format("§7Max: §e%d MB", maxMb),
                String.format("§7Free: §e%d MB", maxMb - usedMb)
            ));

        setItem(12, new GuiItem(Material.REDSTONE)
            .name("§eCPU")
            .lore(
                "§7Threads: §e" + Thread.activeCount(),
                "§7Uptime: §e" + formatUptime()
            ));

        setItem(13, new GuiItem(Material.CLOCK)
            .name("§cFlags 24h")
            .lore("§7Total: §cN/A"));

        setItem(14, new GuiItem(Material.BOOK)
            .name("§6Top Checks")
            .lore("§7N/A"));
    }

    private void populateNavigation() {
        setItem(28, new GuiItem(Material.ARROW)
            .name("§7Back")
            .lore("§7Return to main menu")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new MainGui(plugin, (Player) e.getWhoClicked()))));

        setItem(34, new GuiItem(Material.RECOVERY_COMPASS)
            .name("§aRefresh")
            .lore("§7Refresh stats")
            .action(e -> {
                final Player p = (Player) e.getWhoClicked();
                plugin.getServiceContainer().getGuiManager()
                    .open(p, new ServerStatsGui(plugin, p));
            }));
    }

    private static String formatUptime() {
        final long ms = System.currentTimeMillis();
        return String.format("%dh %dm %ds", ms / 3600000, ms / 60000 % 60, ms / 1000 % 60);
    }
}
