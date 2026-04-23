package dev.tyoudm.assasin.check.impl.world.place;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlaceBlockPacket;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class AirPlaceA extends Check {

    public AirPlaceA() {
        super("AirPlaceA", "Colocación de Bloques en el Aire");
    }

    public void process(Player player, PlayerData data, ServerboundPlaceBlockPacket packet, long tick) {
        BlockPos pos = packet.getTarget().getBlockPos();
        Block clickedBlock = player.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());

        // EXCEPCIÓN 1: Si el bloque clickeado es sólido, tiene apoyo.
        // Esto evita el flag cuando pones arena en el suelo.
        if (clickedBlock.getType().isSolid()) return;

        // EXCEPCIÓN 2: Bloques especiales que no tienen colisión sólida pero son apoyo (ej. Slab, Stairs)
        if (clickedBlock.getType().isInteractable() && !clickedBlock.isReplaceable()) return;

        // REGLA: En Minecraft vanilla, solo puedes poner un bloque si haces clic en la CARA de otro bloque sólido.
        // Si el bloque clickeado es AIRE o LIQUIDO, es un AirPlace.
        if (clickedBlock.isEmpty() || clickedBlock.isLiquid()) {
            
            double buffer = data.getCheckData().getAirPlaceBuffer();
            
            // Usamos un buffer porque el lag de red puede hacer que el bloque de apoyo 
            // aún no se haya actualizado en el servidor.
            if (++buffer > 2) {
                flag(player, data, 2.0, 
                    String.format("air place: no support block found, material=%s", 
                    player.getInventory().getItemInMainHand().getType()), tick);
            }
            data.getCheckData().setAirPlaceBuffer(buffer);
        } else {
            data.getCheckData().setAirPlaceBuffer(Math.max(0, data.getCheckData().getAirPlaceBuffer() - 0.5));
        }
    }
}