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
import dev.tyoudm.assasin.check.CheckProcessor;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.data.tracker.MountTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

/**
 * Handles mount/dismount events.
 *
 * <p>Updates {@link MountTracker} and applies/clears the
 * {@link ExemptType#VEHICLE} exemption for movement checks.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MountEventHandler implements Listener {

    private final AssasinPlugin     plugin;
    private final PlayerDataManager dataManager;
    private final CheckProcessor    checkProcessor;

    public MountEventHandler(final AssasinPlugin plugin,
                             final PlayerDataManager dataManager,
                             final CheckProcessor checkProcessor) {
        this.plugin          = plugin;
        this.dataManager     = dataManager;
        this.checkProcessor  = checkProcessor;
    }

    // ─── Mount ────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof final Player player)) return;

        final PlayerData data = dataManager.get(player);
        if (data == null) return;

        final Entity vehicle  = event.getVehicle();
        final long   tick     = plugin.getServer().getCurrentTick();

        final MountTracker mt = data.getMountTracker();
        if (mt != null) mt.onMount(vehicle.getUniqueId(), vehicle.getType(), tick);

        data.setInVehicle(true);
        data.getExemptManager().addPermanent(ExemptType.VEHICLE);

        checkProcessor.processMount(player, data, tick);
    }

    // ─── Dismount ─────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(final VehicleExitEvent event) {
        if (!(event.getExited() instanceof final Player player)) return;

        final PlayerData data = dataManager.get(player);
        if (data == null) return;

        final long tick = plugin.getServer().getCurrentTick();

        final MountTracker mt = data.getMountTracker();
        if (mt != null) mt.onDismount(tick);

        data.setInVehicle(false);
        data.getExemptManager().clear(ExemptType.VEHICLE);
    }
}
