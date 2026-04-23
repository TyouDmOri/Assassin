package dev.tyoudm.assasin.data.tracker;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;

public class AttributeTracker {
    private volatile double walkSpeed = 0.1;
    private volatile double attackRange = 3.0;

    public void handleSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.UPDATE_ATTRIBUTES) {
            WrapperPlayServerUpdateAttributes wrapper = new WrapperPlayServerUpdateAttributes(event);
            for (WrapperPlayServerUpdateAttributes.Property property : wrapper.getProperties()) {
                String key = property.getKey();
                if (key.contains("movement_speed")) this.walkSpeed = property.getValue();
                if (key.contains("attack_range")) this.attackRange = property.getValue();
            }
        }
    }

    public double getWalkSpeed() { return walkSpeed; }
    public double getAttackRange() { return attackRange; }
}