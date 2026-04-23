package dev.tyoudm.assasin.check.impl.movement.timer;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.entity.Player;

public class TimerA extends Check {
    public TimerA() { super("TimerA", "Game Speed / Timer"); }

    private int packetCount = 0;
    private long lastReset = System.currentTimeMillis();

    @Override
    public void process(Player player, PlayerData data, long tick) {
        packetCount++;
        long now = System.currentTimeMillis();

        // Revisar cada 1000ms (1 segundo)
        if (now - lastReset >= 1000) {
            // El límite teórico es 20. Permitimos 24 para lag, pero tú flagueaste con 25.
            // Sube el límite a 26 o usa un buffer acumulativo.
            if (packetCount > 26) { 
                flag(player, data, 1.0, "packets=" + packetCount + " max=26", tick);
            }
            
            packetCount = 0;
            lastReset = now;
        }
    }
}