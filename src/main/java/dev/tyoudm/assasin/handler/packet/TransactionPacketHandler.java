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
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.latency.LatencyTracker;
import org.bukkit.entity.Player;

/**
 * Handles inbound transaction / window-confirmation packets
 * ({@code WINDOW_CONFIRMATION}).
 *
 * <p>Forwards the action number to {@link LatencyTracker} as a transaction
 * confirmation, closing any open {@link dev.tyoudm.assasin.latency.TransactionBarrier}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class TransactionPacketHandler extends PacketListenerAbstract {

    private final PlayerDataManager dataManager;

    public TransactionPacketHandler(final PlayerDataManager dataManager) {
        super(PacketListenerPriority.NORMAL);
        this.dataManager = dataManager;
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.WINDOW_CONFIRMATION) return;

        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final var wrapper = new WrapperPlayClientWindowConfirmation(event);
        final short actionNumber = wrapper.getActionId();

        final LatencyTracker lt = data.getLatencyTracker();
        if (lt != null) {
            lt.onTransactionConfirmed(actionNumber, data.getPacketCount());
        }
    }
}
