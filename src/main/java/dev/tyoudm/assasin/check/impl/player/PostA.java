/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.player;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

@CheckInfo(
    name              = "PostA",
    type              = CheckType.POST_A,
    category          = CheckCategory.PLAYER,
    description       = "Detecta movimientos realizados antes de confirmar un teletransporte.",
    maxVl             = 5.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class PostA extends Check {

    public PostA(final MitigationEngine engine) { super(engine); }

    @Override
protected void process(Player player, PlayerData data, long tick) {
    MovementTracker movement = data.getMovementTracker();

    if (movement.isTeleportPending()) {
        double deltaH = movement.getDeltaH();
        
        // Si el movimiento es muy pequeño (jitter), lo ignoramos
        if (deltaH < 0.01) return;

        // Si el teletransporte ocurrió hace menos de 200ms, es probable que sea lag de red
        // Damos una exención temporal.
        long timeSinceTeleport = System.currentTimeMillis() - movement.getLastTeleportTime();
        if (timeSinceTeleport < 200) {
            return; 
        }

        // Si después de 200ms sigue enviando movimientos sin confirmar el TP, flagueamos.
        double buffer = data.getCheckData().getMotionABuffer(); // Reutilizamos un buffer
        if (deltaH > 0.1) {
            if (++buffer > 3) {
                flag(player, data, 1.0, "Action before TP confirm. Speed=" + deltaH, tick);
            }
        }
        data.getCheckData().setMotionABuffer(buffer);
    }
}
}