package dev.tyoudm.assasin.data.tracker;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
public class VelocityTracker {
    private int ticks;
    public void handleSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) this.ticks = 40;
    }
    public void tick() { if (ticks > 0) ticks--; }
    public boolean isExempt() { return ticks > 0; }
}
