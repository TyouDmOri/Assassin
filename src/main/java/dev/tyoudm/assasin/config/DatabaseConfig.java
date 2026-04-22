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
 * Typed accessor for the {@code storage} section of {@code config.yml}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class DatabaseConfig {

    private final FileConfiguration cfg;

    public DatabaseConfig(final FileConfiguration cfg) {
        this.cfg = cfg;
    }

    public String  getType()                { return cfg.getString("storage.type", "sqlite"); }
    public String  getHost()                { return cfg.getString("storage.host", "localhost"); }
    public int     getPort()                { return cfg.getInt("storage.port", 3306); }
    public String  getDatabase()            { return cfg.getString("storage.database", "assasin"); }
    public String  getUsername()            { return cfg.getString("storage.username", "root"); }
    public String  getPassword()            { return cfg.getString("storage.password", ""); }
    public int     getMaxPoolSize()         { return cfg.getInt("storage.pool.max-pool-size", 10); }
    public int     getMinIdle()             { return cfg.getInt("storage.pool.min-idle", 2); }
    public long    getConnectionTimeout()   { return cfg.getLong("storage.pool.connection-timeout", 5000); }
    public long    getMaxLifetime()         { return cfg.getLong("storage.pool.max-lifetime", 1800000); }
    public long    getLeakDetection()       { return cfg.getLong("storage.pool.leak-detection-threshold", 30000); }
}
