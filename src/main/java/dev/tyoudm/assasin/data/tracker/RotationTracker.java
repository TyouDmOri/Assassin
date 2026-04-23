package dev.tyoudm.assasin.data.tracker;

import lombok.Getter;

@Getter
public final class RotationTracker {

    private float yaw, pitch;
    private float lastYaw, lastPitch;
    private float deltaYaw, deltaPitch;

    public void update(float yaw, float pitch) {
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;

        this.yaw = yaw;
        this.pitch = pitch;

        // Calcular deltas
        this.deltaYaw = Math.abs(yaw - lastYaw);
        this.deltaPitch = Math.abs(pitch - lastPitch);
    }

    /**
     * Devuelve el Yaw normalizado entre -180 y 180 grados.
     * Esto arregla el error de los 871.4° que veías en el log.
     */
    public float getNormalizedYaw() {
        float normalized = yaw % 360.0f;
        if (normalized > 180.0f) normalized -= 360.0f;
        if (normalized < -180.0f) normalized += 360.0f;
        return normalized;
    }
}