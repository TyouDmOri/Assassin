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
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.data.tracker.VelocityTracker;
import dev.tyoudm.assasin.latency.LatencyTracker;
import org.bukkit.entity.Player;

/**
 * Handles outbound velocity packets ({@code ENTITY_VELOCITY} sent to the client).
 *
 * <p>When the server sends a velocity packet targeting the player themselves,
 * the expected vector is recorded in {@link VelocityTracker} and
 * {@link LatencyTracker} for use by {@code VelocityA/B/C}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class VelocityPacketHandler extends PacketListenerAbstract {

    private final PlayerDataManager dataManager;

    public VelocityPacketHandler(final PlayerDataManager dataManager) {
        super(PacketListenerPriority.NORMAL);
        this.dataManager = dataManager;
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_VELOCITY) return;

        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final var wrapper = new WrapperPlayServerEntityVelocity(event);

        // Only process velocity packets targeting this player
        if (wrapper.getEntityId() != player.getEntityId()) return;

        // PacketEvents uses 1/8000 units; convert to blocks/tick
        final Vector3d velocity = wrapper.getVelocity();
        final double velX = velocity.x / 8000.0;
        final double velY = velocity.y / 8000.0;
        final double velZ = velocity.z / 8000.0;
        final long   tick = data.getPacketCount();

        final VelocityTracker vt = data.getVelocityTracker();
        if (vt != null) vt.recordPending(velX, velY, velZ, tick);

        final LatencyTracker lt = data.getLatencyTracker();
        if (lt != null) lt.onVelocitySent(velX, velY, velZ, tick);
    }
}
