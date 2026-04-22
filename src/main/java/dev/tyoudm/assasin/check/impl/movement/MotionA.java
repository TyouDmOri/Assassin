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
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.VELOCITY, ExemptType.WIND_CHARGE)) return;

        double deltaY = data.getMovementTracker().getDeltaY();
        double lastDeltaY = data.getMovementTracker().getLastDeltaY();

        // Gravedad Vanilla: (LastY - 0.08) * 0.98
        double predictedY = (lastDeltaY - 0.08) * 0.98;

        if (!data.isOnGround() && !data.wasOnGround()) {
            // Si la diferencia entre lo que hizo y lo que debería hacer es muy grande
            if (Math.abs(deltaY - predictedY) > 0.02) {
                double buffer = data.getCheckData().getMotionABuffer();
                if (++buffer > 3) flag(player, data, 1.0, "diff=" + Math.abs(deltaY - predictedY), tick);
                data.getCheckData().setMotionABuffer(buffer);
            }
        }
    }
}