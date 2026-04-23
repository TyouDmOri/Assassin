package dev.tyoudm.assasin.data.tracker;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;

public class VelocityTracker {
    private int velocityTicks = 0;

    public void handleSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            // Si el paquete de velocidad es para el jugador, le damos 40 ticks (2 seg) de margen
            this.velocityTicks = 40;
        }
    }

    public void tick() {
        if (velocityTicks > 0) {
            velocityTicks--;
        }
    }

    public boolean isExempt() {
        return velocityTicks > 0;
    }
}