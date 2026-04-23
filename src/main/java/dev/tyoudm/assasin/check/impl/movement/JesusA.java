package dev.tyoudm.assasin.check.impl.movement.jesus;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class JesusA extends Check {

    public JesusA() {
        super("JesusA", "Detección de Walking on Water");
    }

    @Override
    public void process(Player player, PlayerData data, long tick) {
        // 1. OBTENER ESTADO DEL ENTORNO DESDE EL TRACKER
        boolean inLiquid = data.getPositionTracker().isInLiquid();
        boolean onGround = data.getMovementTracker().isOnGround();
        double y = data.getPositionTracker().getY();
        
        // 2. EXCEPCIONES (Evita falsos positivos al entrar)
        // Si el jugador está cerca de una pared o sobre un bloque sólido, ignoramos.
        if (data.getPositionTracker().isNearWall()) return;
        
        // 3. LÓGICA DE DETECCIÓN
        // Un "Jesus" camina sobre el agua (onGround = true) pero el bloque es agua.
        // Los logs muestran Y=70.0 (exacto), indicando que no se hunde.
        boolean standingOnLiquid = onGround && inLiquid && (y % 1.0 == 0 || y % 1.0 == 0.9375);

        if (standingOnLiquid) {
            // Comprobamos si hay bloques sólidos realmente cerca (Lily pads, etc.)
            if (isNearSolid(player, data)) return;

            double buffer = data.getCheckData().getJesusBuffer();
            
            // Usamos un buffer de 3 ticks para permitir la entrada al agua
            if (++buffer > 3) {
                flag(player, data, 1.0, String.format("standing on water: Y=%.2f", y), tick);
            }
            data.getCheckData().setJesusBuffer(buffer);
        } else {
            data.getCheckData().setJesusBuffer(Math.max(0, data.getCheckData().getJesusBuffer() - 0.2));
        }
    }

    private boolean isNearSolid(Player player, PlayerData data) {
        // Verifica si el bloque debajo es realmente agua o si hay algo sólido
        Material type = player.getLocation().subtract(0, 0.1, 0).getBlock().getType();
        return type.isSolid(); // Si es sólido (ej: Lily Pad), no es Jesus.
    }
}