package dev.tyoudm.assasin.data;

import dev.tyoudm.assasin.data.tracker.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import java.util.UUID;

@Getter
public final class PlayerData {

    private final UUID uuid;
    private final Player player;

    // ─── Trackers (Motores de datos) ─────────────────────────────────────────
    private final PositionTracker  positionTracker  = new PositionTracker();
    private final RotationTracker  rotationTracker  = new RotationTracker();
    private final InventoryTracker inventoryTracker = new InventoryTracker();
    private final MovementTracker  movementTracker  = new MovementTracker();
    private final PotionTracker    potionTracker    = new PotionTracker();
    private final VelocityTracker  velocityTracker  = new VelocityTracker();

    // ─── Estado General ──────────────────────────────────────────────────────
    @Setter private boolean sprinting;
    @Setter private boolean sneaking;
    @Setter private boolean onGround;
    @Setter private boolean usingItem;
    
    // ─── Buffers de Checks (Evitan falsos positivos) ─────────────────────────
    private final CheckData checkData = new CheckData();

    public PlayerData(Player player) {
        this.player = player;
        this.uuid   = player.getUniqueId();
    }

    /**
     * Devuelve si el jugador tiene alguna excepción activa (ej. acaba de recibir golpe).
     */
    public boolean isExempt(long tick) {
        return velocityTracker.isExpectingVelocity(tick) || 
               player.isInsideVehicle() || 
               player.isGliding();
    }

    /**
     * Clase interna para almacenar los buffers de cada check.
     * Esto evita que el VL suba al primer tick de lag.
     */
    @Getter @Setter
    public static class CheckData {
        // Movimiento
        private double speedBuffer;
        private double verticalBuffer;
        private double strafeBuffer;
        private double jesusBuffer;
        
        // Mundo / Interacción
        private double scaffoldBuffer;
        private double airPlaceBuffer;
        private double invMoveBuffer;
        
        // Combate
        private double reachBuffer;
        private double killauraBuffer;

        public void decayAll() {
            this.speedBuffer    = Math.max(0, speedBuffer - 0.1);
            this.verticalBuffer = Math.max(0, verticalBuffer - 0.1);
            this.strafeBuffer   = Math.max(0, strafeBuffer - 0.1);
            this.jesusBuffer    = Math.max(0, jesusBuffer - 0.1);
            this.scaffoldBuffer = Math.max(0, scaffoldBuffer - 0.1);
            this.airPlaceBuffer = Math.max(0, airPlaceBuffer - 0.1);
            this.invMoveBuffer  = Math.max(0, invMoveBuffer - 0.1);
        }
    }

    /** Resetea los datos al morir o cambiar de mundo */
    public void reset() {
        positionTracker.update(0,0,0, true, player.getWorld());
        rotationTracker.update(0, 0);
        inventoryTracker.reset();
        checkData.decayAll();
        sprinting = false;
        sneaking  = false;
    }
}