/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Typed accessor for {@code macro.yml}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MacroConfig {

    private final FileConfiguration cfg;

    /** Cached whitelist UUIDs. */
    private final Set<UUID> whitelist = new HashSet<>();

    public MacroConfig(final FileConfiguration cfg) {
        this.cfg = cfg;
        loadWhitelist();
    }

    // ─── Strictness ───────────────────────────────────────────────────────────

    public String getStrictness() {
        return cfg.getString("strictness", "medium").toLowerCase();
    }

    public int getMinSamples() {
        final String level = getStrictness();
        return cfg.getInt("strictness-levels." + level + ".min-samples", 20);
    }

    public double getMinRSquared() {
        final String level = getStrictness();
        return cfg.getDouble("strictness-levels." + level + ".min-r-squared", 0.95);
    }

    public double getMinSigmaMs() {
        final String level = getStrictness();
        return cfg.getDouble("strictness-levels." + level + ".min-sigma-ms", 1.5);
    }

    // ─── Whitelist ────────────────────────────────────────────────────────────

    public Set<UUID> getWhitelist() {
        return Collections.unmodifiableSet(whitelist);
    }

    public boolean isWhitelisted(final UUID uuid) {
        return whitelist.contains(uuid);
    }

    // ─── Thresholds ───────────────────────────────────────────────────────────

    public double getThreshold(final String checkName, final String key, final double def) {
        return cfg.getDouble("thresholds." + checkName + "." + key, def);
    }

    public int getThresholdInt(final String checkName, final String key, final int def) {
        return cfg.getInt("thresholds." + checkName + "." + key, def);
    }

    // ─── Behavior ─────────────────────────────────────────────────────────────

    public int getDisableTimingAbovePing() {
        return cfg.getInt("disable-timing-above-ping", 300);
    }

    public int getPostLagSpikeSuppressTicks() {
        return cfg.getInt("post-lag-spike-suppress-ticks", 5);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void loadWhitelist() {
        whitelist.clear();
        final List<String> uuids = cfg.getStringList("whitelist");
        for (final String s : uuids) {
            try {
                whitelist.add(UUID.fromString(s));
            } catch (final IllegalArgumentException ignored) {
                // Invalid UUID — skip
            }
        }
    }
}
