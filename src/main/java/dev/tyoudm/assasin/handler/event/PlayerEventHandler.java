/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.handler.event;

import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.exempt.ExemptManager;
import dev.tyoudm.assasin.exempt.ExemptType;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Handles player lifecycle events: join, quit, respawn, teleport, world change,
 * and gamemode change.
 *
 * <p>Responsible for creating/removing {@link PlayerData} entries and
 * applying the appropriate exemptions on state transitions.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class PlayerEventHandler implements Listener {

    /** Ticks to suppress checks after join/respawn. */
    private static final int RESPAWN_EXEMPT_TICKS = 20;

    /** Ticks to suppress movement checks after teleport. */
    private static final int TELEPORT_EXEMPT_TICKS = 5;

    private final AssasinPlugin     plugin;
    private final PlayerDataManager dataManager;

    public PlayerEventHandler(final AssasinPlugin plugin, final PlayerDataManager dataManager) {
        this.plugin      = plugin;
        this.dataManager = dataManager;
    }

    // ─── Join / Quit ──────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player   = event.getPlayer();
        final long   joinTick = plugin.getServer().getCurrentTick();

        final PlayerData data = dataManager.create(player, joinTick);
        data.initLatencyTracker();
        data.initTrackers();

        // Suppress all checks for the first 20 ticks
        data.getExemptManager().add(ExemptType.RESPAWN, joinTick, RESPAWN_EXEMPT_TICKS);

        // Apply gamemode exempt if not survival/adventure
        applyGamemodeExempt(data, player.getGameMode(), joinTick);

        // Update ping every 20 ticks (1 second) while player is online
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) return;
            final PlayerData d = dataManager.get(player.getUniqueId());
            if (d != null) d.setPing(player.getPing());
        }, 20L, 20L);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(final PlayerQuitEvent event) {
        dataManager.remove(event.getPlayer().getUniqueId());
    }

    // ─── Respawn ──────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOW)
    public void onRespawn(final PlayerRespawnEvent event) {
        final Player     player = event.getPlayer();
        final PlayerData data   = dataManager.get(player);
        if (data == null) return;

        final long tick = plugin.getServer().getCurrentTick();

        // Reset all trackers
        resetTrackers(data);

        // Suppress checks for 20 ticks post-respawn
        data.getExemptManager().add(ExemptType.RESPAWN, tick, RESPAWN_EXEMPT_TICKS);
        data.getExemptManager().clear(ExemptType.DEAD);
    }

    // ─── Teleport ─────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent event) {
        final Player     player = event.getPlayer();
        final PlayerData data   = dataManager.get(player);
        if (data == null) return;

        final long tick = plugin.getServer().getCurrentTick();
        data.getExemptManager().add(ExemptType.TELEPORT_PENDING, tick, TELEPORT_EXEMPT_TICKS);

        // Reset latency tracker to avoid stale position history
        if (data.getLatencyTracker() != null) {
            data.getLatencyTracker().reset();
        }
    }

    // ─── World change ─────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOW)
    public void onWorldChange(final PlayerChangedWorldEvent event) {
        final Player     player = event.getPlayer();
        final PlayerData data   = dataManager.get(player);
        if (data == null) return;

        final long tick = plugin.getServer().getCurrentTick();
        data.getExemptManager().add(ExemptType.WORLD_CHANGE, tick, RESPAWN_EXEMPT_TICKS);
        resetTrackers(data);
    }

    // ─── Gamemode ─────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGamemodeChange(final PlayerGameModeChangeEvent event) {
        final Player     player = event.getPlayer();
        final PlayerData data   = dataManager.get(player);
        if (data == null) return;

        final long tick = plugin.getServer().getCurrentTick();
        applyGamemodeExempt(data, event.getNewGameMode(), tick);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void applyGamemodeExempt(final PlayerData data,
                                     final GameMode gm, final long tick) {
        final ExemptManager em = data.getExemptManager();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) {
            em.addPermanent(ExemptType.GAMEMODE);
            data.setFlying(true);
        } else {
            em.clear(ExemptType.GAMEMODE);
            data.setFlying(false);
        }
    }

    private void resetTrackers(final PlayerData data) {
        if (data.getMovementTracker()  != null) data.getMovementTracker().reset();
        if (data.getRotationTracker()  != null) data.getRotationTracker().reset();
        if (data.getCombatTracker()    != null) data.getCombatTracker().reset();
        if (data.getVelocityTracker()  != null) data.getVelocityTracker().reset();
        if (data.getBlockTracker()     != null) data.getBlockTracker().reset();
        if (data.getMountTracker()     != null) data.getMountTracker().reset();
        if (data.getAttackTracker()    != null) data.getAttackTracker().reset();
        if (data.getInputTracker()     != null) data.getInputTracker().reset();
        if (data.getInventoryTracker() != null) data.getInventoryTracker().reset();
        if (data.getActionTracker()    != null) data.getActionTracker().reset();
        if (data.getMacroStateTracker()!= null) data.getMacroStateTracker().reset();
        if (data.getLatencyTracker()   != null) data.getLatencyTracker().reset();
        if (data.getElytraPredictor()  != null) data.getElytraPredictor().reset();
    }
}
