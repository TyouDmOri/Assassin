package dev.tyoudm.assasin.check.impl.movement.strafe;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.entity.Player;

public class StrafeA extends Check {

    public StrafeA() {
        super("StrafeA", "Detección de OmniSprint / Movimiento Imposible");
    }

    @Override
    public void process(Player player, PlayerData data, long tick) {
        // 1. OBTENER DELTAS Y NORMALIZAR YAW
        double dx = data.getMovementTracker().getDx();
        double dz = data.getMovementTracker().getDz();
        double deltaH = data.getMovementTracker().getCurrentSpeedH();
        
        // Normalizamos el Yaw para que esté entre -180 y 180
        float yaw = data.getRotationTracker().getYaw() % 360;
        if (yaw > 180) yaw -= 360;
        if (yaw < -180) yaw += 360;

        // 2. CALCULAR ÁNGULO DE MOVIMIENTO REAL
        // Atan2 nos da el ángulo hacia donde se desplaza el jugador
        double moveAngle = Math.toDegrees(Math.atan2(-dx, dz));
        
        // 3. CALCULAR DESVIACIÓN
        double deviation = Math.abs(yaw - moveAngle) % 360;
        if (deviation > 180) deviation = 360 - deviation;

        // 4. LÓGICA DE DETECCIÓN (Solo si está esprintando y moviéndose rápido)
        if (data.isSprinting() && deltaH > 0.2) {
            
            // En Minecraft vanilla, no puedes esprintar si tu ángulo es mayor a ~70° 
            // (no puedes correr de lado o hacia atrás).
            double limit = 75.0; 

            // Si el jugador está en el aire, permitimos más margen por el "air strafe"
            if (!data.isOnGround()) limit += 15.0;

            if (deviation > limit) {
                double buffer = data.getCheckData().getStrafeBuffer();
                
                // Usamos un buffer porque un solo tick puede ser un giro rápido de ratón
                if (++buffer > 3) {
                    flag(player, data, 1.0, 
                        String.format("deviation=%.1f° max=%.1f° deltaH=%.3f", deviation, limit, deltaH), tick);
                    buffer = 0;
                }
                data.getCheckData().setStrafeBuffer(buffer);
            } else {
                data.getCheckData().setStrafeBuffer(Math.max(0, data.getCheckData().getStrafeBuffer() - 0.1));
            }
        }
    }
}