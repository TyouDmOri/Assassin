package dev.tyoudm.assasin.check.impl.world.scaffold;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlaceBlockPacket;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ScaffoldA extends Check {

    public ScaffoldA() {
        super("ScaffoldA", "Detección de Colocación Imposible (Scaffold)");
    }

    public void process(Player player, PlayerData data, ServerboundPlaceBlockPacket packet, long tick) {
        BlockPos pos = packet.getTarget().getBlockPos();
        Block targetBlock = player.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        
        // EXCEPCIÓN 1: Si el bloque sobre el que se pone es sólido (como el piso), es legítimo.
        // Esto arregla tu error de "material=SAND" en el piso.
        if (targetBlock.getType().isSolid()) return;

        // EXCEPCIÓN 2: Ignorar si el jugador está en vehículos o usando Elytras.
        if (player.isInsideVehicle() || player.isGliding()) return;

        // LÓGICA DE RAYCAST (Simplificada)
        // Obtenemos el vector de dirección donde mira el jugador
        Vector eyeLocation = player.getEyeLocation().toVector();
        Vector lookDirection = player.getEyeLocation().getDirection();
        
        // Calculamos la distancia al bloque
        Vector blockCenter = new Vector(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        double distance = eyeLocation.distance(blockCenter);

        // Si la distancia es muy corta (está pegado al bloque), el raycast puede fallar, así que ignoramos.
        if (distance < 1.2) return;

        // Verificamos el ángulo entre la mirada y el bloque
        Vector dirToBlock = blockCenter.clone().subtract(eyeLocation).normalize();
        double dot = lookDirection.dot(dirToBlock);

        // Si el dot product es muy bajo, significa que está poniendo un bloque a sus espaldas o 
        // en un ángulo físicamente imposible para el cliente vanilla.
        if (dot < 0.45) {
            double buffer = data.getCheckData().getScaffoldBuffer();
            if (++buffer > 3) {
                flag(player, data, 1.5, 
                    String.format("no raycast hit, face=%s material=%s dot=%.2f", 
                    packet.getTarget().getDirection().name(), targetBlock.getType(), dot), tick);
            }
            data.getCheckData().setScaffoldBuffer(buffer);
        } else {
            data.getCheckData().setScaffoldBuffer(Math.max(0, data.getCheckData().getScaffoldBuffer() - 0.1));
        }
    }
}