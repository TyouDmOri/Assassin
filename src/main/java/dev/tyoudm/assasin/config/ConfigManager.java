/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.config;

import dev.tyoudm.assasin.AssasinPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Hot-reloadable configuration manager for ASSASIN.
 *
 * <p>Manages all YAML configuration files, copies defaults from the JAR
 * on first run, and provides typed sub-config accessors.
 *
 * <h2>Config files managed</h2>
 * <ul>
 *   <li>{@code config.yml}           — general, storage, threads, flags</li>
 *   <li>{@code checks.yml}           — per-check toggle + thresholds</li>
 *   <li>{@code mitigation.yml}       — profiles + VL cascades</li>
 *   <li>{@code latency.yml}          — ping compensation settings</li>
 *   <li>{@code alerts.yml}           — alert formats, webhook, sounds</li>
 *   <li>{@code messages.yml}         — i18n-ready user-facing strings</li>
 *   <li>{@code legit-techniques.yml} — legit PvP technique tolerances</li>
 *   <li>{@code macro.yml}            — macro detection strictness</li>
 *   <li>{@code gui.yml}              — GUI layout and materials</li>
 * </ul>
 *
 * <h2>Hot-reload</h2>
 * Call {@link #reload()} to reload all configs at runtime.
 * Subsystems that depend on config values should re-read them after reload.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class ConfigManager {

    private final AssasinPlugin plugin;
    private final Logger        logger;
    private final File          dataFolder;

    // ─── Loaded configs ───────────────────────────────────────────────────────

    private FileConfiguration checksConfig;
    private FileConfiguration mitigationConfig;
    private FileConfiguration latencyConfig;
    private FileConfiguration alertsConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration legitConfig;
    private FileConfiguration macroConfig;
    private FileConfiguration guiConfig;

    // ─── Typed sub-configs ────────────────────────────────────────────────────

    private CheckConfig      checkConfig;
    private DatabaseConfig   databaseConfig;
    private AlertConfig      alertConfig;
    private LatencyConfig    latencyConfigObj;
    private LegitConfig      legitConfigObj;
    private MacroConfig      macroConfigObj;
    private MitigationConfig mitigationConfigObj;
    private MessagesConfig   messagesConfigObj;
    private GuiConfig        guiConfigObj;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public ConfigManager(final AssasinPlugin plugin) {
        this.plugin     = plugin;
        this.logger     = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder();
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    /**
     * Loads all configuration files, copying defaults if they don't exist.
     */
    public void load() {
        dataFolder.mkdirs();

        // Main config.yml is handled by Bukkit's saveDefaultConfig()
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        checksConfig     = loadConfig("checks.yml");
        mitigationConfig = loadConfig("mitigation.yml");
        latencyConfig    = loadConfig("latency.yml");
        alertsConfig     = loadConfig("alerts.yml");
        messagesConfig   = loadConfig("messages.yml");
        legitConfig      = loadConfig("legit-techniques.yml");
        macroConfig      = loadConfig("macro.yml");
        guiConfig        = loadConfig("gui.yml");

        buildSubConfigs();
        logger.info("[ASSASIN] All configuration files loaded.");
    }

    /**
     * Reloads all configuration files from disk.
     */
    public void reload() {
        plugin.reloadConfig();
        checksConfig     = loadConfig("checks.yml");
        mitigationConfig = loadConfig("mitigation.yml");
        latencyConfig    = loadConfig("latency.yml");
        alertsConfig     = loadConfig("alerts.yml");
        messagesConfig   = loadConfig("messages.yml");
        legitConfig      = loadConfig("legit-techniques.yml");
        macroConfig      = loadConfig("macro.yml");
        guiConfig        = loadConfig("gui.yml");
        buildSubConfigs();
        logger.info("[ASSASIN] Configuration reloaded.");
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private FileConfiguration loadConfig(final String fileName) {
        final File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        final YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        // Merge defaults from JAR
        final InputStream defStream = plugin.getResource(fileName);
        if (defStream != null) {
            final YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defStream, StandardCharsets.UTF_8));
            cfg.setDefaults(defaults);
        }
        return cfg;
    }

    private void buildSubConfigs() {
        checkConfig         = new CheckConfig(checksConfig);
        databaseConfig      = new DatabaseConfig(plugin.getConfig());
        alertConfig         = new AlertConfig(alertsConfig);
        latencyConfigObj    = new LatencyConfig(latencyConfig);
        legitConfigObj      = new LegitConfig(legitConfig);
        macroConfigObj      = new MacroConfig(macroConfig);
        mitigationConfigObj = new MitigationConfig(mitigationConfig);
        messagesConfigObj   = new MessagesConfig(messagesConfig);
        guiConfigObj        = new GuiConfig(guiConfig);
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public FileConfiguration getRawChecksConfig()     { return checksConfig; }
    public FileConfiguration getRawMitigationConfig() { return mitigationConfig; }
    public FileConfiguration getRawLatencyConfig()    { return latencyConfig; }
    public FileConfiguration getRawAlertsConfig()     { return alertsConfig; }
    public FileConfiguration getRawMessagesConfig()   { return messagesConfig; }
    public FileConfiguration getRawLegitConfig()      { return legitConfig; }
    public FileConfiguration getRawMacroConfig()      { return macroConfig; }
    public FileConfiguration getRawGuiConfig()        { return guiConfig; }

    public CheckConfig      getCheckConfig()          { return checkConfig; }
    public DatabaseConfig   getDatabaseConfig()       { return databaseConfig; }
    public AlertConfig      getAlertConfig()          { return alertConfig; }
    public LatencyConfig    getLatencyConfig()        { return latencyConfigObj; }
    public LegitConfig      getLegitConfig()          { return legitConfigObj; }
    public MacroConfig      getMacroConfig()          { return macroConfigObj; }
    public MitigationConfig getMitigationConfig()     { return mitigationConfigObj; }
    public MessagesConfig   getMessagesConfig()       { return messagesConfigObj; }
    public GuiConfig        getGuiConfig()            { return guiConfigObj; }
}
