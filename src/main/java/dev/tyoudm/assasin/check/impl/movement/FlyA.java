package dev.tyoudm.assasin.check.impl.movement.fly;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.entity.Player;

public class FlyA extends Check {
    public FlyA() { super("FlyA", "Gravedad Imposible"); }

    @Override
    public void process(Player player, PlayerData data, long tick) {
        // EXCEPCIÓN: La física en el agua, telarañas o con Elytras es distinta.
        if (data.getPositionTracker().isInLiquid() || 
            data.getPositionTracker().isInWeb() || 
            player.isGliding()) {
            data.getCheckData().setVerticalBuffer(0);
            return;
        }

        double motionY = data.getMovementTracker().getDeltaY();
        double lastMotionY = data.getMovementTracker().getLastDeltaY();
        
        // Predicción simple de gravedad en el aire
        double expected = (lastMotionY - 0.08) * 0.98;

        // Si la diferencia entre lo que hace el jugador y la gravedad es mucha
        if (!data.isOnGround() && Math.abs(motionY - expected) > 0.05) {
            double buffer = data.getCheckData().getVerticalBuffer();
            if (++buffer > 5) { // Buffer de 5 para absorber picos de lag
                flag(player, data, 1.0, String.format("motionY=%.4f expected=%.4f", motionY, expected), tick);
            }
            data.getCheckData().setVerticalBuffer(buffer);
        } else {
            data.getCheckData().setVerticalBuffer(Math.max(0, data.getCheckData().getVerticalBuffer() - 0.1));
        }
    }
}