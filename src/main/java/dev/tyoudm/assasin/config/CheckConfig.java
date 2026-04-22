/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed accessor for {@code checks.yml}.
 *
 * <p>Provides per-check enable/disable state and threshold values.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CheckConfig {

    private final FileConfiguration cfg;

    public CheckConfig(final FileConfiguration cfg) {
        this.cfg = cfg;
    }

    /**
     * Returns {@code true} if the given check is enabled.
     *
     * @param checkName the check name (e.g., "SpeedA")
     * @return {@code true} if enabled (default: true)
     */
    public boolean isEnabled(final String checkName) {
        return cfg.getBoolean("checks." + checkName + ".enabled", true);
    }

    /**
     * Returns the maximum VL for the given check.
     *
     * @param checkName the check name
     * @return max VL (default: 10.0)
     */
    public double getMaxVl(final String checkName) {
        return cfg.getDouble("checks." + checkName + ".max-vl", 10.0);
    }

    /**
     * Returns the VL decay rate for the given check.
     *
     * @param checkName   the check name
     * @param globalDecay the global decay rate as fallback
     * @return decay rate per tick
     */
    public double getDecayRate(final String checkName, final double globalDecay) {
        return cfg.getDouble("checks." + checkName + ".decay-rate", globalDecay);
    }

    /**
     * Returns a double threshold for the given check and key.
     *
     * @param checkName    the check name
     * @param key          the threshold key (e.g., "tolerance")
     * @param defaultValue the default value
     * @return the configured value
     */
    public double getDouble(final String checkName, final String key, final double defaultValue) {
        return cfg.getDouble("checks." + checkName + "." + key, defaultValue);
    }

    /**
     * Returns an int threshold for the given check and key.
     *
     * @param checkName    the check name
     * @param key          the threshold key
     * @param defaultValue the default value
     * @return the configured value
     */
    public int getInt(final String checkName, final String key, final int defaultValue) {
        return cfg.getInt("checks." + checkName + "." + key, defaultValue);
    }

    /**
     * Returns the raw {@link ConfigurationSection} for a check.
     *
     * @param checkName the check name
     * @return the section, or {@code null} if not found
     */
    public ConfigurationSection getSection(final String checkName) {
        return cfg.getConfigurationSection("checks." + checkName);
    }
}
