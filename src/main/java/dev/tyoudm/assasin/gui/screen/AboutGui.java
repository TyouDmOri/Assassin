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

public final class AboutGui extends AssasinGui {

    private final AssasinPlugin plugin;
    private final Player        viewer;

    public AboutGui(final AssasinPlugin plugin, final Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        buildInventory();
    }

    @Override
    protected void buildInventory() {
        createInventory(3, Component.text("⭐ ASSASIN — About")
            .decoration(TextDecoration.ITALIC, false));
        applyBorder();

        setItem(13, new GuiItem(Material.NETHER_STAR)
            .name("§6§lASSASSIN")
            .lore(
                "§fAnticheat for Minecraft 1.21.11",
                "",
                "§7Version: §a" + plugin.getDescription().getVersion(),
                "§7Author: §bTyouDm",
                "",
                "§aThanks for using ASSASIN!"
            )
            .enchantGlow()
            .pdc(plugin, "assasin:gui_action", "NONE"));

        setItem(19, new GuiItem(Material.ARROW)
            .name("§7Back")
            .lore("§7Return to main menu")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(), new MainGui(plugin, (Player) e.getWhoClicked()))));
    }
}
