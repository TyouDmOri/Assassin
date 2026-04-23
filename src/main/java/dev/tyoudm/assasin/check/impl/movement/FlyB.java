package dev.tyoudm.assasin.check.impl.movement.fly;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.entity.Player;

public class FlyB extends Check {
    public FlyB() { super("FlyB", "AirTicks / Hover"); }

    @Override
    public void process(Player player, PlayerData data, long tick) {
        // Si está en el agua, puede estar flotando indefinidamente.
        if (data.getPositionTracker().isInLiquid()) return;

        int airTicks = data.getMovementTracker().getAirTicks();

        // Si lleva más de 40 ticks (2 segundos) en el aire sin caer rápido
        if (!data.isOnGround() && airTicks > 40) {
            flag(player, data, 1.0, "airTicks=" + airTicks, tick);
        }
    }
}