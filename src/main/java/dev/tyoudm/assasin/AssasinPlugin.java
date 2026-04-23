/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin;

import com.github.retrooper.packetevents.PacketEvents;
import dev.tyoudm.assasin.core.ServiceContainer;
import dev.tyoudm.assasin.handler.packet.PacketProcessor;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for ASSASIN AntiCheat.
 */
public final class AssasinPlugin extends JavaPlugin {

    /** Singleton instance — accessible via {@link #getInstance()}. */
    private static AssasinPlugin instance;

    /** Root service container — owns all subsystem lifecycles. */
    private ServiceContainer serviceContainer;
    
    /** Global tick counter for all checks. */
    private long tick = 0;

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onLoad() {
        instance = this;

        // 1. Inicializar PacketEvents en onLoad (Crítico para la inyección de red)
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();

        // Save default configs
        saveDefaultConfig();

        getLogger().info("§cASSASIN §fis loading (1.21.11)...");
    }

    @Override
    public void onEnable() {
        final long start = System.currentTimeMillis();
        
        // 2. Iniciar PacketEvents API
        PacketEvents.getAPI().init();
        
        // 3. Registrar el Procesador de Paquetes
        PacketEvents.getAPI().getEventManager().registerListener(new PacketProcessor());
        
        // 4. Bootstrap the service container (Asegúrate de que tus Managers se inicien aquí)
        serviceContainer = new ServiceContainer(this);
        serviceContainer.enable();

        // 5. Motor de Ticks (20 TPS) - Vital para Velocity y Exenciones
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            tick++;
            // Actualizamos trackers que dependen del tiempo real del servidor
            serviceContainer.getDataManager().getPlayerDataMap().values().forEach(data -> {
                if (data.getVelocityTracker() != null) data.getVelocityTracker().tick();
                if (data.getExemptManager() != null) data.getExemptManager().tick();
            });
        }, 1L, 1L);

        final long elapsed = System.currentTimeMillis() - start;
        getLogger().info(String.format(
            "§cASSASIN §fv%s §7enabled in §a%dms §7— by TyouDm",
            getDescription().getVersion(), elapsed
        ));
        getCommand("assasin").setExecutor(new AssassinCommand());
    }

    @Override
    public void onDisable() {
        // 6. Apagado elegante de PacketEvents
        PacketEvents.getAPI().terminate();

        if (serviceContainer != null) {
            serviceContainer.disable();
        }

        getLogger().info("§cASSASIN §fdisabled. Goodbye.");
        instance = null;
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public static AssasinPlugin getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AssasinPlugin has not been loaded yet.");
        }
        return instance;
    }

    public ServiceContainer getServiceContainer() {
        return serviceContainer;
    }

    public long getTick() {
        return tick;
    }
}