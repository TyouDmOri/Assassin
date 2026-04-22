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
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import dev.tyoudm.assasin.check.CheckProcessor;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.data.tracker.RotationTracker;
import org.bukkit.entity.Player;

/**
 * Handles inbound rotation packets ({@code PLAYER_ROTATION} and the
 * rotation component of {@code PLAYER_POSITION_AND_ROTATION}).
 *
 * <p>Updates {@link PlayerData} yaw/pitch and feeds {@link RotationTracker}
 * with delta history for aim and killaura checks.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class RotationPacketHandler extends PacketListenerAbstract {

    private final PlayerDataManager dataManager;
    private final CheckProcessor    checkProcessor;

    public RotationPacketHandler(final PlayerDataManager dataManager,
                                 final CheckProcessor checkProcessor) {
        super(PacketListenerPriority.NORMAL);
        this.dataManager    = dataManager;
        this.checkProcessor = checkProcessor;
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        final var type = event.getPacketType();

        if (type == PacketType.Play.Client.PLAYER_ROTATION) {
            handleRotation(event);
        } else if (type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            handlePositionAndRotation(event);
        }
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void handleRotation(final PacketReceiveEvent event) {
        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final var wrapper = new WrapperPlayClientPlayerRotation(event);
        applyRotation(data, player, wrapper.getYaw(), wrapper.getPitch());
    }

    private void handlePositionAndRotation(final PacketReceiveEvent event) {
        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final var wrapper = new WrapperPlayClientPlayerPositionAndRotation(event);
        applyRotation(data, player, wrapper.getYaw(), wrapper.getPitch());
    }

    private void applyRotation(final PlayerData data, final Player player,
                               final float yaw, final float pitch) {
        data.setRotation(yaw, pitch);

        final RotationTracker rt = data.getRotationTracker();
        if (rt != null) rt.update(yaw, pitch);

        checkProcessor.processRotation(player, data, player.getServer().getCurrentTick());
    }
}
