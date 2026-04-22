/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation;

import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.alert.AlertContext;
import dev.tyoudm.assasin.alert.AlertManager;
import dev.tyoudm.assasin.handler.async.AsyncProcessor;
import dev.tyoudm.assasin.mitigation.strategy.CancelBlockActionStrategy;
import dev.tyoudm.assasin.mitigation.strategy.CancelDamageStrategy;
import dev.tyoudm.assasin.mitigation.strategy.CancelPacketStrategy;
import dev.tyoudm.assasin.mitigation.strategy.DismountStrategy;
import dev.tyoudm.assasin.mitigation.strategy.FreezeStrategy;
import dev.tyoudm.assasin.mitigation.strategy.KickStrategy;
import dev.tyoudm.assasin.mitigation.strategy.ResyncStrategy;
import dev.tyoudm.assasin.mitigation.strategy.SetbackStrategy;
import dev.tyoudm.assasin.mitigation.strategy.SlowStrategy;
import dev.tyoudm.assasin.mitigation.strategy.VelocityStrategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Central mitigation engine — orchestrates strategy execution for all checks.
 *
 * <p>When a check flags a violation, it calls {@link #mitigate(MitigationContext)}
 * with the appropriate context. The engine:
 * <ol>
 *   <li>Looks up the {@link MitigationProfile} for the check name.</li>
 *   <li>Retrieves the strategies for the current VL.</li>
 *   <li>Executes {@link MitigationPriority#IMMEDIATE} strategies inline
 *       (packet-cancel, on the netty thread).</li>
 *   <li>Schedules {@link MitigationPriority#NORMAL} and
 *       {@link MitigationPriority#HIGH} strategies on the main thread.</li>
 *   <li>Submits {@link MitigationPriority#DEFERRED} strategies to the
 *       {@link AsyncProcessor} (DB writes, Discord, kick).</li>
 * </ol>
 *
 * <h2>Default profiles</h2>
 * Built-in profiles are registered at construction time. They are overridden
 * by {@code mitigation.yml} in FASE 18.
 *
 * <ul>
 *   <li>{@code soft}   — VL≥1: soft setback; VL≥5: slow</li>
 *   <li>{@code medium} — VL≥1: cancel+setback; VL≥8: freeze; VL≥15: kick</li>
 *   <li>{@code hard}   — VL≥1: cancel+hard setback; VL≥5: freeze; VL≥10: kick</li>
 *   <li>{@code combat} — VL≥1: cancel damage; VL≥10: freeze; VL≥20: kick</li>
 *   <li>{@code world}  — VL≥1: cancel block; VL≥10: kick</li>
 *   <li>{@code macro}  — VL 0-4: noop; VL 5-9: silent; VL 10-14: cancel;
 *                        VL 15-19: cancel+resync; VL≥20: kick</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MitigationEngine {

    // ─── Profile registry ─────────────────────────────────────────────────────

    /** Map of profile name → profile. */
    private final Map<String, MitigationProfile> profiles = new HashMap<>();

    /** Default profile used when no specific profile is registered for a check. */
    private static final String DEFAULT_PROFILE = "medium";

    /** Map of check name → profile name override. */
    private final Map<String, String> checkProfileMap = new HashMap<>();

    // ─── Dependencies ─────────────────────────────────────────────────────────

    private final AssasinPlugin    plugin;
    private final AsyncProcessor   asyncProcessor;
    private final Logger           logger;

    /** Injected after construction to avoid circular dependency. */
    private AlertManager alertManager;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates and initializes the mitigation engine with default profiles.
     *
     * @param plugin         the owning plugin
     * @param asyncProcessor the async processor for deferred strategies
     */
    public MitigationEngine(final AssasinPlugin plugin, final AsyncProcessor asyncProcessor) {
        this.plugin         = plugin;
        this.asyncProcessor = asyncProcessor;
        this.logger         = plugin.getLogger();
        registerDefaultProfiles();
    }

    /**
     * Injects the alert manager. Called from {@link dev.tyoudm.assasin.core.ServiceContainer}
     * after both subsystems are constructed.
     *
     * @param alertManager the alert manager
     */
    public void setAlertManager(final AlertManager alertManager) {
        this.alertManager = alertManager;
    }

    // ─── Mitigate ─────────────────────────────────────────────────────────────

    /**
     * Processes a violation flag and executes the appropriate strategies.
     *
     * <p>IMMEDIATE strategies are executed on the calling thread (netty).
     * NORMAL/HIGH strategies are scheduled on the main thread.
     * DEFERRED strategies are submitted to the async processor.
     *
     * @param ctx the mitigation context
     */
    public void mitigate(final MitigationContext ctx) {
        // ── Alert (always, regardless of VL threshold) ───────────────────────
        if (alertManager != null) {
            final double tps = plugin.getServer().getTPS()[0];
            final AlertContext alertCtx = AlertContext.of(
                ctx.player(), ctx.data(),
                ctx.checkName(), ctx.violationLevel(),
                ctx.severity(), ctx.details(), tps
            );
            // dispatch needs Bukkit API — schedule on main thread if called from netty
            if (plugin.getServer().isPrimaryThread()) {
                alertManager.dispatch(alertCtx);
            } else {
                plugin.getServer().getScheduler().runTask(plugin,
                    () -> alertManager.dispatch(alertCtx));
            }
        }

        // ── Strategies ───────────────────────────────────────────────────────
        final String profileName = checkProfileMap.getOrDefault(ctx.checkName(), DEFAULT_PROFILE);
        final MitigationProfile profile = profiles.getOrDefault(profileName,
            profiles.get(DEFAULT_PROFILE));

        if (profile == null) {
            logger.warning("[ASSASIN] No mitigation profile found for check: " + ctx.checkName());
            return;
        }

        final List<MitigationStrategy> strategies = profile.strategiesFor(ctx.violationLevel());
        if (strategies.isEmpty()) return;

        for (final MitigationStrategy strategy : strategies) {
            switch (strategy.priority()) {
                case IMMEDIATE -> executeImmediate(strategy, ctx);
                case NORMAL, HIGH -> scheduleMain(strategy, ctx);
                case DEFERRED -> scheduleAsync(strategy, ctx);
            }
        }
    }

    // ─── Profile management ───────────────────────────────────────────────────

    /**
     * Registers a mitigation profile.
     *
     * @param profile the profile to register
     */
    public void registerProfile(final MitigationProfile profile) {
        profiles.put(profile.getName(), profile);
    }

    /**
     * Maps a check name to a specific profile.
     *
     * @param checkName   the check name (e.g., "SpeedA")
     * @param profileName the profile name (e.g., "soft")
     */
    public void mapCheckToProfile(final String checkName, final String profileName) {
        checkProfileMap.put(checkName, profileName);
    }

    /**
     * Returns the profile registered under the given name, or {@code null}.
     *
     * @param name the profile name
     * @return the profile, or {@code null}
     */
    public MitigationProfile getProfile(final String name) {
        return profiles.get(name);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void executeImmediate(final MitigationStrategy strategy,
                                  final MitigationContext ctx) {
        try {
            strategy.execute(ctx);
        } catch (final Exception ex) {
            logger.warning("[ASSASIN] IMMEDIATE strategy " + strategy.name()
                + " threw: " + ex.getMessage());
        }
    }

    private void scheduleMain(final MitigationStrategy strategy,
                              final MitigationContext ctx) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                strategy.execute(ctx);
            } catch (final Exception ex) {
                logger.warning("[ASSASIN] NORMAL strategy " + strategy.name()
                    + " threw: " + ex.getMessage());
            }
        });
    }

    private void scheduleAsync(final MitigationStrategy strategy,
                               final MitigationContext ctx) {
        asyncProcessor.submit(() -> {
            try {
                // Deferred strategies that need Bukkit API must re-schedule to main
                if (strategy instanceof KickStrategy) {
                    plugin.getServer().getScheduler().runTask(plugin,
                        () -> strategy.execute(ctx));
                } else {
                    strategy.execute(ctx);
                }
            } catch (final Exception ex) {
                logger.warning("[ASSASIN] DEFERRED strategy " + strategy.name()
                    + " threw: " + ex.getMessage());
            }
        });
    }

    // ─── Default profiles ─────────────────────────────────────────────────────

    private void registerDefaultProfiles() {

        // ── soft: minor violations ───────────────────────────────────────────
        registerProfile(new MitigationProfile("soft")
            .addStrategy(1.0,  SetbackStrategy.soft())
            .addStrategy(5.0,  SlowStrategy.defaultSlow())
            .addStrategy(10.0, SetbackStrategy.hard())
        );

        // ── medium: standard movement/combat ────────────────────────────────
        registerProfile(new MitigationProfile("medium")
            .addStrategy(1.0,  CancelPacketStrategy.INSTANCE)
            .addStrategy(1.0,  SetbackStrategy.soft())
            .addStrategy(8.0,  FreezeStrategy.defaultFreeze())
            .addStrategy(15.0, KickStrategy.generic())
        );

        // ── hard: severe violations ──────────────────────────────────────────
        registerProfile(new MitigationProfile("hard")
            .addStrategy(1.0,  CancelPacketStrategy.INSTANCE)
            .addStrategy(1.0,  SetbackStrategy.hard())
            .addStrategy(5.0,  FreezeStrategy.defaultFreeze())
            .addStrategy(10.0, KickStrategy.generic())
        );

        // ── combat: killaura, reach, criticals ───────────────────────────────
        registerProfile(new MitigationProfile("combat")
            .addStrategy(1.0,  CancelDamageStrategy.INSTANCE)
            .addStrategy(10.0, FreezeStrategy.defaultFreeze())
            .addStrategy(20.0, KickStrategy.generic())
        );

        // ── world: scaffold, fastbreak, nuker ────────────────────────────────
        registerProfile(new MitigationProfile("world")
            .addStrategy(1.0,  CancelBlockActionStrategy.INSTANCE)
            .addStrategy(10.0, KickStrategy.generic())
        );

        // ── mount: mount speed, mount fly ────────────────────────────────────
        registerProfile(new MitigationProfile("mount")
            .addStrategy(1.0,  DismountStrategy.INSTANCE)
            .addStrategy(5.0,  SetbackStrategy.soft())
            .addStrategy(10.0, KickStrategy.generic())
        );

        // ── velocity: KB ratio checks ────────────────────────────────────────
        registerProfile(new MitigationProfile("velocity")
            .addStrategy(1.0,  VelocityStrategy.zero())
            .addStrategy(5.0,  SetbackStrategy.soft())
            .addStrategy(15.0, KickStrategy.generic())
        );

        // ── macro: conservative — don't reveal detection ─────────────────────
        // VL 0-4: NO_ACTION (log only — handled by check, not engine)
        // VL 5-9: SILENT_ALERT (handled by alert system in FASE 15)
        registerProfile(new MitigationProfile("macro")
            .addStrategy(10.0, CancelPacketStrategy.INSTANCE)
            .addStrategy(15.0, CancelPacketStrategy.INSTANCE)
            .addStrategy(15.0, ResyncStrategy.INSTANCE)
            .addStrategy(20.0, KickStrategy.generic())
        );

        // ── badpackets: crash/exploit packets ────────────────────────────────
        registerProfile(new MitigationProfile("badpackets")
            .addStrategy(1.0,  CancelPacketStrategy.INSTANCE)
            .addStrategy(3.0,  KickStrategy.generic())
        );
    }
}
