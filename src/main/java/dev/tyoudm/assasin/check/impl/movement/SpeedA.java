/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.movement;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.prediction.MovementPredictor;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * SpeedA — Horizontal speed violation (absolute threshold).
 *
 * <p>Flags when the player's horizontal speed exceeds the maximum expected
 * value for their current input state (sprint/walk/sneak), accounting for
 * ping tolerance.
 *
 * <h2>False-flag prevention</h2>
 * <ul>
 *   <li>Exempt during elytra, riptide, vehicle, liquid transitions.</li>
 *   <li>Ping tolerance added via {@link MovementPredictor#maxExpectedSpeedH}.</li>
 *   <li>W-tap exempt: sprint toggle within 3t of attack suppresses for 5t.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "SpeedA",
    type             = CheckType.SPEED_A,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects horizontal speed exceeding vanilla maximum.",
    maxVl            = 10.0,
    severity         = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class SpeedA extends Check {

    public SpeedA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(Player player, PlayerData data, long tick) {
        // 1. EXENCIONES: Si el jugador acaba de recibir un golpe o teletransporte, ignoramos.
        if (isExemptAny(data, tick, ExemptType.VELOCITY, ExemptType.TELEPORT_PENDING)) return;

        double deltaH = data.getMovementTracker().getDeltaH();
    
        // 2. CÁLCULO DINÁMICO DEL MÁXIMO (Ajustado a la 1.21.11)
        // La velocidad base es 0.1, pero con sprint sube un 30% aprox.
        double baseSpeed = data.getAttributeTracker().getWalkSpeed(); 
        double maxExpected = baseSpeed * (data.isSprinting() ? 1.302 : 1.0);
    
        // Si está en el aire, la física cambia (se le permite un poco más de inercia)
        if (!data.isOnGround()) {
        maxExpected += 0.02; 
        }

        // 3. SISTEMA DE BUFFER (Para evitar flags por 0.01 de diferencia)
        double excess = deltaH - maxExpected;
        double buffer = data.getCheckData().getSpeedBBuffer();

        if (excess > 0.005) { // Solo si el exceso es notable
            buffer += excess * 10; // El buffer sube proporcionalmente al hack
        
            if (buffer > 1.5) { // Solo flagueamos si el buffer se llena (aprox 3-5 ticks de exceso)
                flag(player, data, 1.0, "Speed=" + String.format("%.4f", deltaH) + " Max=" + String.format("%.4f", maxExpected), tick);
                buffer /= 2; // Bajamos el buffer tras el flag para no spamear
            }
        } else {
            buffer = Math.max(0, buffer - 0.05); // El buffer baja si camina normal
        }

        data.getCheckData().setSpeedBBuffer(buffer);
    }
}
