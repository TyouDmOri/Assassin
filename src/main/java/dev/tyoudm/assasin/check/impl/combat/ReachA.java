package dev.tyoudm.assasin.check.impl.combat;

import dev.tyoudm.assasin.check.*;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

@CheckInfo(
    name = "ReachA", 
    type = CheckType.AIM_A, // Puedes usar REACH_A si lo tienes en tu Enum
    category = CheckCategory.COMBAT,
    maxVl = 5.0
)
public final class ReachA extends Check {
    public ReachA(MitigationEngine engine) {
        super(engine);
    }

    @Override
    protected void process(Player player, PlayerData data, long tick) {
        // Exenciones básicas (Teleports, Respawn, etc.)
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.JOINED)) return;

        // Distancia del último ataque (Calculada en tu CombatTracker)
        double distance = data.getCombatTracker().getLastAttackedDistance();
        
        // Rango máximo dinámico (Importante para 1.21.11 - Lanzas)
        double maxReach = data.getAttributeTracker().getAttackRange();
        
        // Añadimos un pequeño margen por latencia (Ping)
        double threshold = maxReach + 0.45;

        if (distance > threshold) {
            double buffer = data.getCheckData().getReachBuffer();
            
            // Usamos un buffer para no banear por un solo lagazo
            if (++buffer > 2) {
                flag(player, data, 1.0, "D=" + String.format("%.2f", distance) + " Max=" + maxReach, tick);
            }
            data.getCheckData().setReachBuffer(buffer);
        } else {
            // Bajamos el buffer lentamente si el ataque es legal
            data.getCheckData().setReachBuffer(Math.max(0, data.getCheckData().getReachBuffer() - 0.1));
        }
    }
}