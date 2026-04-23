package dev.tyoudm.assasin.handler.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketProcessor extends PacketListenerAbstract {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;

        // 1. Obtener los datos del jugador desde el ServiceContainer
        PlayerData data = AssasinPlugin.getInstance().getServiceContainer().getDataManager().getData(player);
        if (data == null) return;

        long tick = AssasinPlugin.getInstance().getTick();

        // 2. MANEJO DE MOVIMIENTO Y POSICIÓN (Flying, Position, Rotation)
        if (PacketType.Play.Client.isPosition(event.getPacketType()) || 
            PacketType.Play.Client.isLook(event.getPacketType())) {
            
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
            
            // Actualizar Trackers
            data.getMovementTracker().handleReceive(event);
            data.getRotationTracker().handleReceive(event);
            
            // Actualizar Atómicos en PlayerData (Para Thread-Safety)
            if (flying.hasPositionChanged()) {
                data.setPosition(flying.getLocation().getX(), flying.getLocation().getY(), flying.getLocation().getZ());
            }
            if (flying.hasRotationChanged()) {
                data.setRotation(flying.getLocation().getYaw(), flying.getLocation().getPitch());
            }
            data.setOnGround(flying.isOnGround());

            // 3. GRABACIÓN EN REPLAY BUFFER (Caja Negra)
            data.getReplayBuffer().addSnapshot(
                data.getX(), data.getY(), data.getZ(),
                data.getYaw(), data.getPitch(), data.isOnGround()
            );

            // 4. COLISIONES (Sincrónico con Bukkit API)
            Bukkit.getScheduler().runTask(AssasinPlugin.getInstance(), () -> {
                data.getCollisionTracker().handle(player);
            });

            // 5. EJECUTAR CHECKS DE MOVIMIENTO (Asíncrono)
            AssasinPlugin.getInstance().getServiceContainer().getCheckManager().runMovementChecks(player, data, tick);
        }

        // 6. MANEJO DE COMBATE (Interact Entity)
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            
            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                data.getCombatTracker().handleInteract(event);
                
                // Ejecutar Checks de Combate
                AssasinPlugin.getInstance().getServiceContainer().getCheckManager().runCombatChecks(player, data, tick);
            }
        }

        // Detectar cuando el cliente confirma el teletransporte
if (event.getPacketType() == PacketType.Play.Client.TELEPORT_CONFIRM) {
    WrapperPlayClientTeleportConfirm confirm = new WrapperPlayClientTeleportConfirm(event);
    data.getMovementTracker().confirmTeleport(confirm.getTeleportId());
    
    // Al confirmar, limpiamos cualquier exención de movimiento
    data.getExemptManager().addExempt(ExemptType.TELEPORT_PENDING, 2); 
}
        // 7. MANEJO DE LATENCIA (Pong)
        if (event.getPacketType() == PacketType.Play.Client.PONG) {
            data.getTrackerLatency().update(player);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;

        PlayerData data = AssasinPlugin.getInstance().getServiceContainer().getDataManager().getData(player);
        if (data == null) return;

        // 8. TRACKING SALIENTE (Atributos y Velocity)
        // Detectar cuando el servidor le da velocidad al jugador (Knockback)
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            data.getVelocityTracker().handleSend(event);
        }
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
    WrapperPlayServerPlayerPositionAndLook tp = new WrapperPlayServerPlayerPositionAndLook(event);
    data.getMovementTracker().handleTeleport(tp.getTeleportId());
}
        // Detectar cambios en velocidad de caminata o rango de ataque (1.21.11)
        if (event.getPacketType() == PacketType.Play.Server.UPDATE_ATTRIBUTES) {
            data.getAttributeTracker().handleSend(event);
        }
    }
}