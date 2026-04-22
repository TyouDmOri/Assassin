/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed accessor for {@code alerts.yml}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class AlertConfig {

    private final FileConfiguration cfg;

    public AlertConfig(final FileConfiguration cfg) {
        this.cfg = cfg;
    }

    public String  getChatFormat()          { return cfg.getString("format.chat", "{player} failed {check} (VL: {vl})"); }
    public String  getActionBarFormat()     { return cfg.getString("format.action-bar", "{player} » {check} VL:{vl}"); }
    public String  getTitleFormat()         { return cfg.getString("format.title", "{player} » {check}"); }
    public String  getSubtitleFormat()      { return cfg.getString("format.subtitle", "VL: {vl} | ping: {ping}ms"); }
    public String  getDiscordWebhook()      { return cfg.getString("discord.webhook", ""); }
    public double  getMinVlForDiscord()     { return cfg.getDouble("discord.min-vl-for-discord", 5.0); }
    public String  getAlertSound()          { return cfg.getString("sounds.alert", "UI_BUTTON_CLICK"); }
    public float   getAlertVolume()         { return (float) cfg.getDouble("sounds.alert-volume", 0.5); }
    public String  getToggleOnSound()       { return cfg.getString("sounds.toggle-on", "BLOCK_NOTE_BLOCK_PLING"); }
    public String  getToggleOffSound()      { return cfg.getString("sounds.toggle-off", "BLOCK_NOTE_BLOCK_BASS"); }
    public boolean isDefaultChatEnabled()   { return cfg.getBoolean("default-channels.chat", true); }
    public boolean isDefaultActionBar()     { return cfg.getBoolean("default-channels.action-bar", true); }
    public boolean isDefaultTitle()         { return cfg.getBoolean("default-channels.title", false); }
    public boolean isDefaultSound()         { return cfg.getBoolean("default-channels.sound", true); }
    public boolean isDefaultDiscord()       { return cfg.getBoolean("default-channels.discord", false); }
}
