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

        // 1. Actualizar Trackers con paquetes de entrada (Movimiento, Rotación)
        if (PacketType.Play.Client.isPosition(event.getPacketType())) {
            data.getMovementTracker().handleReceive(event);
            // Ejecutar Checks de Movimiento después de actualizar datos
            Assassin.getInstance().getCheckManager().runMovementChecks(player, data);
        } 
        
        if (PacketType.Play.Client.isLook(event.getPacketType())) {
            data.getRotationTracker().handleReceive(event);
            // Ejecutar Checks de Combate/Rotación
            Assassin.getInstance().getCheckManager().runCombatChecks(player, data);
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

        // Actualizar Atributos y Velocidad (1.21.11)
        data.getAttributeTracker().handleSend(event);
        data.getVelocityTracker().handleSend(event);
    }
}