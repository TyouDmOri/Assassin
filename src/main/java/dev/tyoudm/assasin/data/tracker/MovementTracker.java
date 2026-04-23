package dev.tyoudm.assasin.data.tracker;

import dev.tyoudm.assasin.util.RingBuffer;

/**
 * Tracks per-player movement history for movement checks.
 */
public final class MovementTracker {

    public static final int HISTORY = 20;

    // ─── History buffers ──────────────────────────────────────────────────────
    private final RingBuffer.OfDouble speedH = new RingBuffer.OfDouble(HISTORY);
    private final RingBuffer.OfDouble speedY = new RingBuffer.OfDouble(HISTORY);
    private final RingBuffer.OfDouble groundHistory = new RingBuffer.OfDouble(HISTORY);

    // ─── Current-tick state ───────────────────────────────────────────────────
    private double currentSpeedH;
    private double lastSpeedH; // NECESARIO PARA PREDICCIÓN
    private double currentSpeedY;
    
    private int airTicks;
    private int groundTicks;
    private int sprintTicks;
    private double fallDistance;
    
    private boolean jumped; // DETECTA EL TICK DEL SALTO
    private boolean lastOnGround;

    // ─── Teleport & Timer ─────────────────────────────────────────────────────
    private int movementPackets;
    private long windowStartTick;
    private int pendingTeleportId = -1;
    private long lastTeleportTime;

    /**
     * @param dx X delta
     * @param dy Y delta
     * @param dz Z delta
     * @param onGround client-reported onGround flag
     */
    public void update(final double dx, final double dy, final double dz, final boolean onGround) {
        // 1. Guardar estado anterior
        this.lastSpeedH = this.currentSpeedH;
        
        // 2. Calcular valores actuales
        this.currentSpeedH = Math.sqrt(dx * dx + dz * dz);
        this.currentSpeedY = dy;

        // 3. Detección de salto (Clave para SpeedB)
        // En Minecraft, un salto ocurre cuando dy > 0, el cliente dice que no está en el suelo, 
        // pero en el tick anterior SÍ lo estaba.
        this.jumped = dy > 0.0 && !onGround && lastOnGround;

        // 4. Actualizar Buffers
        speedH.add(currentSpeedH);
        speedY.add(currentSpeedY);
        groundHistory.add(onGround ? 1.0 : 0.0);

        // 5. Lógica de estados
        if (onGround) {
            airTicks = 0;
            groundTicks++;
            fallDistance = 0.0;
        } else {
            airTicks++;
            groundTicks = 0;
            if (dy < 0) fallDistance += Math.abs(dy);
        }

        // Umbral de sprint ajustable para 1.21.11
        sprintTicks = currentSpeedH > 0.2806 ? sprintTicks + 1 : 0;
        
        this.lastOnGround = onGround;
        movementPackets++;
    }

    // ─── Teleport Logic ───────────────────────────────────────────────────────

    public void handleTeleport(int id) {
        this.pendingTeleportId = id;
        this.lastTeleportTime = System.currentTimeMillis();
    }

    public boolean isTeleportPending() {
        return pendingTeleportId != -1;
    }

    public void confirmTeleport(int id) {
        if (this.pendingTeleportId == id) {
            this.pendingTeleportId = -1;
        }
    }

    public long getLastTeleportTime() {
        return lastTeleportTime;
    }

    // ─── Timer Logic ──────────────────────────────────────────────────────────

    public int pollMovementPackets(final long currentTick) {
        if (windowStartTick == 0) {
            windowStartTick = currentTick;
            return -1;
        }
        if (currentTick - windowStartTick < 20) return -1;
        final int count = movementPackets;
        movementPackets = 0;
        windowStartTick = currentTick;
        return count;
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public double getCurrentSpeedH() { return currentSpeedH; }
    public double getLastSpeedH()    { return lastSpeedH; } // Para la predicción
    public double getCurrentSpeedY() { return currentSpeedY; }
    public int getAirTicks()         { return airTicks; }
    public int getGroundTicks()      { return groundTicks; }
    public int getSprintTicks()      { return sprintTicks; }
    public double getFallDistance()  { return fallDistance; }
    public boolean hasJumped()       { return jumped; } // Para SpeedB
    public boolean isOnGround()      { return lastOnGround; }

    public void reset() {
        speedH.clear();
        speedY.clear();
        groundHistory.clear();
        currentSpeedH = 0.0;
        lastSpeedH = 0.0;
        currentSpeedY = 0.0;
        airTicks = 0;
        groundTicks = 0;
        sprintTicks = 0;
        fallDistance = 0.0;
        movementPackets = 0;
        windowStartTick = 0L;
        pendingTeleportId = -1;
        jumped = false;
        lastOnGround = true;
    }
}