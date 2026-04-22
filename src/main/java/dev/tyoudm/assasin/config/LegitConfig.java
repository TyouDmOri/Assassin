/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.config;

import dev.tyoudm.assasin.core.LegitTechniqueRegistry;
import dev.tyoudm.assasin.core.LegitTechniqueRegistry.Technique;
import dev.tyoudm.assasin.core.LegitTechniqueRegistry.Tolerance;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed accessor for {@code legit-techniques.yml}.
 *
 * <p>Reads per-technique tolerance values and applies them to the
 * {@link LegitTechniqueRegistry} on load/reload.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class LegitConfig {

    private final FileConfiguration cfg;

    public LegitConfig(final FileConfiguration cfg) {
        this.cfg = cfg;
    }

    /**
     * Applies all configured tolerances to the given registry.
     *
     * @param registry the registry to update
     */
    public void applyTo(final LegitTechniqueRegistry registry) {
        for (final Technique technique : Technique.values()) {
            final String path = "techniques." + technique.name();
            if (!cfg.contains(path)) continue;

            final int    exemptTicks      = cfg.getInt(path + ".exempt-ticks", 0);
            final int    minSamples       = cfg.getInt(path + ".min-samples", 0);
            final double sigmaThreshold   = cfg.getDouble(path + ".sigma-threshold", 0.0);
            final double successRateLimit = cfg.getDouble(path + ".success-rate-limit", 1.0);
            final double kbMultiplier     = cfg.getDouble(path + ".kb-multiplier", 1.0);

            registry.set(technique, new Tolerance(
                exemptTicks, minSamples, sigmaThreshold, successRateLimit, kbMultiplier));
        }
    }

    /**
     * Returns the exempt ticks for a specific technique.
     *
     * @param technique the technique name (e.g., "WTAP")
     * @param def       default value
     * @return configured exempt ticks
     */
    public int getExemptTicks(final String technique, final int def) {
        return cfg.getInt("techniques." + technique + ".exempt-ticks", def);
    }

    /**
     * Returns the KB multiplier for a specific technique.
     *
     * @param technique the technique name
     * @param def       default value
     * @return configured KB multiplier
     */
    public double getKbMultiplier(final String technique, final double def) {
        return cfg.getDouble("techniques." + technique + ".kb-multiplier", def);
    }
}
