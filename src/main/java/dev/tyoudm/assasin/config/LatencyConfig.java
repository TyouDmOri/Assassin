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

/**
 * Typed accessor for {@code latency.yml}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class LatencyConfig {

    private final FileConfiguration cfg;

    public LatencyConfig(final FileConfiguration cfg) {
        this.cfg = cfg;
    }

    public int    getMaxCompensatedPingMs()     { return cfg.getInt("ping-compensation.max-compensated-ping-ms", 300); }
    public double getReachPer100ms()            { return cfg.getDouble("ping-compensation.reach-per-100ms", 0.05); }
    public int    getVelocityTicksPer50ms()     { return cfg.getInt("ping-compensation.velocity-ticks-per-50ms", 1); }
    public int    getTransactionIntervalTicks() { return cfg.getInt("transaction.interval-ticks", 20); }
    public int    getMaxBarrierTicks()          { return cfg.getInt("transaction.max-barrier-ticks", 40); }
    public int    getHistoryTicks()             { return cfg.getInt("lag-compensation.history-ticks", 40); }
    public double getLagSpikeTps()              { return cfg.getDouble("lag-compensation.lag-spike-tps", 18.0); }
    public int    getPostSpikeSuppressTicks()   { return cfg.getInt("lag-compensation.post-spike-suppress-ticks", 5); }
}
