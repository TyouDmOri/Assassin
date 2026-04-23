package dev.tyoudm.assasin.handler.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import dev.tyoudm.assasin.Assassin;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.entity.Player;

public class PacketProcessor extends PacketListenerAbstract {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;

        PlayerData data = Assassin.getInstance().getDataManager().getData(player);
        if (data == null) return;

        long tick = Assassin.getInstance().getTick();

        // 1. Manejo de Movimiento
        if (PacketType.Play.Client.isPosition(event.getPacketType())) {
            data.getMovementTracker().handleReceive(event);
            data.getCollisionTracker().handle(player); // Actualizar colisiones
            data.getLatencyTracker().update(player);   // Actualizar ping
            
            Assassin.getInstance().getCheckManager().runMovementChecks(player, data, tick);
        }

        // 2. Manejo de Rotación y Combate
        if (PacketType.Play.Client.isLook(event.getPacketType())) {
            data.getRotationTracker().handleReceive(event);
            Assassin.getInstance().getCheckManager().runCombatChecks(player, data, tick);
        }

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            data.getCombatTracker().handleInteract(event);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;

        PlayerData data = Assassin.getInstance().getDataManager().getData(player);
        if (data == null) return;

        data.getAttributeTracker().handleSend(event);
        data.getVelocityTracker().handleSend(event);
    }
}