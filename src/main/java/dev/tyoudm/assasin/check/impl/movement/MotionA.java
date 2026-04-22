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
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.PotionEffectType;
import org.bukkit.entity.Player;

@CheckInfo(
    name              = "MotionA",
    type              = CheckType.MOTION_A,
    category          = CheckCategory.MOVEMENT,
    description       = "Detecta inyección de movimiento ilegal mediante umbrales dinámicos.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class MotionA extends Check {

    public MotionA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        // Exenciones extendidas para mecánicas 1.21
        if (isExemptAny(data, tick,
                ExemptType.ELYTRA_ACTIVE, ExemptType.ELYTRA_BOOST,
                ExemptType.RIPTIDE, ExemptType.VEHICLE,
                ExemptType.VELOCITY, ExemptType.EXPLOSION_KNOCKBACK,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK,
                ExemptType.CLIMBABLE, // Evita flags en escaleras/lianas
                ExemptType.SLIME_BLOCK)) return; // El slime bloque maneja su propia física compleja

        final var mt = data.getMovementTracker();
        if (mt == null || mt.getSpeedH().size() < 2) return;

        // 1. Calcular umbral dinámico Vertical (Y)
        // Base salto (0.42) + margen de error + Jump Boost
        double maxDeltaY = 0.421; 
        
        // Potion Effect Tracking (Asegúrate de tener esto en tu PlayerData/Tracker)
        int jumpBoostLevel = data.getPotionTracker().getEffectLevel(PotionEffectType.JUMP_BOOST);
        if (jumpBoostLevel > 0) {
            maxDeltaY += (jumpBoostLevel * 0.1);
        }

        // Margen extra para Wind Charges (1.21) si el tracker detecta una explosión cerca
        if (data.getCombatTracker().getLastWindChargeTick() > tick - 20) {
            maxDeltaY += 0.8; // Permitir el impulso de la Wind Charge
        }

        // 2. Obtener deltas reales
        final double speedH = data.getVelocityH();
        final double prevSpeedH = mt.getSpeedH().get(mt.getSpeedH().size() - 2);
        final double deltaH = speedH - prevSpeedH;

        final double speedY = data.getVelocityY();
        final double prevSpeedY = mt.getSpeedY().get(mt.getSpeedY().size() - 2);
        final double deltaY = speedY - prevSpeedY; // Cambio neto, no absoluto

        // 3. Lógica de validación
        // Solo flagueamos si el cambio es HACIA ARRIBA (inyección de salto/vuelo)
        // Los cambios hacia abajo suelen ser gravedad o caídas y son menos críticos aquí.
        if (deltaY > maxDeltaY) {
            
            // Usamos un pequeño buffer para evitar falsos positivos por micro-lags de paquetes
            double buffer = data.getCheckData().getMotionABuffer();
            if (++buffer > 1) { 
                flag(player, data, 1.0,
                    String.format("ΔY=%.4f max=%.4f (JumpLvl=%d)", deltaY, maxDeltaY, jumpBoostLevel),
                    tick);
            }
            data.getCheckData().setMotionABuffer(buffer);
        } else {
            data.getCheckData().setMotionABuffer(Math.max(0, data.getCheckData().getMotionABuffer() - 0.1));
        }

        // Validación Horizontal (H)
        // El umbral de 0.5 suele ser seguro a menos que haya explosiones o efectos de velocidad.
        double maxDeltaH = 0.5;
        if (data.getPotionTracker().getEffectLevel(PotionEffectType.SPEED) > 0) {
            maxDeltaH += 0.15;
        }

        if (deltaH > maxDeltaH) {
             flag(player, data, 0.5, String.format("ΔH=%.4f max=%.4f", deltaH, maxDeltaH), tick);
        }
    }
}