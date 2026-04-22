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
    description = "Friction-based movement prediction.",
    maxVl = 10.0,
    severity = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class SpeedB extends Check {
    public SpeedB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK, 
            ExemptType.VELOCITY, ExemptType.WIND_CHARGE, ExemptType.ICE)) return;

        double speedH = data.getVelocityH();
        double lastSpeedH = data.getMovementTracker().getLastSpeedH();
        
        // 1. Calcular fricción basada en el bloque (requiere BlockTracker)
        double friction = data.isOnGround() ? data.getBlockTracker().getLastFriction() * 0.91 : 0.91;
        
        // 2. Calcular predicción
        double attributeSpeed = data.getAttributeTracker().getWalkSpeed();
        double sprintMult = data.isSprinting() ? 1.3 : 1.0;
        
        double predicted = (lastSpeedH * friction) + (attributeSpeed * 0.1 * sprintMult);
        
        // 3. Compensar el primer tick del aire (Jump Boost horizontal)
        if (!data.isOnGround() && data.wasOnGround()) {
            predicted += 0.2;
        }

        double threshold = predicted + 0.01; // Margen de precisión

        if (speedH > threshold) {
            flag(player, data, 1.0, String.format("H=%.3f Pred=%.3f", speedH, threshold), tick);
        }
    }
}