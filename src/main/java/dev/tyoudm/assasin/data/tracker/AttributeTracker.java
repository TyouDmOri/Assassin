package dev.tyoudm.assasin.data.tracker;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;

public class AttributeTracker {
    private double walkSpeed = 0.10000000149011612;
    private double jumpHeight = 0.42;
    private double reachDistance = 3.0;

    public void handleSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.UPDATE_ATTRIBUTES) {
            WrapperPlayServerUpdateAttributes wrapper = new WrapperPlayServerUpdateAttributes(event);
            for (WrapperPlayServerUpdateAttributes.Property property : wrapper.getProperties()) {
                String key = property.getKey();
                double value = property.getValue();

                if (key.contains("movement_speed")) this.walkSpeed = value;
                else if (key.contains("jump_strength")) this.jumpHeight = value;
                else if (key.contains("reach_distance")) this.reachDistance = value;
            }
        }
    }

    // Getters manuales (Sustituyen a @Getter)
    public double getWalkSpeed() { return walkSpeed; }
    public double getJumpHeight() { return jumpHeight; }
    public double getReachDistance() { return reachDistance; }
}