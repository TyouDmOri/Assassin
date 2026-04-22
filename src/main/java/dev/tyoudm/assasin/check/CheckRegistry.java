/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check;

import dev.tyoudm.assasin.check.impl.combat.*;
import dev.tyoudm.assasin.check.impl.macro.*;
import dev.tyoudm.assasin.check.impl.misc.*;
import dev.tyoudm.assasin.check.impl.mount.*;
import dev.tyoudm.assasin.check.impl.movement.*;
import dev.tyoudm.assasin.check.impl.player.*;
import dev.tyoudm.assasin.check.impl.world.*;
import dev.tyoudm.assasin.core.LegitTechniqueRegistry;
import dev.tyoudm.assasin.handler.async.AsyncProcessor;
import dev.tyoudm.assasin.mitigation.MitigationEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Instantiates and stores all ASSASIN check instances.
 *
 * <p>Each check is a singleton per server (not per-player). Per-player
 * state lives inside {@link dev.tyoudm.assasin.data.PlayerData} via
 * trackers and violation buffers.
 *
 * <p>Checks are indexed by {@link CheckType} for O(1) lookup and also
 * grouped by {@link CheckCategory} for batch dispatch.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CheckRegistry {

    /** All registered checks, indexed by type. */
    private final Map<CheckType, Check> byType = new EnumMap<>(CheckType.class);

    /** All registered checks, grouped by category. */
    private final Map<CheckCategory, List<Check>> byCategory = new EnumMap<>(CheckCategory.class);

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Instantiates every check in dependency order.
     *
     * @param engine        the shared mitigation engine
     * @param legitRegistry the legit technique registry (for false-flag prevention)
     * @param asyncProcessor the async processor (for FFT-heavy checks)
     */
    public CheckRegistry(final MitigationEngine engine,
                         final LegitTechniqueRegistry legitRegistry,
                         final AsyncProcessor asyncProcessor) {
        registerAll(engine, legitRegistry, asyncProcessor);
    }

    // ─── Registration ─────────────────────────────────────────────────────────

    private void registerAll(final MitigationEngine e,
                             final LegitTechniqueRegistry lr,
                             final AsyncProcessor ap) {
        // ── Movement ─────────────────────────────────────────────────────────
        register(new SpeedA(e));
        register(new SpeedB(e));
        register(new FlyA(e));
        register(new FlyB(e));
        register(new NoFallA(e));
        register(new JesusA(e));
        register(new StepA(e));
        register(new TimerA(e));
        register(new PhaseA(e));
        register(new StrafeA(e));
        register(new ElytraA(e));
        register(new JumpResetA(e, lr));
        register(new JumpResetB(e));
        register(new MotionA(e));

        // ── Mount ─────────────────────────────────────────────────────────────
        register(new MountSpeedA(e));
        register(new NautilusA(e));
        register(new ZombieHorseA(e));
        register(new MountFlyA(e));

        // ── Combat ────────────────────────────────────────────────────────────
        register(new KillauraA(e));
        register(new KillauraB(e));
        register(new KillauraC(e));
        register(new KillauraD(e));
        register(new AimA(e));
        register(new AimB(e));
        register(new AimC(e));
        register(new ReachA(e));
        register(new ReachB(e));
        register(new HitboxA(e));
        register(new AutoClickerA(e, lr));
        register(new AutoClickerB(e));
        register(new AutoClickerC(e, ap));
        register(new VelocityA(e, lr));
        register(new VelocityB(e));
        register(new VelocityC(e));
        register(new CriticalsA(e));
        register(new SpearA(e));
        register(new MaceDmgA(e));
        register(new MaceDmgB(e));
        register(new MaceDmgC(e));
        register(new AttributeSwapA(e));

        // ── World ─────────────────────────────────────────────────────────────
        register(new ScaffoldA(e));
        register(new ScaffoldB(e, lr));
        register(new ScaffoldC(e));
        register(new TowerA(e));
        register(new NukerA(e));
        register(new FastBreakA(e));
        register(new AirPlaceA(e));
        register(new FastPlaceA(e));
        register(new LiquidWalkA(e));

        // ── Player ────────────────────────────────────────────────────────────
        register(new BadPacketsA(e));
        register(new BadPacketsB(e));
        register(new BadPacketsC(e));
        register(new BadPacketsD(e));
        register(new BadPacketsE(e));
        register(new BadPacketsF(e));
        register(new AutoTotemA(e, lr));
        register(new AutoTotemB(e, lr));
        register(new AutoTotemC(e));
        register(new AutoTotemD(e));
        register(new AutoArmorA(e));
        register(new ChestStealerA(e));
        register(new InventoryA(e));
        register(new InventoryB(e));
        register(new FastEatA(e));
        register(new BookA(e));
        register(new CrashA(e));
        register(new PostA(e));
        register(new TimerPacketA(e));

        // ── Macro ─────────────────────────────────────────────────────────────
        register(new MacroClickerA(e, ap));
        register(new MacroCorrelationA(e));
        register(new MacroInputA(e));
        register(new MacroInventoryA(e));
        register(new MacroSequenceA(e));
        register(new MacroTimingA(e));
        register(new MacroVarianceA(e));

        // ── Misc ──────────────────────────────────────────────────────────────
        register(new ClientBrandA(e));
        register(new GhostHandA(e));
        register(new NameSpoofA(e));
    }

    private void register(final Check check) {
        final CheckInfo info = check.getInfo();
        byType.put(info.type(), check);
        byCategory.computeIfAbsent(info.category(), k -> new ArrayList<>()).add(check);
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /**
     * Returns the check for the given type, or {@code null} if not registered.
     *
     * @param type the check type
     * @return the check instance, or {@code null}
     */
    public Check getCheck(final CheckType type) {
        return byType.get(type);
    }

    /**
     * Returns all checks in the given category (unmodifiable).
     *
     * @param category the category
     * @return list of checks, never {@code null}
     */
    public List<Check> getChecks(final CheckCategory category) {
        return byCategory.getOrDefault(category, Collections.emptyList());
    }

    /**
     * Returns all registered checks (unmodifiable).
     *
     * @return all checks
     */
    public List<Check> getAllChecks() {
        return Collections.unmodifiableList(new ArrayList<>(byType.values()));
    }

    /**
     * Returns the total number of registered checks.
     *
     * @return check count
     */
    public int size() {
        return byType.size();
    }
}
