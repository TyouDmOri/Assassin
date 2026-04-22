package dev.tyoudm.assasin.check.impl.movement;
import dev.tyoudm.assasin.check.*;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;
@CheckInfo(name = "SpeedB", type = CheckType.SPEED_B, category = CheckCategory.MOVEMENT)
public final class SpeedB extends Check {
    public SpeedB(MitigationEngine engine) { super(engine); }
    @Override
    protected void process(Player player, PlayerData data, long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SPEAR_LUNGE)) return;
        double speedH = data.getVelocityH();
        double lastH = data.getMovementTracker().getLastSpeedH();
        double friction = data.isOnGround() ? 0.546 : 0.91;
        double pred = (lastH * friction) + (data.getAttributeTracker().getWalkSpeed() * (data.isSprinting() ? 0.13 : 0.1));
        if (speedH > pred + 0.01) {
            double b = data.getCheckData().getSpeedBBuffer();
            if (++b > 3) flag(player, data, 1.0, "H=" + speedH + " P=" + pred, tick);
            data.getCheckData().setSpeedBBuffer(b);
        } else data.getCheckData().setSpeedBBuffer(0);
    }
}
