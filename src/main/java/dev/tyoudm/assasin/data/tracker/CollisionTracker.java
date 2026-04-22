package dev.tyoudm.assasin.data.tracker;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.ArrayList;

public class CollisionTracker {
    private boolean onHoney, onSlime, inWeb, inLiquid, onIce, onClimbable;
    private double lastFriction = 0.6;

    public void handle(Player player) {
        // Obtenemos bloques en el bounding box del jugador (simplificado)
        List<Material> materials = new ArrayList<>();
        for (double x = -0.3; x <= 0.3; x += 0.3) {
            for (double z = -0.3; z <= 0.3; z += 0.3) {
                Block b = player.getLocation().add(x, -0.1, z).getBlock();
                materials.add(b.getType());
            }
        }

        this.onHoney = materials.contains(Material.HONEY_BLOCK);
        this.onSlime = materials.contains(Material.SLIME_BLOCK);
        this.inWeb = materials.contains(Material.COBWEB);
        this.onIce = materials.stream().anyMatch(m -> m.name().contains("ICE"));
        
        // Determinar fricción de la 1.21.11
        if (onIce) lastFriction = 0.98;
        else if (onSlime) lastFriction = 0.8;
        else lastFriction = 0.6;
    }

    public double getLastFriction() { return lastFriction; }
    public boolean isInWeb() { return inWeb; }
    public boolean isOnHoney() { return onHoney; }
}