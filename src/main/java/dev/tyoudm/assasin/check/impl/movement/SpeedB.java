package dev.tyoudm.assasin.check.impl.movement;

import dev.tyoudm.assasin.check.*;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

@CheckInfo(
    name = "SpeedB", 
    type = CheckType.SPEED_B, 
    category = CheckCategory.MOVEMENT,
    maxVl = 8.0
)
public final class SpeedB extends Check {
    public SpeedB(MitigationEngine engine) {
        super(engine);
    }

    @Override
    protected void process(Player player, PlayerData data, long tick) {
        // Exenciones críticas para evitar falsos positivos
        if (isExemptAny(data, tick, 
            ExemptType.TELEPORT_PENDING, 
            ExemptType.SPEAR_LUNGE, 
            ExemptType.VELOCITY, 
            ExemptType.JOINED)) return;

        double speedH = data.getVelocityH();
        double lastH = data.getMovementTracker().getLastSpeedH();
        
        // Fricción básica: 0.91 en aire, 0.546 en suelo
        double friction = data.isOnGround() ? 0.546 : 0.91;
        
        // Predicción: v_{pred} = (v_{last} \times friction) + acceleration
        double walkSpeed = data.getAttributeTracker().getWalkSpeed();
        double acceleration = walkSpeed * (data.isSprinting() ? 0.13 : 0.1);
        
        double predicted = (lastH * friction) + acceleration;

        if (speedH > predicted + 0.01) {
            double buffer = data.getCheckData().getSpeedBBuffer();
            
            if (++buffer > 3) {
                flag(player, data, 1.0, "H=" + String.format("%.3f", speedH) + " P=" + String.format("%.3f", predicted), tick);
            }
            data.getCheckData().setSpeedBBuffer(buffer);
        } else {
            data.getCheckData().setSpeedBBuffer(Math.max(0, data.getCheckData().getSpeedBBuffer() - 0.1));
        }
    }
}