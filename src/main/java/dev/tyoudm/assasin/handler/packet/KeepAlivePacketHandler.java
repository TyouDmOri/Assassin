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
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.latency.LatencyTracker;
import org.bukkit.entity.Player;

/**
 * Handles inbound keep-alive packets ({@code KEEP_ALIVE}).
 *
 * <p>Forwards the keep-alive ID to {@link LatencyTracker} as a transaction
 * confirmation to measure RTT.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class KeepAlivePacketHandler extends PacketListenerAbstract {

    private final PlayerDataManager dataManager;

    public KeepAlivePacketHandler(final PlayerDataManager dataManager) {
        super(PacketListenerPriority.NORMAL);
        this.dataManager = dataManager;
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.KEEP_ALIVE) return;

        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final var wrapper = new WrapperPlayClientKeepAlive(event);
        final long id = wrapper.getId();

        final LatencyTracker lt = data.getLatencyTracker();
        if (lt != null) {
            // Keep-alive IDs are longs; cast to short for transaction matching
            lt.onTransactionConfirmed((short) id, data.getPacketCount());
        }
    }
}
