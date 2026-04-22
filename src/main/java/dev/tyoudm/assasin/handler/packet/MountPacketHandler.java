/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.handler.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSteerVehicle;
import dev.tyoudm.assasin.check.CheckProcessor;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.data.tracker.MountTracker;
import org.bukkit.entity.Player;

/**
 * Handles inbound mount steering packets ({@code STEER_VEHICLE}).
 *
 * <p>Feeds {@link MountTracker} with steering input for mount speed checks.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MountPacketHandler extends PacketListenerAbstract {

    private final PlayerDataManager dataManager;
    private final CheckProcessor    checkProcessor;

    public MountPacketHandler(final PlayerDataManager dataManager,
                              final CheckProcessor checkProcessor) {
        super(PacketListenerPriority.NORMAL);
        this.dataManager    = dataManager;
        this.checkProcessor = checkProcessor;
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.STEER_VEHICLE) return;

        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final MountTracker mt = data.getMountTracker();
        if (mt == null || !mt.isMounted()) return;

        final var wrapper = new WrapperPlayClientSteerVehicle(event);

        // Dismount request
        if (wrapper.isUnmount()) {
            mt.onDismount(data.getPacketCount());
            data.setInVehicle(false);
        }

        checkProcessor.processMount(player, data, player.getServer().getCurrentTick());
    }
}
