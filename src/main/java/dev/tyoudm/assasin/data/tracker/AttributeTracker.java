package dev.tyoudm.assasin.data.tracker;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
public class AttributeTracker {
    private double walkSpeed = 0.1, attackRange = 3.0;
    public void handleSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.UPDATE_ATTRIBUTES) {
            WrapperPlayServerUpdateAttributes wrapper = new WrapperPlayServerUpdateAttributes(event);
            for (WrapperPlayServerUpdateAttributes.Property p : wrapper.getProperties()) {
                if (p.getKey().contains("movement_speed")) this.walkSpeed = p.getValue();
                if (p.getKey().contains("attack_range")) this.attackRange = p.getValue();
            }
        }
    }
    public double getWalkSpeed() { return walkSpeed; }
    public double getAttackRange() { return attackRange; }
}
