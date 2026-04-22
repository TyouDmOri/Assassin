/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.core;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registry of legit PvP technique tolerances.
 *
 * <p>Centralizes all thresholds that distinguish legitimate player behavior
 * from cheating. Values are loaded from {@code legit-techniques.yml} in
 * FASE 18; until then, defaults defined here are used.
 *
 * <h2>Techniques covered</h2>
 * <ul>
 *   <li>W-tap / S-tap / A/D-tap</li>
 *   <li>Jump-reset (legit, high σ)</li>
 *   <li>Block-hit with shield</li>
 *   <li>Crit-tapping</li>
 *   <li>Butterfly / Jitter / Drag click</li>
 *   <li>Attribute-swap between attacks</li>
 *   <li>Elytra dive (2 → 40 b/s in ~8s is vanilla)</li>
 *   <li>Riptide trident</li>
 *   <li>Godbridge / Speed-bridge / Ninja-bridge</li>
 *   <li>Pearl phase (3s exempt)</li>
 *   <li>Legit totem-swap</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class LegitTechniqueRegistry {

    // ─── Technique enum ───────────────────────────────────────────────────────

    /**
     * Enumeration of all recognized legit PvP techniques.
     */
    public enum Technique {
        WTAP,
        STAP,
        ADTAP,
        JUMP_RESET,
        BLOCK_HIT,
        CRIT_TAPPING,
        BUTTERFLY_CLICK,
        JITTER_CLICK,
        DRAG_CLICK,
        ATTRIBUTE_SWAP,
        ELYTRA_DIVE,
        RIPTIDE,
        GODBRIDGE,
        SPEED_BRIDGE,
        NINJA_BRIDGE,
        PEARL_PHASE,
        TOTEM_SWAP,
        COMBO_RESET
    }

    // ─── Tolerance record ─────────────────────────────────────────────────────

    /**
     * Tolerance parameters for a single legit technique.
     *
     * @param exemptTicks      how many ticks to suppress related checks
     * @param minSamples       minimum sample count before flagging
     * @param sigmaThreshold   minimum σ (standard deviation) to consider legit
     * @param successRateLimit maximum success rate before flagging (0–1)
     * @param kbMultiplier     knockback multiplier applied when technique is active
     */
    public record Tolerance(
        int    exemptTicks,
        int    minSamples,
        double sigmaThreshold,
        double successRateLimit,
        double kbMultiplier
    ) {
        /** Tolerance with only exemptTicks set; other fields use neutral defaults. */
        public static Tolerance ofExempt(final int ticks) {
            return new Tolerance(ticks, 0, 0.0, 1.0, 1.0);
        }
    }

    // ─── Default tolerances ───────────────────────────────────────────────────

    private static final Map<Technique, Tolerance> DEFAULTS;

    static {
        final Map<Technique, Tolerance> m = new EnumMap<>(Technique.class);

        // W-tap: sprint off→on ≤3t around ATTACK. Exempt VelocityA 5t.
        m.put(Technique.WTAP,            new Tolerance(5,  0,  0.0,  1.0, 1.0));

        // S-tap: backward input ≤2t + re-sprint. Predictor re-baseline.
        m.put(Technique.STAP,            new Tolerance(3,  0,  0.0,  1.0, 1.0));

        // A/D-tap: lateral oscillation with stable yaw. StrafeA tolerates Δ≤0.15.
        m.put(Technique.ADTAP,           new Tolerance(2,  0,  0.0,  1.0, 1.0));

        // Jump-reset legit: σ≥1.5, success rate <95% in n≥8.
        m.put(Technique.JUMP_RESET,      new Tolerance(0,  8,  1.5,  0.95, 1.0));

        // Block-hit: expected KB * 0.5 while shield USE_ITEM active.
        m.put(Technique.BLOCK_HIT,       new Tolerance(0,  0,  0.0,  1.0, 0.5));

        // Crit-tapping: onGround=false + motionY<0 required.
        m.put(Technique.CRIT_TAPPING,    new Tolerance(0,  0,  0.0,  1.0, 1.0));

        // Butterfly / Jitter / Drag click: bimodal distribution, high kurtosis.
        m.put(Technique.BUTTERFLY_CLICK, new Tolerance(0, 20,  0.0,  1.0, 1.0));
        m.put(Technique.JITTER_CLICK,    new Tolerance(0, 20,  0.0,  1.0, 1.0));
        m.put(Technique.DRAG_CLICK,      new Tolerance(0, 20,  0.0,  1.0, 1.0));

        // Attribute-swap: re-baseline VelocityA, MaceDmgA, AutoClickerA.
        m.put(Technique.ATTRIBUTE_SWAP,  new Tolerance(3,  0,  0.0,  1.0, 1.0));

        // Elytra dive: ElytraA only flags persistent deviation ≥12t.
        m.put(Technique.ELYTRA_DIVE,     new Tolerance(12, 0,  0.0,  1.0, 1.0));

        // Riptide: exempt SpeedA/B, FlyA for 20t.
        m.put(Technique.RIPTIDE,         new Tolerance(20, 0,  0.0,  1.0, 1.0));

        // Godbridge: pitch ~80° fixed, ScaffoldB requires σ>0.1° jitter.
        m.put(Technique.GODBRIDGE,       new Tolerance(0,  0,  0.1,  1.0, 1.0));

        // Speed-bridge / Ninja-bridge: raytrace valid, no rotation isolation.
        m.put(Technique.SPEED_BRIDGE,    new Tolerance(0,  0,  0.0,  1.0, 1.0));
        m.put(Technique.NINJA_BRIDGE,    new Tolerance(0,  0,  0.0,  1.0, 1.0));

        // Pearl phase: exempt PhaseA for 60t (3s).
        m.put(Technique.PEARL_PHASE,     new Tolerance(60, 0,  0.0,  1.0, 1.0));

        // Totem-swap legit: reswap ≥5t, σ>1.5, no multitasking.
        m.put(Technique.TOTEM_SWAP,      new Tolerance(0,  5,  1.5,  1.0, 1.0));

        // Combo-reset: multi-target with rotation pre-hit.
        m.put(Technique.COMBO_RESET,     new Tolerance(0,  0,  0.0,  1.0, 1.0));

        DEFAULTS = Collections.unmodifiableMap(m);
    }

    // ─── Instance state ───────────────────────────────────────────────────────

    /** Active tolerances — starts as a copy of defaults, overridden by config. */
    private final Map<Technique, Tolerance> tolerances = new EnumMap<>(DEFAULTS);

    // ─── API ──────────────────────────────────────────────────────────────────

    /**
     * Returns the {@link Tolerance} for the given technique.
     *
     * @param technique the technique
     * @return tolerance (never {@code null} — falls back to a neutral default)
     */
    public Tolerance get(final Technique technique) {
        return tolerances.getOrDefault(technique, Tolerance.ofExempt(0));
    }

    /**
     * Overrides the tolerance for a technique (called by ConfigManager in FASE 18).
     *
     * @param technique the technique to override
     * @param tolerance the new tolerance
     */
    public void set(final Technique technique, final Tolerance tolerance) {
        tolerances.put(technique, tolerance);
    }

    /**
     * Resets all tolerances to their compiled-in defaults.
     */
    public void resetToDefaults() {
        tolerances.clear();
        tolerances.putAll(DEFAULTS);
    }

    /**
     * Returns an unmodifiable view of all active tolerances.
     *
     * @return unmodifiable tolerance map
     */
    public Map<Technique, Tolerance> all() {
        return Collections.unmodifiableMap(tolerances);
    }
}
