/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.core;

import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.alert.AlertManager;
import dev.tyoudm.assasin.alert.DiscordWebhook;
import dev.tyoudm.assasin.check.CheckProcessor;
import dev.tyoudm.assasin.check.CheckRegistry;
import dev.tyoudm.assasin.command.AssasinCommand;
import dev.tyoudm.assasin.config.ConfigManager;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.gui.GuiManager;
import dev.tyoudm.assasin.handler.HandlerManager;
import dev.tyoudm.assasin.handler.async.AsyncProcessor;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.storage.StorageFactory;
import dev.tyoudm.assasin.storage.StorageProvider;

/**
 * Root service container — wires all ASSASIN subsystems in dependency order.
 *
 * <p>Owns the lifecycle of every subsystem module. Modules are registered
 * into a {@link ModuleRegistry} and enabled/disabled in registration order
 * (LIFO for disable).
 *
 * <h2>Module registration order (FASE 2 + 3)</h2>
 * <ol>
 *   <li>{@code legitTechniques} — {@link LegitTechniqueRegistry}</li>
 *   <li>{@code playerData}      — {@link PlayerDataManager}</li>
 *   <li>{@code latency}         — latency subsystem (FASE 3, stateless module)</li>
 * </ol>
 * Further modules (handlers, checks, etc.) are added in later phases.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class ServiceContainer {

    private final AssasinPlugin        plugin;
    private final ModuleRegistry       moduleRegistry;

    // ─── Subsystem references (typed shortcuts) ───────────────────────────────

    private LegitTechniqueRegistry legitTechniqueRegistry;
    private PlayerDataManager      playerDataManager;
    private HandlerManager         handlerManager;
    private AsyncProcessor         asyncProcessor;
    private MitigationEngine       mitigationEngine;
    private CheckRegistry          checkRegistry;
    private CheckProcessor         checkProcessor;
    private StorageProvider        storageProvider;
    private AlertManager           alertManager;
    private DiscordWebhook         discordWebhook;
    private GuiManager             guiManager;
    private AssasinCommand         assasinCommand;
    private ConfigManager          configManager;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates the service container. Does not start any subsystems.
     *
     * @param plugin the owning plugin instance
     */
    public ServiceContainer(final AssasinPlugin plugin) {
        this.plugin         = plugin;
        this.moduleRegistry = new ModuleRegistry();
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    /**
     * Instantiates, registers, and enables all subsystems in dependency order.
     * Called from {@link AssasinPlugin#onEnable()}.
     */
    public void enable() {
        // ── FASE 18: Config (must be first — all subsystems read from it) ────
        configManager = new ConfigManager(plugin);
        moduleRegistry.register("config", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable()  {
                configManager.load();
                // Init debug logger from config
                final boolean debug = plugin.getConfig().getBoolean("general.debug", true);
                dev.tyoudm.assasin.check.CheckDebug.init(plugin.getLogger(), debug);
                if (debug) plugin.getLogger().info("[ASSASIN] Debug mode enabled — check flags will be logged to console.");
            }
            @Override public void onDisable() { /* stateless */ }
            @Override public String moduleName() { return "ConfigManager"; }
        });

        // ── FASE 2: Core subsystems ──────────────────────────────────────────

        legitTechniqueRegistry = new LegitTechniqueRegistry();
        moduleRegistry.register("legitTechniques", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable()  { /* stateless — no init needed */ }
            @Override public void onDisable() { /* stateless — no teardown needed */ }
            @Override public String moduleName() { return "LegitTechniqueRegistry"; }
        });

        playerDataManager = new PlayerDataManager();
        moduleRegistry.register("playerData", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable()  { plugin.getLogger().info("[ASSASIN] PlayerDataManager enabled."); }
            @Override public void onDisable() { playerDataManager.clear(); }
            @Override public String moduleName() { return "PlayerDataManager"; }
        });

        // ── FASE 3: Latency subsystem ────────────────────────────────────────
        // The latency subsystem is per-player (LatencyTracker lives inside
        // PlayerData). This module entry documents the dependency and logs
        // startup; actual LatencyTracker instances are created in
        // PlayerDataManager.create() via PlayerData.initLatencyTracker().
        moduleRegistry.register("latency", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable() {
                plugin.getLogger().info("[ASSASIN] Latency subsystem enabled "
                    + "(TransactionManager, BucketedPingHistory, LagCompensatedWorld, "
                    + "KnockbackValidator, TransactionBarrier).");
            }
            @Override public void onDisable() { /* per-player cleanup handled by PlayerDataManager */ }
            @Override public String moduleName() { return "LatencySubsystem"; }
        });

        // ── FASE 4 (cont.): AsyncProcessor ──────────────────────────────────
        asyncProcessor = new AsyncProcessor(plugin.getLogger());
        moduleRegistry.register("asyncProcessor", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable()  { plugin.getLogger().info("[ASSASIN] AsyncProcessor enabled."); }
            @Override public void onDisable() { asyncProcessor.shutdown(); }
            @Override public String moduleName() { return "AsyncProcessor"; }
        });

        // ── FASE 6: Mitigation Engine ────────────────────────────────────────
        mitigationEngine = new MitigationEngine(plugin, asyncProcessor);
        moduleRegistry.register("mitigation", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable()  { plugin.getLogger().info("[ASSASIN] MitigationEngine enabled."); }
            @Override public void onDisable() { /* stateless — no teardown needed */ }
            @Override public String moduleName() { return "MitigationEngine"; }
        });

        // ── Checks: Registry + Processor ────────────────────────────────────
        checkRegistry  = new CheckRegistry(mitigationEngine, legitTechniqueRegistry, asyncProcessor);
        checkProcessor = new CheckProcessor(checkRegistry);
        moduleRegistry.register("checks", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable() {
                plugin.getLogger().info(
                    "[ASSASIN] CheckRegistry enabled — " + checkRegistry.size() + " checks loaded.");
            }
            @Override public void onDisable() { /* stateless — checks are singletons */ }
            @Override public String moduleName() { return "CheckRegistry"; }
        });

        // ── FASE 4 (cont.): Handlers + Trackers ─────────────────────────────
        handlerManager = new HandlerManager(plugin, playerDataManager, checkProcessor);
        moduleRegistry.register("handlers", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable()  { handlerManager.enable(); }
            @Override public void onDisable() { handlerManager.disable(); }
            @Override public String moduleName() { return "HandlerManager"; }
        });

        // ── FASE 14: Storage ─────────────────────────────────────────────────
        storageProvider = StorageFactory.create(plugin, asyncProcessor::submit);
        moduleRegistry.register("storage", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable() {
                try {
                    storageProvider.init();
                    plugin.getLogger().info("[ASSASIN] Storage (" + storageProvider.getType() + ") enabled.");
                } catch (final Exception ex) {
                    plugin.getLogger().severe("[ASSASIN] Storage init failed: " + ex.getMessage());
                }
            }
            @Override public void onDisable() { storageProvider.close(); }
            @Override public String moduleName() { return "StorageProvider"; }
        });

        // ── FASE 15: Alert Manager + Discord ────────────────────────────────
        final String webhookUrl = plugin.getConfig().getString("alerts.discord.webhook", "");
        discordWebhook = new DiscordWebhook(webhookUrl, asyncProcessor, plugin.getLogger());
        alertManager   = new AlertManager(plugin, storageProvider, discordWebhook);
        mitigationEngine.setAlertManager(alertManager);
        moduleRegistry.register("alerts", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable()  { plugin.getLogger().info("[ASSASIN] AlertManager enabled."); }
            @Override public void onDisable() { /* stateless — no teardown needed */ }
            @Override public String moduleName() { return "AlertManager"; }
        });

        // ── FASE 16: GUI ─────────────────────────────────────────────────────
        guiManager = new GuiManager(plugin);
        moduleRegistry.register("gui", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable()  { plugin.getLogger().info("[ASSASIN] GuiManager enabled."); }
            @Override public void onDisable() { /* listeners auto-unregistered on plugin disable */ }
            @Override public String moduleName() { return "GuiManager"; }
        });

        // ── FASE 17: Commands ────────────────────────────────────────────────
        assasinCommand = new AssasinCommand(plugin);
        assasinCommand.register();
        moduleRegistry.register("commands", new ModuleRegistry.AssasinModule() {
            @Override public void onEnable()  { plugin.getLogger().info("[ASSASIN] Commands registered."); }
            @Override public void onDisable() { /* Brigadier commands are unregistered automatically */ }
            @Override public String moduleName() { return "AssasinCommand"; }
        });

        // ── Future phases register additional modules here ───────────────────
        // FASE 19: benchmarks
        // ...

        moduleRegistry.enableAll();
    }

    /**
     * Disables all subsystems in reverse registration order.
     * Called from {@link AssasinPlugin#onDisable()}.
     */
    public void disable() {
        moduleRegistry.disableAll();
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /**
     * Returns the {@link LegitTechniqueRegistry}.
     *
     * @return legit technique registry
     */
    public LegitTechniqueRegistry getLegitTechniqueRegistry() {
        return legitTechniqueRegistry;
    }

    /**
     * Returns the {@link PlayerDataManager}.
     *
     * @return player data manager
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public HandlerManager         getHandlerManager()         { return handlerManager; }
    public AsyncProcessor         getAsyncProcessor()         { return asyncProcessor; }
    public MitigationEngine       getMitigationEngine()       { return mitigationEngine; }
    public CheckRegistry          getCheckRegistry()          { return checkRegistry; }
    public CheckProcessor         getCheckProcessor()         { return checkProcessor; }
    public StorageProvider        getStorageProvider()        { return storageProvider; }
    public AlertManager           getAlertManager()           { return alertManager; }
    public DiscordWebhook         getDiscordWebhook()         { return discordWebhook; }
    public GuiManager             getGuiManager()             { return guiManager; }
    public AssasinCommand         getAssasinCommand()         { return assasinCommand; }
    public ConfigManager          getConfigManager()          { return configManager; }

    /**
     * Returns the underlying {@link ModuleRegistry} (for advanced use).
     *
     * @return module registry
     */
    public ModuleRegistry getModuleRegistry() {
        return moduleRegistry;
    }

    /**
     * Returns the owning {@link AssasinPlugin}.
     *
     * @return plugin instance
     */
    public AssasinPlugin getPlugin() {
        return plugin;
    }
}
