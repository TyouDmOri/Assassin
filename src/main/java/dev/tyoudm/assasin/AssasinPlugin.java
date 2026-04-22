/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin;

import dev.tyoudm.assasin.core.ServiceContainer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for ASSASIN AntiCheat.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>{@link #onLoad()} — early init, config loading, service wiring</li>
 *   <li>{@link #onEnable()} — register listeners, commands, packet handlers</li>
 *   <li>{@link #onDisable()} — graceful shutdown, flush storage, close pools</li>
 * </ol>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class AssasinPlugin extends JavaPlugin {

    /** Singleton instance — accessible via {@link #getInstance()}. */
    private static AssasinPlugin instance;

    /** Root service container — owns all subsystem lifecycles. */
    private ServiceContainer serviceContainer;

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onLoad() {
        instance = this;

        // Save default configs so they exist before ServiceContainer reads them
        saveDefaultConfig();

        getLogger().info("ASSASIN is loading...");
    }

    @Override
    public void onEnable() {
        final long start = System.currentTimeMillis();

        // Bootstrap the service container — wires all subsystems in dependency order
        serviceContainer = new ServiceContainer(this);
        serviceContainer.enable();

        final long elapsed = System.currentTimeMillis() - start;
        getLogger().info(String.format(
            "ASSASIN v%s enabled in %dms — by TyouDm",
            getDescription().getVersion(), elapsed
        ));
    }

    @Override
    public void onDisable() {
        if (serviceContainer != null) {
            serviceContainer.disable();
        }

        getLogger().info("ASSASIN disabled. Goodbye.");
        instance = null;
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /**
     * Returns the singleton plugin instance.
     *
     * @return the {@link AssasinPlugin} instance
     * @throws IllegalStateException if called before {@link #onLoad()}
     */
    public static AssasinPlugin getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AssasinPlugin has not been loaded yet.");
        }
        return instance;
    }

    /**
     * Returns the root {@link ServiceContainer}.
     *
     * @return the service container
     */
    public ServiceContainer getServiceContainer() {
        return serviceContainer;
    }
}
