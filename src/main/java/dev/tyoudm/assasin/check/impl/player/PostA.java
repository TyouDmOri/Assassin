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
    protected void process(final Player player, final PlayerData data, final long tick) {
        // Solo procesamos si hay un TP pendiente detectado por el tracker
        if (!isExempt(data, ExemptType.TELEPORT_PENDING, tick)) {
            // Si no hay TP pendiente, reseteamos el buffer de este check
            data.getCheckData().setPostABuffer(0);
            return;
        }

        final double speedH = data.getVelocityH();
        
        // 1. Aumentamos el umbral. 0.1 es demasiado sensible.
        // 0.22 es aproximadamente la velocidad de caminar.
        if (speedH > 0.2) {
            
            // 2. Implementamos un Buffer de Gracia (Packet-based)
            // Permitimos hasta 2-3 paquetes de "vuelo" para absorber el ping.
            int buffer = data.getCheckData().getPostABuffer();
            buffer++;

            // Si el jugador envía más de 3 paquetes de movimiento sin confirmar el TP
            if (buffer > 3) {
                flag(player, data, 1.0,
                    String.format("action before confirm: speedH=%.4f buffer=%d", speedH, buffer),
                    tick);
            }
            
            data.getCheckData().setPostABuffer(buffer);
        }
    }
}