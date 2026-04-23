package dev.tyoudm.assasin.data.tracker;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.Set;
import java.util.HashSet;

public class CollisionTracker {
    private volatile boolean onIce, inLiquid, onClimbable, underBlock;

    public void handle(Player player) {
        Set<Material> materials = new HashSet<>();
        // Revisión de 3x3 bloques alrededor de los pies para máxima precisión
        for (double x = -0.3; x <= 0.3; x += 0.3) {
            for (double z = -0.3; z <= 0.3; z += 0.3) {
                materials.add(player.getLocation().add(x, -0.01, z).getBlock().getType());
                if (player.getLocation().add(x, 2.1, z).getBlock().getType().isSolid()) {
                    this.underBlock = true;
                }
            }
        }
        this.onIce = materials.stream().anyMatch(m -> m.name().contains("ICE"));
        this.inLiquid = materials.contains(Material.WATER) || materials.contains(Material.LAVA);
        this.onClimbable = materials.contains(Material.LADDER) || materials.contains(Material.VINE) || materials.contains(Material.SCAFFOLDING);
    }

    public boolean isUnderBlock() { return underBlock; }
    public boolean isOnIce() { return onIce; }
    public boolean isInLiquid() { return inLiquid; }
    public boolean isOnClimbable() { return onClimbable; }
}