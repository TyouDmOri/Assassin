package dev.tyoudm.assasin.data.tracker;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class CollisionTracker {
    private boolean onIce, inLiquid, onClimbable, underBlock, onSlime, onHoney;

    public void handle(Player player) {
        List<Material> materials = new ArrayList<>();
        // Revisamos el Bounding Box (0.6x1.8x0.6)
        for (double x = -0.31; x <= 0.31; x += 0.31) {
            for (double z = -0.31; z <= 0.31; z += 0.31) {
                // Bloque debajo (Pies)
                materials.add(player.getLocation().add(x, -0.1, z).getBlock().getType());
                // Bloque arriba (Cabeza - Detectar Head-hit)
                if (player.getLocation().add(x, 2.1, z).getBlock().getType().isSolid()) {
                    this.underBlock = true;
                } else {
                    this.underBlock = false;
                }
            }
        }

        this.onIce = materials.stream().anyMatch(m -> m.name().contains("ICE"));
        this.inLiquid = materials.contains(Material.WATER) || materials.contains(Material.LAVA);
        this.onClimbable = materials.contains(Material.LADDER) || materials.contains(Material.VINE) || materials.contains(Material.SCAFFOLDING);
        this.onSlime = materials.contains(Material.SLIME_BLOCK);
        this.onHoney = materials.contains(Material.HONEY_BLOCK);
    }

    // Getters para usar en los Checks
    public boolean isUnderBlock() { return underBlock; }
    public boolean isOnIce() { return onIce; }
    public boolean isSpecialFriction() { return onIce || onSlime || onHoney; }
}