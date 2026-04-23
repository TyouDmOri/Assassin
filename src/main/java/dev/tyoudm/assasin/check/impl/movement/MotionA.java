package dev.tyoudm.assasin.check.impl.movement;

import dev.tyoudm.assasin.check.*;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

@CheckInfo(name = "MotionA", type = CheckType.MOTION_A, category = CheckCategory.MOVEMENT)
public final class MotionA extends Check {
    public MotionA(MitigationEngine engine) { super(engine); }

    @Override
    protected void process(Player player, PlayerData data, long tick) {
        // Exenciones de gravedad (Wind Charge, Elevadores, etc.)
        if (isExemptAny(data, tick, ExemptType.VELOCITY, ExemptType.TELEPORT_PENDING)) return;

        double deltaY = data.getMovementTracker().getDeltaY();
        double lastDeltaY = data.getMovementTracker().getLastDeltaY();
        
        // Predicción física de Minecraft: Gravedad (0.08) y Rozamiento (0.98)
        double prediction = (lastDeltaY - 0.08) * 0.98;

        // Si el jugador está en el aire y no baja como debería...
        if (!data.isOnGround() && !data.getCollisionTracker().isInLiquid()) {
            
            // Margen de error para pequeñas variaciones
            if (deltaY > prediction + 0.001 && deltaY > -0.5) {
                double buffer = data.getCheckData().getMotionABuffer();
                if (++buffer > 5) {
                    flag(player, data, 1.0, "Y=" + deltaY + " Pred=" + prediction, tick);
                }
                data.getCheckData().setMotionABuffer(buffer);
            } else {
                data.getCheckData().setMotionABuffer(Math.max(0, data.getCheckData().getMotionABuffer() - 0.1));
            }
        }
    }
}