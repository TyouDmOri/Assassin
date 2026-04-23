package dev.tyoudm.assasin.check.impl.movement.inventory;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.entity.Player;

public class InventoryA extends Check {
    public InventoryA() { super("InventoryA", "InventoryMove"); }

    @Override
    public void process(Player player, PlayerData data, long tick) {
        if (!data.getInventoryTracker().isWindowOpen()) return;
        
        // Ignorar si acaba de cerrar (latencia) o si está en el aire (caída)
        if (tick - data.getInventoryTracker().getLastWindowCloseTick() < 3) return;
        if (!data.isOnGround()) return;

        double deltaH = data.getMovementTracker().getCurrentSpeedH();
        double lastDeltaH = data.getMovementTracker().getLastSpeedH();

        // Si acelera mientras tiene la GUI abierta y está en el suelo
        if (deltaH > lastDeltaH && deltaH > 0.12) {
            flag(player, data, 1.0, "Accelerating with GUI", tick);
        }
    }
}