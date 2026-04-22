/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.storage;

import dev.tyoudm.assasin.AssasinPlugin;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * Factory that creates the appropriate {@link StorageProvider} based on
 * the {@code config.yml} database type setting.
 *
 * <p>Supported types (case-insensitive):
 * <ul>
 *   <li>{@code sqlite}  — default, zero-config</li>
 *   <li>{@code mysql}   — requires host/port/database/username/password</li>
 *   <li>{@code mariadb} — same as mysql but uses MariaDB driver</li>
 * </ul>
 *
 * <p>In FASE 18, this factory will read from {@code config.yml}.
 * Until then, it defaults to SQLite.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class StorageFactory {

    /** Utility class — no instantiation. */
    private StorageFactory() {
        throw new UnsupportedOperationException("StorageFactory is a utility class.");
    }

    /**
     * Creates and returns the configured {@link StorageProvider}.
     *
     * <p>The provider is NOT yet initialized — call {@link StorageProvider#init()}
     * after construction.
     *
     * @param plugin   the owning plugin (used for data folder and config)
     * @param executor async executor for DB operations
     * @return the configured provider
     */
    public static StorageProvider create(final AssasinPlugin plugin,
                                         final Executor executor) {
        final Logger logger = plugin.getLogger();

        // FASE 18: read from config.yml
        // For now, default to SQLite
        final String type = plugin.getConfig().getString("storage.type", "sqlite")
            .toLowerCase();

        return switch (type) {
            case "mysql" -> {
                final String host     = plugin.getConfig().getString("storage.host", "localhost");
                final int    port     = plugin.getConfig().getInt("storage.port", 3306);
                final String database = plugin.getConfig().getString("storage.database", "assasin");
                final String user     = plugin.getConfig().getString("storage.username", "root");
                final String pass     = plugin.getConfig().getString("storage.password", "");
                yield new MySQLProvider(host, port, database, user, pass, logger, executor);
            }
            case "mariadb" -> {
                final String host     = plugin.getConfig().getString("storage.host", "localhost");
                final int    port     = plugin.getConfig().getInt("storage.port", 3306);
                final String database = plugin.getConfig().getString("storage.database", "assasin");
                final String user     = plugin.getConfig().getString("storage.username", "root");
                final String pass     = plugin.getConfig().getString("storage.password", "");
                yield new MariaDBProvider(host, port, database, user, pass, logger, executor);
            }
            default -> {
                // sqlite (default)
                final File dbFile = new File(plugin.getDataFolder(), "data/assasin.db");
                yield new SQLiteProvider(dbFile, logger, executor);
            }
        };
    }
}
