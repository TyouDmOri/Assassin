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
import dev.tyoudm.assasin.storage.model.ViolationRecord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class RecentFlagsGui extends AssasinGui {

    private static final int[] FLAG_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34
    };

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("HH:mm:ss");

    private final AssasinPlugin plugin;
    private final Player        viewer;

    public RecentFlagsGui(final AssasinPlugin plugin, final Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        buildInventory();
    }

    @Override
    protected void buildInventory() {
        createInventory(5, Component.text("📋 ASSASIN — Recent Flags")
            .decoration(TextDecoration.ITALIC, false));
        applyBorder();
        loadFlags();
        populateNavigation();
    }

    private void loadFlags() {
        plugin.getServiceContainer().getStorageProvider()
            .getViolations(viewer.getUniqueId(), FLAG_SLOTS.length)
            .thenAccept(violations -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (int i = 0; i < Math.min(violations.size(), FLAG_SLOTS.length); i++) {
                    setFlagItem(FLAG_SLOTS[i], violations.get(i));
                }
            }));
    }

    private void setFlagItem(final int slot, final ViolationRecord v) {
        final Material mat = v.violationLevel() >= 40 ? Material.RED_WOOL
            : v.violationLevel() >= 20 ? Material.ORANGE_WOOL
            : Material.YELLOW_WOOL;
        final String color = v.violationLevel() >= 40 ? "§c"
            : v.violationLevel() >= 20 ? "§6" : "§e";
        final String time = DATE_FMT.format(new Date(v.timestampMs()));

        setItem(slot, new GuiItem(mat)
            .name(color + v.playerUuid().toString().substring(0, 8) + "...")
            .lore(
                "§7Check: §e" + v.checkName(),
                "§7VL: §c" + String.format("%.2f", v.violationLevel()),
                "§7Ping: §f" + v.pingMs() + "ms",
                "§7Time: §7" + time,
                "",
                "§eClick: Teleport to player",
                "§bRight-click: Ban player"
            )
            .pdc(plugin, "assasin:gui_action", "FLAG_TELEPORT")
            .action(e -> ((Player) e.getWhoClicked())
                .sendMessage("§a[ASSASIN] Teleporting to " + v.playerUuid())));
    }

    private void populateNavigation() {
        setItem(37, new GuiItem(Material.ARROW)
            .name("§7Back")
            .lore("§7Return to main menu")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new MainGui(plugin, (Player) e.getWhoClicked()))));

        setItem(43, new GuiItem(Material.BARRIER)
            .name("§cClear History")
            .lore("§7Delete all flag history", "", "§cClick to clear")
            .action(e -> ((Player) e.getWhoClicked())
                .sendMessage("§a[ASSASIN] History cleared.")));
    }
}
