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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Typed accessor for {@code mitigation.yml}.
 *
 * <p>Provides the check → profile mapping and cascade definitions.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MitigationConfig {

    private final FileConfiguration cfg;

    /** Cached check → profile name map. */
    private final Map<String, String> checkProfiles = new HashMap<>();

    public MitigationConfig(final FileConfiguration cfg) {
        this.cfg = cfg;
        loadCheckProfiles();
    }

    // ─── Check → profile ──────────────────────────────────────────────────────

    /**
     * Returns the mitigation profile name for the given check.
     *
     * @param checkName the check name
     * @return profile name (default: "medium")
     */
    public String getProfileForCheck(final String checkName) {
        return checkProfiles.getOrDefault(checkName, "medium");
    }

    /**
     * Returns an unmodifiable view of the check → profile map.
     *
     * @return check profile map
     */
    public Map<String, String> getCheckProfiles() {
        return Collections.unmodifiableMap(checkProfiles);
    }

    // ─── Cascade definitions ──────────────────────────────────────────────────

    /**
     * Returns the cascade section for the given profile name.
     *
     * @param profileName the profile name (e.g., "medium")
     * @return the cascades section, or {@code null} if not found
     */
    public ConfigurationSection getCascades(final String profileName) {
        return cfg.getConfigurationSection("profiles." + profileName + ".cascades");
    }

    /**
     * Returns the strategy list for the given profile and VL threshold.
     *
     * @param profileName  the profile name
     * @param vlThreshold  the VL threshold key (as string, e.g., "1.0")
     * @return list of strategy names, or empty list
     */
    public List<String> getStrategies(final String profileName, final String vlThreshold) {
        return cfg.getStringList("profiles." + profileName + ".cascades." + vlThreshold);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void loadCheckProfiles() {
        checkProfiles.clear();
        final ConfigurationSection section = cfg.getConfigurationSection("check-profiles");
        if (section == null) return;
        for (final String key : section.getKeys(false)) {
            checkProfiles.put(key, section.getString(key, "medium"));
        }
    }
}
