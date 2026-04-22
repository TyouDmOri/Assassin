/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for all ASSASIN subsystem modules.
 *
 * <p>Modules are registered in dependency order and enabled/disabled
 * in that same order (LIFO for disable). Each module implements
 * {@link AssasinModule} and is identified by a unique string key.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *   registry.register("playerData", playerDataManager);
 *   registry.register("latency",    transactionManager);
 *   registry.enableAll();
 *   // ...
 *   registry.disableAll(); // disables in reverse order
 * }</pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class ModuleRegistry {

    /**
     * Interface that all ASSASIN subsystem modules must implement.
     */
    public interface AssasinModule {

        /**
         * Called when the module should initialize and start.
         * Invoked in registration order.
         */
        void onEnable();

        /**
         * Called when the module should stop and release resources.
         * Invoked in reverse registration order.
         */
        void onDisable();

        /**
         * Returns a human-readable name for this module (used in logs).
         *
         * @return module name
         */
        String moduleName();
    }

    // ─── State ────────────────────────────────────────────────────────────────

    /**
     * Insertion-ordered map of key → module.
     * LinkedHashMap preserves registration order for enable/disable.
     */
    private final Map<String, AssasinModule> modules = new LinkedHashMap<>();

    /** Whether {@link #enableAll()} has been called. */
    private boolean enabled = false;

    // ─── Registration ─────────────────────────────────────────────────────────

    /**
     * Registers a module under the given key.
     *
     * <p>Must be called before {@link #enableAll()}. Duplicate keys are
     * rejected with an {@link IllegalArgumentException}.
     *
     * @param key    unique identifier for this module
     * @param module the module to register
     * @throws IllegalArgumentException if the key is already registered
     * @throws IllegalStateException    if called after {@link #enableAll()}
     */
    public void register(@NotNull final String key, @NotNull final AssasinModule module) {
        if (enabled) {
            throw new IllegalStateException(
                "Cannot register module '" + key + "' after enableAll() has been called.");
        }
        if (modules.containsKey(key)) {
            throw new IllegalArgumentException(
                "Module key '" + key + "' is already registered.");
        }
        modules.put(key, module);
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    /**
     * Enables all registered modules in registration order.
     *
     * @throws IllegalStateException if already enabled
     */
    public void enableAll() {
        if (enabled) {
            throw new IllegalStateException("ModuleRegistry is already enabled.");
        }
        enabled = true;
        for (final AssasinModule module : modules.values()) {
            module.onEnable();
        }
    }

    /**
     * Disables all registered modules in reverse registration order.
     * Safe to call even if {@link #enableAll()} was never called.
     */
    public void disableAll() {
        final List<AssasinModule> reversed = new ArrayList<>(modules.values());
        Collections.reverse(reversed);
        for (final AssasinModule module : reversed) {
            try {
                module.onDisable();
            } catch (final Exception ex) {
                // Log but continue disabling remaining modules
                System.err.println("[ASSASIN] Error disabling module '"
                    + module.moduleName() + "': " + ex.getMessage());
            }
        }
        enabled = false;
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    /**
     * Returns the module registered under the given key, or {@code null}.
     *
     * @param key the module key
     * @return the module, or {@code null} if not found
     */
    @Nullable
    public AssasinModule get(@NotNull final String key) {
        return modules.get(key);
    }

    /**
     * Returns the module registered under the given key, cast to {@code T}.
     *
     * @param key  the module key
     * @param type the expected type
     * @param <T>  the module type
     * @return the module cast to {@code T}, or {@code null}
     * @throws ClassCastException if the module is not of type {@code T}
     */
    @Nullable
    public <T extends AssasinModule> T get(@NotNull final String key, @NotNull final Class<T> type) {
        final AssasinModule module = modules.get(key);
        return module == null ? null : type.cast(module);
    }

    /**
     * Returns {@code true} if a module is registered under the given key.
     *
     * @param key the module key
     * @return {@code true} if registered
     */
    public boolean isRegistered(@NotNull final String key) {
        return modules.containsKey(key);
    }

    /**
     * Returns the number of registered modules.
     *
     * @return module count
     */
    public int size() {
        return modules.size();
    }
}
