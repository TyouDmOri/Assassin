/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.storage;

import com.zaxxer.hikari.HikariConfig;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * SQLite storage provider — zero-config default backend.
 *
 * <p>Uses the JDBC SQLite driver bundled with the JVM (via the
 * {@code org.xerial:sqlite-jdbc} driver shaded into the JAR, or the
 * driver available on the server classpath).
 *
 * <p>The database file is stored at
 * {@code plugins/ASSASIN/data/assasin.db}.
 *
 * <h2>SQLite-specific settings</h2>
 * <ul>
 *   <li>WAL journal mode for concurrent reads.</li>
 *   <li>Synchronous = NORMAL for performance.</li>
 *   <li>Pool size = 1 (SQLite is single-writer).</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class SQLiteProvider extends AbstractSqlProvider {

    private final File dbFile;

    /**
     * Creates a new SQLite provider.
     *
     * @param dbFile   the database file (created if it doesn't exist)
     * @param logger   plugin logger
     * @param executor async executor for DB operations
     */
    public SQLiteProvider(final File dbFile, final Logger logger, final Executor executor) {
        super(logger, executor);
        this.dbFile = dbFile;
    }

    @Override
    protected HikariConfig buildHikariConfig() {
        dbFile.getParentFile().mkdirs();

        final HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("org.sqlite.JDBC");
        cfg.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());

        // SQLite is single-writer — pool size 1 avoids lock contention
        cfg.setMaximumPoolSize(1);
        cfg.setMinimumIdle(1);

        // WAL mode + NORMAL sync for performance
        cfg.addDataSourceProperty("journal_mode", "WAL");
        cfg.addDataSourceProperty("synchronous", "NORMAL");
        cfg.addDataSourceProperty("foreign_keys", "ON");

        return cfg;
    }

    @Override
    public @org.jetbrains.annotations.NotNull String getType() {
        return "sqlite";
    }
}
