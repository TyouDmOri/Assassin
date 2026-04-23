package dev.tyoudm.assasin.data.tracker;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class RotationTracker {
    private float yaw, pitch, lastYaw, lastPitch;
    private float deltaYaw, deltaPitch;

    public void handleReceive(PacketReceiveEvent event) {
        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
        
        if (wrapper.hasRotationChanged()) {
            this.lastYaw = yaw;
            this.lastPitch = pitch;
            
            this.yaw = wrapper.getLocation().getYaw();
            this.pitch = wrapper.getLocation().getPitch();

            this.deltaYaw = Math.abs(yaw - lastYaw);
            this.deltaPitch = Math.abs(pitch - lastPitch);
        }
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getLastYaw() { return lastYaw; }
    public float getDeltaYaw() { return deltaYaw; }
}