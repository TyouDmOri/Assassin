package dev.tyoudm.assasin.data.tracker;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;

public class AttributeTracker {
    private double walkSpeed = 0.1;
    private double attackRange = 3.0; // Rango base de Minecraft

    public void handleSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.UPDATE_ATTRIBUTES) {
            WrapperPlayServerUpdateAttributes wrapper = new WrapperPlayServerUpdateAttributes(event);
            
            for (WrapperPlayServerUpdateAttributes.Property property : wrapper.getProperties()) {
                String key = property.getKey();
                
                // Actualizar velocidad de caminata
                if (key.contains("movement_speed")) {
                    this.walkSpeed = property.getValue();
                }
                
                // 1.21.11: Atributo dinámico de alcance de ataque (Spears/Lanzas)
                if (key.contains("attack_range")) {
                    this.attackRange = property.getValue();
                }
            }
        }
    }

    public double getWalkSpeed() { return walkSpeed; }
    public double getAttackRange() { return attackRange; }
}