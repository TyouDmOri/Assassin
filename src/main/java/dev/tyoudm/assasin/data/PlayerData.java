/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data;

import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.tracker.ActionTracker;
import dev.tyoudm.assasin.data.tracker.AttackTracker;
import dev.tyoudm.assasin.data.tracker.BlockTracker;
import dev.tyoudm.assasin.data.tracker.CombatTracker;
import dev.tyoudm.assasin.data.tracker.InputTracker;
import dev.tyoudm.assasin.data.tracker.InventoryTracker;
import dev.tyoudm.assasin.data.tracker.MacroStateTracker;
import dev.tyoudm.assasin.data.tracker.MountTracker;
import dev.tyoudm.assasin.data.tracker.MovementTracker;
import dev.tyoudm.assasin.data.tracker.RotationTracker;
import dev.tyoudm.assasin.data.tracker.VelocityTracker;
import dev.tyoudm.assasin.data.prediction.ElytraPredictor;
import dev.tyoudm.assasin.exempt.ExemptManager;
import dev.tyoudm.assasin.latency.LatencyTracker;
import dev.tyoudm.assasin.mitigation.replay.ReplayBuffer;

import dev.tyoudm.assasin.mitigation.buffer.ViolationBuffer;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Per-player data container for ASSASIN.
 *
 * <p>Holds all mutable state associated with a single online player:
 * position history, velocity, latency, exemptions, violation levels,
 * and all tracker instances (FASE 4).
 *
 * <h2>Threading model</h2>
 * <ul>
 *   <li>Packet handlers (netty thread) write position, velocity, rotation.</li>
 *   <li>Check processors (async ForkJoinPool) read and compute VL.</li>
 *   <li>Main thread reads for GUI, commands, and event handlers.</li>
 * </ul>
 * All fields that cross thread boundaries use atomic types.
 * Tracker fields are accessed from the netty thread only and use plain references.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class PlayerData {

    // ─── Identity ─────────────────────────────────────────────────────────────

    private final UUID   uuid;
    private final String name;
    private final long   joinTick;

    // ─── Position ─────────────────────────────────────────────────────────────

    private final AtomicReference<Double> x     = new AtomicReference<>(0.0);
    private final AtomicReference<Double> y     = new AtomicReference<>(0.0);
    private final AtomicReference<Double> z     = new AtomicReference<>(0.0);
    private final AtomicReference<Double> lastX = new AtomicReference<>(0.0);
    private final AtomicReference<Double> lastY = new AtomicReference<>(0.0);
    private final AtomicReference<Double> lastZ = new AtomicReference<>(0.0);

    // ─── Rotation ─────────────────────────────────────────────────────────────

    private final AtomicReference<Float> yaw       = new AtomicReference<>(0.0f);
    private final AtomicReference<Float> pitch     = new AtomicReference<>(0.0f);
    private final AtomicReference<Float> lastYaw   = new AtomicReference<>(0.0f);
    private final AtomicReference<Float> lastPitch = new AtomicReference<>(0.0f);

    // ─── Ground ───────────────────────────────────────────────────────────────

    private volatile boolean onGround     = true;
    private volatile boolean lastOnGround = true;

    // ─── Velocity ─────────────────────────────────────────────────────────────

    private final AtomicReference<Double> velocityH = new AtomicReference<>(0.0);
    private final AtomicReference<Double> velocityY = new AtomicReference<>(0.0);

    // ─── Latency ──────────────────────────────────────────────────────────────

    private final AtomicInteger ping                     = new AtomicInteger(0);
    private final AtomicLong    lastTransactionTick      = new AtomicLong(0L);
    private final AtomicLong    lastTransactionConfirmTick = new AtomicLong(0L);

    // ─── Packet counter ───────────────────────────────────────────────────────

    private final AtomicLong packetCount = new AtomicLong(0L);

    // ─── Game state ───────────────────────────────────────────────────────────

    private volatile boolean sprinting    = false;
    private volatile boolean sneaking     = false;
    private volatile boolean flying       = false;
    private volatile boolean elytraActive = false;
    private volatile boolean inVehicle    = false;

    // ─── Exemptions ───────────────────────────────────────────────────────────

    private final ExemptManager exemptManager = new ExemptManager();

    // ─── Violation buffers (per-player, per-check) ────────────────────────────

    private final Map<CheckType, ViolationBuffer> violationBuffers =
        new EnumMap<>(CheckType.class);

    // ─── Latency subsystem (FASE 3) ───────────────────────────────────────────

    private LatencyTracker latencyTracker;

    // ─── Trackers (FASE 4) ────────────────────────────────────────────────────

    private MovementTracker   movementTracker;
    private RotationTracker   rotationTracker;
    private CombatTracker     combatTracker;
    private VelocityTracker   velocityTracker;
    private dev.tyoudm.assasin.data.tracker.LatencyTracker trackerLatency;
    private BlockTracker      blockTracker;
    private MountTracker      mountTracker;
    private AttackTracker     attackTracker;
    private InputTracker      inputTracker;
    private InventoryTracker  inventoryTracker;
    private ActionTracker     actionTracker;
    private MacroStateTracker macroStateTracker;


    /** Per-player elytra physics predictor (FASE 5). */
    private ElytraPredictor   elytraPredictor;

    /** Per-player replay buffer — last 200 ticks (FASE 6). */
    private ReplayBuffer      replayBuffer;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public PlayerData(final UUID uuid, final String name, final long joinTick) {
        this.uuid     = uuid;
        this.name     = name;
        this.joinTick = joinTick;
    }

    // ─── Tracker initialization ───────────────────────────────────────────────

    /**
     * Initializes the {@link LatencyTracker} (FASE 3).
     * Called by {@code PlayerEventHandler} on join.
     */
    public void initLatencyTracker() {
        this.latencyTracker = new LatencyTracker(this);
    }

    /**
     * Initializes all FASE 4 trackers.
     * Called by {@code PlayerEventHandler} on join, after latency tracker.
     */
    public void initTrackers() {
        this.movementTracker   = new MovementTracker();
        this.rotationTracker   = new RotationTracker();
        this.combatTracker     = new CombatTracker();
        this.velocityTracker   = new VelocityTracker();
        this.trackerLatency    = new dev.tyoudm.assasin.data.tracker.LatencyTracker();
        this.blockTracker      = new BlockTracker();
        this.mountTracker      = new MountTracker();
        this.attackTracker     = new AttackTracker();
        this.inputTracker      = new InputTracker();
        this.inventoryTracker  = new InventoryTracker();
        this.actionTracker     = new ActionTracker();
        this.macroStateTracker = new MacroStateTracker();
        this.elytraPredictor   = new ElytraPredictor();
        this.replayBuffer      = new ReplayBuffer();
    }

    // ─── Position ─────────────────────────────────────────────────────────────

    public double getX()     { return x.get(); }
    public double getY()     { return y.get(); }
    public double getZ()     { return z.get(); }
    public double getLastX() { return lastX.get(); }
    public double getLastY() { return lastY.get(); }
    public double getLastZ() { return lastZ.get(); }

    public void setPosition(final double nx, final double ny, final double nz) {
        lastX.set(x.getAndSet(nx));
        lastY.set(y.getAndSet(ny));
        lastZ.set(z.getAndSet(nz));
    }

    // ─── Rotation ─────────────────────────────────────────────────────────────

    public float getYaw()       { return yaw.get(); }
    public float getPitch()     { return pitch.get(); }
    public float getLastYaw()   { return lastYaw.get(); }
    public float getLastPitch() { return lastPitch.get(); }

    public void setRotation(final float ny, final float np) {
        lastYaw.set(yaw.getAndSet(ny));
        lastPitch.set(pitch.getAndSet(np));
    }

    // ─── Ground ───────────────────────────────────────────────────────────────

    public boolean isOnGround()  { return onGround; }
    public boolean wasOnGround() { return lastOnGround; }

    public void setOnGround(final boolean value) {
        lastOnGround = onGround;
        onGround     = value;
    }

    // ─── Velocity ─────────────────────────────────────────────────────────────

    public double getVelocityH() { return velocityH.get(); }
    public double getVelocityY() { return velocityY.get(); }
    public void setVelocityH(final double v) { velocityH.set(v); }
    public void setVelocityY(final double v) { velocityY.set(v); }

    // ─── Latency ──────────────────────────────────────────────────────────────

    public int  getPing()                             { return ping.get(); }
    public void setPing(final int ms)                 { ping.set(ms); }
    public long getLastTransactionTick()              { return lastTransactionTick.get(); }
    public void setLastTransactionTick(final long t)  { lastTransactionTick.set(t); }
    public long getLastTransactionConfirmTick()       { return lastTransactionConfirmTick.get(); }
    public void setLastTransactionConfirmTick(final long t) { lastTransactionConfirmTick.set(t); }

    // ─── Packet counter ───────────────────────────────────────────────────────

    public long getPacketCount()   { return packetCount.get(); }
    public long incrementPackets() { return packetCount.incrementAndGet(); }

    // ─── Game state ───────────────────────────────────────────────────────────

    public boolean isSprinting()           { return sprinting; }
    public void    setSprinting(boolean v) { sprinting = v; }
    public boolean isSneaking()            { return sneaking; }
    public void    setSneaking(boolean v)  { sneaking = v; }
    public boolean isFlying()              { return flying; }
    public void    setFlying(boolean v)    { flying = v; }
    public boolean isElytraActive()        { return elytraActive; }
    public void    setElytraActive(boolean v) { elytraActive = v; }
    public boolean isInVehicle()           { return inVehicle; }
    public void    setInVehicle(boolean v) { inVehicle = v; }

    // ─── Exemptions ───────────────────────────────────────────────────────────

    public ExemptManager getExemptManager() { return exemptManager; }

    // ─── Violation buffers ────────────────────────────────────────────────────

    /**
     * Returns the per-player {@link ViolationBuffer} for the given check type,
     * creating it lazily with the given maxVl if it doesn't exist yet.
     *
     * @param type  the check type
     * @param maxVl the maximum VL (used only on first creation)
     * @return the violation buffer for this player + check
     */
    public ViolationBuffer getViolationBuffer(final CheckType type, final double maxVl) {
        return violationBuffers.computeIfAbsent(type, k -> new ViolationBuffer(maxVl));
    }

    // ─── Latency tracker ──────────────────────────────────────────────────────

    public LatencyTracker getLatencyTracker() { return latencyTracker; }

    // ─── Trackers ─────────────────────────────────────────────────────────────

    public MovementTracker   getMovementTracker()   { return movementTracker; }
    public RotationTracker   getRotationTracker()   { return rotationTracker; }
    public CombatTracker     getCombatTracker()     { return combatTracker; }
    public VelocityTracker   getVelocityTracker()   { return velocityTracker; }
    public dev.tyoudm.assasin.data.tracker.LatencyTracker getTrackerLatency() { return trackerLatency; }
    public BlockTracker      getBlockTracker()      { return blockTracker; }
    public MountTracker      getMountTracker()      { return mountTracker; }
    public AttackTracker     getAttackTracker()     { return attackTracker; }
    public InputTracker      getInputTracker()      { return inputTracker; }
    public InventoryTracker  getInventoryTracker()  { return inventoryTracker; }
    public ActionTracker     getActionTracker()     { return actionTracker; }
    public MacroStateTracker getMacroStateTracker() { return macroStateTracker; }
    public ElytraPredictor   getElytraPredictor()   { return elytraPredictor; }
    public ReplayBuffer      getReplayBuffer()       { return replayBuffer; }
    public AttributeTracker getAttributeTracker() { return attributeTracker; }
    public VelocityTracker getVelocityTracker() { return velocityTracker; }
    // ─── Identity ─────────────────────────────────────────────────────────────

    public UUID   getUuid()     { return uuid; }
    public String getName()     { return name; }
    public long   getJoinTick() { return joinTick; }

    @Override
    public String toString() {
        return "PlayerData{uuid=" + uuid + ", name=" + name + "}";
    }
}
