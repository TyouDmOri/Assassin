package dev.tyoudm.assasin.packet;

import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import net.minecraft.network.protocol.game.*;
import org.bukkit.entity.Player;

public class PacketProcessor {

    public void handleIn(Player player, Object packet, long tick) {
        PlayerData data = PlayerDataManager.getInstance().get(player);
        if (data == null) return;

        if (packet instanceof ServerboundMovePlayerPacket p) {
            double x = p.getX(data.getPositionTracker().getX());
            double y = p.getY(data.getPositionTracker().getY());
            double z = p.getZ(data.getPositionTracker().getZ());
            float yaw = p.getYRot(data.getRotationTracker().getYaw());
            float pitch = p.getXRot(data.getRotationTracker().getPitch());
            boolean ground = p.isOnGround();

            data.getPositionTracker().update(x, y, z, ground, player.getWorld());
            data.getRotationTracker().update(yaw, pitch);
            
            // Ejecutar Checks de Movimiento
            data.getCheckManager().runMovementChecks(player, data, tick);
        } 
        data.getCheckManager().getCheck(ScaffoldA.class).process(player, data, packet, tick);
        data.getCheckManager().getCheck(AirPlaceA.class).process(player, data, packet, tick);
        else if (packet instanceof ServerboundContainerClosePacket) {
            data.getInventoryTracker().onWindowClose(tick);
        } 
        
        else if (packet instanceof ServerboundPlaceBlockPacket p) {
            // Ejecutar Checks de World (Scaffold/AirPlace)
            data.getCheckManager().runWorldChecks(player, data, p, tick);
        }
    }

    public void handleOut(Player player, Object packet, long tick) {
        PlayerData data = PlayerDataManager.getInstance().get(player);
        if (data == null) return;

        if (packet instanceof ClientboundOpenScreenPacket p) {
            data.getInventoryTracker().onWindowOpen(p.getContainerId(), tick);
        } 
        else if (packet instanceof ClientboundContainerClosePacket) {
            data.getInventoryTracker().onWindowClose(tick);
        }
    }
}