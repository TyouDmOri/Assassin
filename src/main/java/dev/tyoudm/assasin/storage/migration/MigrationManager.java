/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.storage.migration;

import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Schema migration manager for ASSASIN storage.
 *
 * <p>Applies SQL migration scripts in version order. Each migration is
 * tracked in the {@code assasin_schema_version} table. Already-applied
 * migrations are skipped.
 *
 * <h2>Migration files</h2>
 * Located in {@code resources/migrations/} and named
 * {@code V{n}__{description}.sql} (e.g., {@code V1__init.sql}).
 *
 * <h2>Idempotency</h2>
 * Each migration is applied exactly once. If the server crashes mid-migration,
 * the incomplete migration will be re-applied on next startup (SQL scripts
 * use {@code IF NOT EXISTS} to be safe).
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MigrationManager {

    /** Ordered list of migration resource paths. */
    private static final String[] MIGRATIONS = {
        "migrations/V1__init.sql",
        "migrations/V2__add_mitigation.sql",
        "migrations/V3__add_alert_prefs.sql",
        "migrations/V4__add_macro_log.sql"
    };

    private final HikariDataSource dataSource;
    private final Logger           logger;

    public MigrationManager(final HikariDataSource dataSource, final Logger logger) {
        this.dataSource = dataSource;
        this.logger     = logger;
    }

    // ─── Migrate ──────────────────────────────────────────────────────────────

    /**
     * Runs all pending migrations in order.
     *
     * @throws SQLException if a migration fails
     */
    public void migrate() throws SQLException {
        ensureVersionTable();

        for (int i = 0; i < MIGRATIONS.length; i++) {
            final int version = i + 1;
            if (isApplied(version)) continue;

            final String sql = loadResource(MIGRATIONS[i]);
            if (sql == null) {
                logger.warning("[ASSASIN] Migration resource not found: " + MIGRATIONS[i]);
                continue;
            }

            applyMigration(version, MIGRATIONS[i], sql);
        }
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void ensureVersionTable() throws SQLException {
        final String sql = "CREATE TABLE IF NOT EXISTS assasin_schema_version ("
            + "version INTEGER PRIMARY KEY,"
            + "description TEXT NOT NULL,"
            + "applied_at INTEGER NOT NULL"
            + ")";
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }

    private boolean isApplied(final int version) throws SQLException {
        final String sql = "SELECT 1 FROM assasin_schema_version WHERE version=?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void applyMigration(final int version, final String name,
                                 final String sql) throws SQLException {
        logger.info("[ASSASIN] Applying migration V" + version + ": " + name);

        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                // Execute each statement separated by semicolons
                for (final String stmt : sql.split(";")) {
                    final String trimmed = stmt.trim();
                    if (!trimmed.isEmpty()) {
                        st.execute(trimmed);
                    }
                }
            }

            // Record the migration
            final String record = "INSERT INTO assasin_schema_version "
                + "(version,description,applied_at) VALUES(?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(record)) {
                ps.setInt(1, version);
                ps.setString(2, name);
                ps.setLong(3, System.currentTimeMillis());
                ps.executeUpdate();
            }

            c.commit();
            logger.info("[ASSASIN] Migration V" + version + " applied successfully.");
        } catch (final SQLException ex) {
            logger.severe("[ASSASIN] Migration V" + version + " failed: " + ex.getMessage());
            throw ex;
        }
    }

    private String loadResource(final String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) return null;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (final IOException ex) {
            logger.warning("[ASSASIN] Failed to load migration resource: " + path);
            return null;
        }
    }
}
