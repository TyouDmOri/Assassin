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
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import dev.tyoudm.assasin.check.CheckProcessor;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.data.tracker.MovementTracker;
import dev.tyoudm.assasin.latency.LatencyTracker;
import org.bukkit.entity.Player;

/**
 * Handles inbound movement packets ({@code PLAYER_POSITION} and
 * {@code PLAYER_POSITION_AND_ROTATION}).
 *
 * <p>Updates {@link PlayerData} position, {@link MovementTracker} speed
 * history, and {@link LatencyTracker} lag-compensated world on every
 * movement packet received from the client.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MovementPacketHandler extends PacketListenerAbstract {

    private final PlayerDataManager dataManager;
    private final CheckProcessor    checkProcessor;

    public MovementPacketHandler(final PlayerDataManager dataManager,
                                 final CheckProcessor checkProcessor) {
        super(PacketListenerPriority.NORMAL);
        this.dataManager    = dataManager;
        this.checkProcessor = checkProcessor;
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        final var type = event.getPacketType();

        if (type == PacketType.Play.Client.PLAYER_POSITION) {
            handlePosition(event);
        } else if (type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            handlePositionAndRotation(event);
        }
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void handlePosition(final PacketReceiveEvent event) {
        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final var wrapper = new WrapperPlayClientPlayerPosition(event);
        final double nx = wrapper.getPosition().getX();
        final double ny = wrapper.getPosition().getY();
        final double nz = wrapper.getPosition().getZ();
        final boolean onGround = wrapper.isOnGround();

        updateMovement(player, data, nx, ny, nz, onGround);
    }

    private void handlePositionAndRotation(final PacketReceiveEvent event) {
        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final var wrapper = new WrapperPlayClientPlayerPositionAndRotation(event);
        final double nx = wrapper.getPosition().getX();
        final double ny = wrapper.getPosition().getY();
        final double nz = wrapper.getPosition().getZ();
        final boolean onGround = wrapper.isOnGround();

        updateMovement(player, data, nx, ny, nz, onGround);
    }

    private void updateMovement(final Player player, final PlayerData data,
                                final double nx, final double ny, final double nz,
                                final boolean onGround) {
        final double dx = nx - data.getX();
        final double dy = ny - data.getY();
        final double dz = nz - data.getZ();

        data.setPosition(nx, ny, nz);
        data.setOnGround(onGround);
        data.setVelocityH(Math.sqrt(dx * dx + dz * dz));
        data.setVelocityY(dy);
        data.incrementPackets();

        // Update movement tracker
        final MovementTracker mt = data.getMovementTracker();
        if (mt != null) mt.update(dx, dy, dz, onGround);

        // Update lag-compensated world
        final LatencyTracker lt = data.getLatencyTracker();
        if (lt != null) {
            final long tick = data.getPacketCount(); // approximate tick
            lt.onPositionPacket(tick, nx, ny, nz);
        }

        // Dispatch movement + player checks — use server tick for exempt comparisons
        final long serverTick = player.getServer().getCurrentTick();
        checkProcessor.processMovement(player, data, serverTick);
    }
}
