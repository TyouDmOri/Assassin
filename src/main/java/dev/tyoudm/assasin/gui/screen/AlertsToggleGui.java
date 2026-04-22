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
import dev.tyoudm.assasin.storage.model.AlertPreference;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class AlertsToggleGui extends AssasinGui {

    private final AssasinPlugin plugin;
    private final Player        viewer;
    private int channelBitmask;

    public AlertsToggleGui(final AssasinPlugin plugin, final Player viewer) {
        this.plugin         = plugin;
        this.viewer         = viewer;
        this.channelBitmask = AlertPreference.DEFAULT_CHANNELS;
        buildInventory();
    }

    @Override
    protected void buildInventory() {
        createInventory(3, Component.text("🔔 ASSASIN — Configure Alerts")
            .decoration(TextDecoration.ITALIC, false));
        applyBorder();
        populateChannels();
        populateNavigation();
    }

    private void populateChannels() {
        buildChannelItem(10, AlertPreference.CHANNEL_CHAT,       Material.GREEN_DYE,  Material.RED_DYE,  "§fChat",       "§7Alerts in chat",           "TOGGLE_ALERT_CHAT");
        buildChannelItem(11, AlertPreference.CHANNEL_ACTION_BAR, Material.GREEN_DYE,  Material.RED_DYE,  "§fAction Bar", "§7Alerts in action bar",     "TOGGLE_ALERT_ACTIONBAR");
        buildChannelItem(12, AlertPreference.CHANNEL_TITLE,      Material.GREEN_DYE,  Material.RED_DYE,  "§fTitle",      "§7Alerts as title",          "TOGGLE_ALERT_TITLE");
        buildChannelItem(13, AlertPreference.CHANNEL_SOUND,      Material.NOTE_BLOCK, Material.RED_DYE,  "§fSound",      "§7Sound on alert receive",   "TOGGLE_ALERT_SOUND");
        buildChannelItem(14, AlertPreference.CHANNEL_DISCORD,    Material.PAPER,      Material.RED_DYE,  "§fDiscord",    "§7Alerts via Discord webhook","TOGGLE_ALERT_DISCORD");
    }

    private void buildChannelItem(final int slot, final int channel,
                                   final Material onMat, final Material offMat,
                                   final String name, final String desc,
                                   final String action) {
        final boolean enabled = (channelBitmask & channel) != 0;
        final Material mat    = enabled ? onMat : offMat;
        final String state    = enabled ? "§aENABLED" : "§cDISABLED";

        setItem(slot, new GuiItem(mat)
            .name(name)
            .lore(desc, "", state, "", "§eClick to toggle")
            .pdc(plugin, "assasin:gui_action", action)
            .action(e -> {
                final Player p = (Player) e.getWhoClicked();
                final boolean nowEnabled = (channelBitmask & channel) != 0;
                if (nowEnabled) {
                    channelBitmask &= ~channel;
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.8f);
                } else {
                    channelBitmask |= channel;
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.2f);
                }
                plugin.getServiceContainer().getAlertManager()
                    .setPreference(p.getUniqueId(), "*", true, channelBitmask);
                buildChannelItem(slot, channel, onMat, offMat, name, desc, action);
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
