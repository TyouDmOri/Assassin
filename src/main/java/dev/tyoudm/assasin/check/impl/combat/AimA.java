/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.combat;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.RotationTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.MathUtil;
import org.bukkit.entity.Player;

/**
 * AimA — Análisis de Sensibilidad y GCD (Greatest Common Divisor).
 * 
 * Basado en la fórmula de rotación de Minecraft:
 * Δθ = pixels * f^3 * 8 * 0.15
 */
@CheckInfo(
    name              = "AimA",
    type              = CheckType.AIM_A,
    category          = CheckCategory.COMBAT,
    description       = "Detecta Aim Assist mediante el análisis de la constante de sensibilidad (GCD).",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "combat"
)
public final class AimA extends Check {

    private static final int SAMPLES_REQUIRED = 40;
    
    // Usamos una escala mucho mayor para evitar la pérdida de precisión en sensibilidades bajas.
    // 2^24 es un estándar en AntiCheats avanzados para normalizar floats de rotación.
    private static final double EXPAND = Math.pow(2, 24);

    public AimA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        // Exenciones cruciales para evitar falsos positivos
        if (isExemptAny(data, tick, 
            ExemptType.TELEPORT_PENDING, 
            ExemptType.SETBACK, 
            ExemptType.CINEMATIC_CAMERA, // <--- Nueva exención necesaria
            ExemptType.VEHICLE)) return;

        final RotationTracker rt = data.getRotationTracker();
        
        float yaw = data.getRotationTracker().getYaw();
        float pitch = data.getRotationTracker().getPitch();
        float lastYaw = data.getRotationTracker().getLastYaw();
        float lastPitch = data.getRotationTracker().getLastPitch();

        double deltaYaw = Math.abs(yaw - lastYaw);
        double deltaPitch = Math.abs(pitch - lastPitch);

        // Ignorar rotaciones nulas o movimientos cinemáticos (muy pequeños)
        if (deltaYaw < 0.001 || deltaPitch < 0.001) return;

        // Implementación de análisis dinámico por muestra en lugar de intervalo fijo
        // Esto permite detectar cheats que se activan/desactivan rápido (Toggle)
        long currentGcd = MathUtil.gcd(
            (long) (deltaYaw * EXPAND), 
            (long) (deltaPitch * EXPAND)
        );

        // El GCD de un humano con ratón real suele estar relacionado con 
        // la constante de sensibilidad de MC (0.15 * f^3).
        // Si el GCD es "extrañamente perfecto" o estático, acumulamos evidencia.
        if (currentGcd > 20000L) { // Umbral normalizado para escala 2^24
            double buffer = data.getCheckData().getAimABuffer();
            buffer += 1.0;

            if (buffer > SAMPLES_REQUIRED) {
                flag(player, data, 1.0, 
                    String.format("gcd=%d scaled_delta=%.2f", currentGcd, deltaYaw * EXPAND), 
                    tick);
                buffer = 0;
            }
            data.getCheckData().setAimABuffer(buffer);
        } else {
            // Reducción lenta de buffer para evitar flags por "flicks" accidentales
            data.getCheckData().setAimABuffer(Math.max(0, data.getCheckData().getAimABuffer() - 0.5));
        }
    }
}