package dev.tyoudm.assasin.data.tracker;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.entity.Player;

public class LatencyTracker {
    private int ping;

    public void update(Player player) {
        this.ping = PacketEvents.getAPI().getPlayerManager().getPing(player);
    }

    public int getPing() { return ping; }

    // Fórmula de compensación: Δ_reach = (ping / 100) * 0.12
    public double getReachTransaction() {
        return (ping / 100.0) * 0.12;
    }
}