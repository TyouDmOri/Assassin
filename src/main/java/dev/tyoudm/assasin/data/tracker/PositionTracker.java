package dev.tyoudm.assasin.data.tracker;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

@Getter
public final class PositionTracker {

    private double x, y, z;
    private double lastX, lastY, lastZ;
    private double deltaX, deltaY, deltaZ;
    private double deltaH;

    private boolean onGround;
    private boolean lastOnGround;
    
    // Estados del entorno (Clave para evitar falsos positivos)
    private boolean inLiquid;
    private boolean inWeb;
    private boolean onSlime;
    private boolean onIce;
    private boolean nearWall;

    public void update(double x, double y, double z, boolean onGround, World world) {
        // 1. Guardar posiciones anteriores
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.lastOnGround = this.onGround;

        // 2. Actualizar posiciones actuales
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;

        // 3. Calcular Deltas
        this.deltaX = x - lastX;
        this.deltaY = y - lastY;
        this.deltaZ = z - lastZ;
        this.deltaH = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // 4. Analizar el entorno (Optimizado para no causar lag)
        analyzeEnvironment(world);
    }

    private void analyzeEnvironment(World world) {
        Location loc = new Location(world, x, y, z);
        
        // Revisamos el bloque en los pies y un poco arriba
        Block blockAt = world.getBlockAt(loc);
        Block blockUnder = world.getBlockAt(loc.clone().subtract(0, 0.1, 0));
        Block blockAbove = world.getBlockAt(loc.clone().add(0, 1.8, 0));

        this.inLiquid = blockAt.isLiquid() || blockUnder.isLiquid();
        this.inWeb = blockAt.getType() == Material.COBWEB;
        
        Material underType = blockUnder.getType();
        this.onSlime = underType == Material.SLIME_BLOCK;
        this.onIce = underType == Material.ICE || underType == Material.PACKED_ICE || underType == Material.BLUE_ICE;
        
        // Detección simple de paredes (para Strafe y Speed)
        this.nearWall = world.getBlockAt(loc.clone().add(0.3, 0.5, 0)).getType().isSolid() ||
                        world.getBlockAt(loc.clone().add(-0.3, 0.5, 0)).getType().isSolid() ||
                        world.getBlockAt(loc.clone().add(0, 0.5, 0.3)).getType().isSolid() ||
                        world.getBlockAt(loc.clone().add(0, 0.5, -0.3)).getType().isSolid();
    }
}