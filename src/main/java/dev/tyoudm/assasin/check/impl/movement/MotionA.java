package dev.tyoudm.assasin.check.impl.movement.motion;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class MotionA extends Check {

    public MotionA() {
        super("MotionA", "Detección de Movimiento Vertical Ilegal");
    }

    @Override
    public void process(Player player, PlayerData data, long tick) {
        double deltaY = data.getMovementTracker().getCurrentSpeedY();
        double deltaH = data.getMovementTracker().getCurrentSpeedH();

        // 1. CALCULAR EL MÁXIMO SALTO PERMITIDO DINÁMICAMENTE
        // El salto base es 0.42. Cada nivel de Jump Boost añade 0.1.
        int jumpBoostLevel = data.getPotionTracker().getEffectLevel(PotionEffectType.JUMP_BOOST);
        
        // El valor 0.5413 que ves en tu log es típico de Jump Boost I + un pequeño buffer de Paper.
        // Ponemos un margen de 0.05 para evitar falsos positivos por lag.
        double maxY = 0.42 + (jumpBoostLevel * 0.1) + 0.15; 

        // 2. COMPROBAR LÍMITE VERTICAL
        // Si el jugador sube más rápido de lo que la física permite
        if (deltaY > maxY) {
            // EXCEPCIONES: Si ha sido golpeado (Velocity) o está en telarañas/agua, ignoramos.
            if (isExemptAny(data, tick, ExemptType.VELOCITY, ExemptType.LIQUID)) return;

            flag(player, data, 1.0, 
                String.format("ΔspeedH=%.4f ΔspeedY=%.4f maxH=0.5000 maxY=%.4f", deltaH, deltaY, maxY), tick);
        }
        
        // 3. COMPROBAR MOVIMIENTO "IMPOSIBLE" (Salto sin velocidad horizontal)
        // Algunos hacks de "Vertical Fly" suben recto sin moverse hacia los lados.
        if (deltaY > 0.42 && deltaH < 0.001 && !data.isOnGround()) {
            double buffer = data.getCheckData().getVerticalBuffer();
            if (++buffer > 2) {
                flag(player, data, 1.0, "Vertical Movement without Horizontal Speed", tick);
            }
            data.getCheckData().setVerticalBuffer(buffer);
        } else {
            data.getCheckData().setVerticalBuffer(Math.max(0, data.getCheckData().getVerticalBuffer() - 0.1));
        }
    }
}