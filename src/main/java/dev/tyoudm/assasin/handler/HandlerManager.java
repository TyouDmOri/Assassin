/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.handler;

import com.github.retrooper.packetevents.PacketEvents;
import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.check.CheckProcessor;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.handler.event.CombatEventHandler;
import dev.tyoudm.assasin.handler.event.MountEventHandler;
import dev.tyoudm.assasin.handler.event.PlayerEventHandler;
import dev.tyoudm.assasin.handler.event.WorldEventHandler;
import dev.tyoudm.assasin.handler.packet.BlockPacketHandler;
import dev.tyoudm.assasin.handler.packet.CombatPacketHandler;
import dev.tyoudm.assasin.handler.packet.InventoryPacketHandler;
import dev.tyoudm.assasin.handler.packet.KeepAlivePacketHandler;
import dev.tyoudm.assasin.handler.packet.MountPacketHandler;
import dev.tyoudm.assasin.handler.packet.MovementPacketHandler;
import dev.tyoudm.assasin.handler.packet.RotationPacketHandler;
import dev.tyoudm.assasin.handler.packet.TransactionPacketHandler;
import dev.tyoudm.assasin.handler.packet.VelocityPacketHandler;
import org.bukkit.plugin.PluginManager;

/**
 * Central registration point for all ASSASIN packet and event handlers.
 *
 * <p>Instantiates and registers every handler in the correct order:
 * <ol>
 *   <li>PacketEvents listeners (netty pipeline, inbound + outbound)</li>
 *   <li>Bukkit event listeners (main thread)</li>
 * </ol>
 *
 * <p>All handlers receive a reference to {@link PlayerDataManager} so they
 * can look up the per-player {@link dev.tyoudm.assasin.data.PlayerData}
 * without going through the plugin singleton.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class HandlerManager {

    private final AssasinPlugin     plugin;
    private final PlayerDataManager dataManager;
    private final CheckProcessor    checkProcessor;

    // ─── Packet handlers ──────────────────────────────────────────────────────

    private MovementPacketHandler    movementPacketHandler;
    private RotationPacketHandler    rotationPacketHandler;
    private CombatPacketHandler      combatPacketHandler;
    private BlockPacketHandler       blockPacketHandler;
    private MountPacketHandler       mountPacketHandler;
    private KeepAlivePacketHandler   keepAlivePacketHandler;
    private TransactionPacketHandler transactionPacketHandler;
    private InventoryPacketHandler   inventoryPacketHandler;
    private VelocityPacketHandler    velocityPacketHandler;

    // ─── Event handlers ───────────────────────────────────────────────────────

    private PlayerEventHandler playerEventHandler;
    private CombatEventHandler combatEventHandler;
    private WorldEventHandler  worldEventHandler;
    private MountEventHandler  mountEventHandler;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates the handler manager.
     *
     * @param plugin         the owning plugin
     * @param dataManager    the player data manager
     * @param checkProcessor the check processor
     */
    public HandlerManager(final AssasinPlugin plugin,
                          final PlayerDataManager dataManager,
                          final CheckProcessor checkProcessor) {
        this.plugin          = plugin;
        this.dataManager     = dataManager;
        this.checkProcessor  = checkProcessor;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    /**
     * Registers all packet and event handlers.
     * Called from {@link dev.tyoudm.assasin.core.ServiceContainer#enable()}.
     */
    public void enable() {
        registerPacketHandlers();
        registerEventHandlers();
        plugin.getLogger().info("[ASSASIN] HandlerManager: all handlers registered.");
    }

    /**
     * Unregisters all handlers and releases resources.
     * Called from {@link dev.tyoudm.assasin.core.ServiceContainer#disable()}.
     */
    public void disable() {
        if (PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().getEventManager().unregisterAllListeners();
        }
        plugin.getLogger().info("[ASSASIN] HandlerManager: all handlers unregistered.");
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void registerPacketHandlers() {
        movementPacketHandler    = new MovementPacketHandler(dataManager, checkProcessor);
        rotationPacketHandler    = new RotationPacketHandler(dataManager, checkProcessor);
        combatPacketHandler      = new CombatPacketHandler(dataManager, checkProcessor);
        blockPacketHandler       = new BlockPacketHandler(dataManager, checkProcessor);
        mountPacketHandler       = new MountPacketHandler(dataManager, checkProcessor);
        keepAlivePacketHandler   = new KeepAlivePacketHandler(dataManager);
        transactionPacketHandler = new TransactionPacketHandler(dataManager);
        inventoryPacketHandler   = new InventoryPacketHandler(dataManager, checkProcessor);
        velocityPacketHandler    = new VelocityPacketHandler(dataManager);

        final var eventManager = PacketEvents.getAPI().getEventManager();
        eventManager.registerListener(movementPacketHandler);
        eventManager.registerListener(rotationPacketHandler);
        eventManager.registerListener(combatPacketHandler);
        eventManager.registerListener(blockPacketHandler);
        eventManager.registerListener(mountPacketHandler);
        eventManager.registerListener(keepAlivePacketHandler);
        eventManager.registerListener(transactionPacketHandler);
        eventManager.registerListener(inventoryPacketHandler);
        eventManager.registerListener(velocityPacketHandler);
    }

    private void registerEventHandlers() {
        final PluginManager pm = plugin.getServer().getPluginManager();

        playerEventHandler = new PlayerEventHandler(plugin, dataManager);
        combatEventHandler = new CombatEventHandler(plugin, dataManager, checkProcessor);
        worldEventHandler  = new WorldEventHandler(plugin, dataManager, checkProcessor);
        mountEventHandler  = new MountEventHandler(plugin, dataManager, checkProcessor);

        pm.registerEvents(playerEventHandler, plugin);
        pm.registerEvents(combatEventHandler, plugin);
        pm.registerEvents(worldEventHandler,  plugin);
        pm.registerEvents(mountEventHandler,  plugin);
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public MovementPacketHandler    getMovementPacketHandler()    { return movementPacketHandler; }
    public RotationPacketHandler    getRotationPacketHandler()    { return rotationPacketHandler; }
    public CombatPacketHandler      getCombatPacketHandler()      { return combatPacketHandler; }
    public BlockPacketHandler       getBlockPacketHandler()       { return blockPacketHandler; }
    public MountPacketHandler       getMountPacketHandler()       { return mountPacketHandler; }
    public KeepAlivePacketHandler   getKeepAlivePacketHandler()   { return keepAlivePacketHandler; }
    public TransactionPacketHandler getTransactionPacketHandler() { return transactionPacketHandler; }
    public InventoryPacketHandler   getInventoryPacketHandler()   { return inventoryPacketHandler; }
    public VelocityPacketHandler    getVelocityPacketHandler()    { return velocityPacketHandler; }
    public PlayerEventHandler       getPlayerEventHandler()       { return playerEventHandler; }
    public CombatEventHandler       getCombatEventHandler()       { return combatEventHandler; }
    public WorldEventHandler        getWorldEventHandler()        { return worldEventHandler; }
    public MountEventHandler        getMountEventHandler()        { return mountEventHandler; }
}
