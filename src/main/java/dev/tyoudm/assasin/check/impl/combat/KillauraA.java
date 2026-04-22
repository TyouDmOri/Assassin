package dev.tyoudm.assasin.check.impl.combat;

import dev.tyoudm.assasin.check.*;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

@CheckInfo(name = "KillauraA", type = CheckType.POST_A, category = CheckCategory.COMBAT)
public final class KillauraA extends Check {
    public KillauraA(MitigationEngine engine) { super(engine); }

    @Override
    protected void process(Player player, PlayerData data, long tick) {
        // Detectar si el ataque ocurrió exactamente después de un paquete de posición
        // pero antes del siguiente tick del servidor.
        boolean attacking = data.getCombatTracker().isAttacking();
        boolean sentRotation = data.getRotationTracker().isSentRotationThisTick();

        if (attacking && sentRotation) {
            double buffer = data.getCheckData().getPostABuffer();
            if (++buffer > 5) {
                flag(player, data, 1.0, "Post-Tick Interaction", tick);
            }
            data.getCheckData().setPostABuffer(buffer);
        } else {
            data.getCheckData().setPostABuffer(Math.max(0, data.getCheckData().getPostABuffer() - 0.1));
        }
    }
}