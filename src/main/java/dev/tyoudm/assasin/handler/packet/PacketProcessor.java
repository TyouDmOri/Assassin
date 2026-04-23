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
            // ... obtener data ...
        if (PacketType.Play.Client.isPosition(event.getPacketType())) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
        
            // Actualizar PlayerData con hilos protegidos
            data.setPosition(
                wrapper.getLocation().getX(), 
                wrapper.getLocation().getY(), 
                wrapper.getLocation().getZ()
            );
            data.setOnGround(wrapper.isOnGround());
        
            // Ejecutar colisiones (Main Thread es mejor para Bukkit API)
            Bukkit.getScheduler().runTask(AssasinPlugin.getInstance(), () -> {
                data.getCollisionTracker().handle((Player) event.getPlayer());
            });
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