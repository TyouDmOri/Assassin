package dev.tyoudm.assasin.check.impl.movement.speed;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.entity.Player;

public class SpeedB extends Check {

    public SpeedB() {
        super("SpeedB", "Detección de Movimiento por Predicción");
    }

    @Override
    public void process(Player player, PlayerData data, long tick) {
        double deltaH = data.getMovementTracker().getDeltaH();
        double lastDeltaH = data.getMovementTracker().getLastDeltaH();
        
        // 1. OBTENER VELOCIDAD BASE (Soporte para Atributos 1.21.11)
        double attributeSpeed = data.getAttributeTracker().getWalkSpeed(); 
        
        // 2. CALCULAR PREDICCIÓN (Física de rozamiento de Minecraft)
        // El factor 0.91 es el rozamiento estándar del aire/bloques
        double predicted = lastDeltaH * 0.91F; 
        
        // 3. APLICAR IMPULSO (Si está en el suelo o saltando)
        double max = predicted;
        
        if (data.isOnGround()) {
            // Si está en el suelo, el max es la velocidad de sprint (aprox 0.28)
            max = attributeSpeed * (data.isSprinting() ? 1.302 : 1.0);
        } else {
            // SI ESTÁ EN EL AIRE: Minecraft permite un "impulso" adicional 
            // Esto es lo que faltaba en tus logs (por eso daba 0.29 y el jugador iba a 0.46)
            max += 0.026; // Factor de inercia en aire
            
            // Si acaba de saltar en este tick, le sumamos el impulso del salto (Jump Boost)
            if (data.hasJumped()) {
                max += 0.2; // El salto añade aprox 0.2 a la velocidad horizontal
            }
        }

        // 4. COMPARACIÓN Y VL
        double excess = deltaH - max;

        if (excess > 0.01) {
            double buffer = data.getCheckData().getSpeedBBuffer();
            buffer += excess * 15; // Escalamos según el exceso

            if (buffer > 1.2) {
                flag(player, data, 1.0, 
                    String.format("speedH=%.4f predicted=%.4f max=%.4f", deltaH, predicted, max), tick);
                buffer = 0.5; // Reset parcial
            }
            data.getCheckData().setSpeedBBuffer(buffer);
        } else {
            data.getCheckData().setSpeedBBuffer(Math.max(0, data.getCheckData().getSpeedBBuffer() - 0.01));
        }
    }
}