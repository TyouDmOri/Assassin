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
import com.zaxxer.hikari.HikariDataSource;
import dev.tyoudm.assasin.storage.migration.MigrationManager;
import dev.tyoudm.assasin.storage.model.AlertLog;
import dev.tyoudm.assasin.storage.model.AlertPreference;
import dev.tyoudm.assasin.storage.model.MacroEvidence;
import dev.tyoudm.assasin.storage.model.MitigationLog;
import dev.tyoudm.assasin.storage.model.PlayerProfile;
import dev.tyoudm.assasin.storage.model.ViolationRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;

/**
 * Abstract SQL storage provider implementing all {@link StorageProvider} methods
 * using JDBC prepared statements and HikariCP connection pooling.
 *
 * <p>Concrete subclasses ({@link SQLiteProvider}, {@link MySQLProvider},
 * {@link MariaDBProvider}) only need to configure the {@link HikariConfig}
 * and provide the JDBC URL.
 *
 * <h2>Batching</h2>
 * Write operations (insert violation, insert alert, etc.) are executed
 * asynchronously via the provided {@link Executor}. Reads are also async.
 *
 * <h2>HikariCP defaults</h2>
 * <ul>
 *   <li>maxPoolSize = 10</li>
 *   <li>minIdle = 2</li>
 *   <li>connectionTimeout = 5000ms</li>
 *   <li>maxLifetime = 1800000ms (30 min)</li>
 *   <li>leakDetectionThreshold = 30000ms</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public abstract class AbstractSqlProvider implements StorageProvider {

    protected HikariDataSource dataSource;
    protected final Logger     logger;
    protected final Executor   executor;

    protected AbstractSqlProvider(final Logger logger, final Executor executor) {
        this.logger   = logger;
        this.executor = executor;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void init() throws Exception {
        final HikariConfig config = buildHikariConfig();
        applyDefaults(config);
        dataSource = new HikariDataSource(config);
        new MigrationManager(dataSource, logger).migrate();
        logger.info("[ASSASIN] Storage (" + getType() + ") initialized.");
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("[ASSASIN] Storage (" + getType() + ") closed.");
        }
    }

    /**
     * Subclasses configure the HikariCP datasource here (JDBC URL, driver, etc.).
     *
     * @return configured {@link HikariConfig}
     */
    protected abstract HikariConfig buildHikariConfig();

    private static void applyDefaults(final HikariConfig cfg) {
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(5_000);
        cfg.setMaxLifetime(1_800_000);
        cfg.setLeakDetectionThreshold(30_000);
        cfg.setPoolName("assasin-pool");
    }

    // ─── Player profiles ──────────────────────────────────────────────────────

    @Override
    public @NotNull CompletableFuture<@Nullable PlayerProfile> loadProfile(
            @NotNull final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT uuid,name,first_join,last_join,total_violations,"
                + "banned,ban_reason FROM assasin_players WHERE uuid=?";
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    return new PlayerProfile(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        rs.getLong("first_join"),
                        rs.getLong("last_join"),
                        rs.getInt("total_violations"),
                        rs.getBoolean("banned"),
                        rs.getString("ban_reason")
                    );
                }
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] loadProfile failed: " + ex.getMessage());
                return null;
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveProfile(@NotNull final PlayerProfile p) {
        return CompletableFuture.runAsync(() -> {
            final String sql = "INSERT INTO assasin_players "
                + "(uuid,name,first_join,last_join,total_violations,banned,ban_reason) "
                + "VALUES(?,?,?,?,?,?,?) "
                + onConflictUpdate("uuid",
                    "name=excluded.name,last_join=excluded.last_join,"
                    + "total_violations=excluded.total_violations,"
                    + "banned=excluded.banned,ban_reason=excluded.ban_reason");
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.uuid().toString());
                ps.setString(2, p.name());
                ps.setLong(3, p.firstJoinMs());
                ps.setLong(4, p.lastJoinMs());
                ps.setInt(5, p.totalViolations());
                ps.setBoolean(6, p.banned());
                ps.setString(7, p.banReason());
                ps.executeUpdate();
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] saveProfile failed: " + ex.getMessage());
            }
        }, executor);
    }

    // ─── Violations ───────────────────────────────────────────────────────────

    @Override
    public @NotNull CompletableFuture<Long> insertViolation(
            @NotNull final ViolationRecord r) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "INSERT INTO assasin_violations "
                + "(uuid,check_name,vl,timestamp_ms,ping_ms,tps,world,x,y,z,"
                + "mitigation_applied,data_json) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, r.playerUuid().toString());
                ps.setString(2, r.checkName());
                ps.setDouble(3, r.violationLevel());
                ps.setLong(4, r.timestampMs());
                ps.setInt(5, r.pingMs());
                ps.setDouble(6, r.tps());
                ps.setString(7, r.world());
                ps.setDouble(8, r.x());
                ps.setDouble(9, r.y());
                ps.setDouble(10, r.z());
                ps.setString(11, r.mitigationApplied());
                ps.setString(12, r.dataJson());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    return keys.next() ? keys.getLong(1) : -1L;
                }
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] insertViolation failed: " + ex.getMessage());
                return -1L;
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<ViolationRecord>> getViolations(
            @NotNull final UUID uuid, final int limit) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT id,uuid,check_name,vl,timestamp_ms,ping_ms,tps,"
                + "world,x,y,z,mitigation_applied,data_json "
                + "FROM assasin_violations WHERE uuid=? ORDER BY timestamp_ms DESC LIMIT ?";
            final List<ViolationRecord> list = new ArrayList<>();
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new ViolationRecord(
                            rs.getLong("id"),
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("check_name"),
                            rs.getDouble("vl"),
                            rs.getLong("timestamp_ms"),
                            rs.getInt("ping_ms"),
                            rs.getDouble("tps"),
                            rs.getString("world"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getString("mitigation_applied"),
                            rs.getString("data_json")
                        ));
                    }
                }
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] getViolations failed: " + ex.getMessage());
            }
            return list;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Integer> countViolations(@NotNull final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT COUNT(*) FROM assasin_violations WHERE uuid=?";
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] countViolations failed: " + ex.getMessage());
                return 0;
            }
        }, executor);
    }

    // ─── Mitigation log ───────────────────────────────────────────────────────

    @Override
    public @NotNull CompletableFuture<Void> insertMitigationLog(
            @NotNull final MitigationLog log) {
        return CompletableFuture.runAsync(() -> {
            final String sql = "INSERT INTO assasin_mitigations "
                + "(violation_id,strategy,result,timestamp_ms) VALUES(?,?,?,?)";
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, log.violationId());
                ps.setString(2, log.strategy());
                ps.setString(3, log.result());
                ps.setLong(4, log.timestampMs());
                ps.executeUpdate();
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] insertMitigationLog failed: " + ex.getMessage());
            }
        }, executor);
    }

    // ─── Alerts ───────────────────────────────────────────────────────────────

    @Override
    public @NotNull CompletableFuture<Void> insertAlertLog(@NotNull final AlertLog alert) {
        return CompletableFuture.runAsync(() -> {
            final String sql = "INSERT INTO assasin_alerts "
                + "(staff_uuid,player_uuid,check_name,vl,timestamp_ms) VALUES(?,?,?,?,?)";
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, alert.staffUuid() != null ? alert.staffUuid().toString() : null);
                ps.setString(2, alert.playerUuid().toString());
                ps.setString(3, alert.checkName());
                ps.setDouble(4, alert.violationLevel());
                ps.setLong(5, alert.timestampMs());
                ps.executeUpdate();
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] insertAlertLog failed: " + ex.getMessage());
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<AlertPreference>> loadAlertPreferences(
            @NotNull final UUID staffUuid) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT uuid,check_name,enabled,channels_bitmask "
                + "FROM assasin_alert_preferences WHERE uuid=?";
            final List<AlertPreference> list = new ArrayList<>();
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, staffUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new AlertPreference(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("check_name"),
                            rs.getBoolean("enabled"),
                            rs.getInt("channels_bitmask")
                        ));
                    }
                }
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] loadAlertPreferences failed: " + ex.getMessage());
            }
            return list;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveAlertPreference(
            @NotNull final AlertPreference pref) {
        return CompletableFuture.runAsync(() -> {
            final String sql = "INSERT INTO assasin_alert_preferences "
                + "(uuid,check_name,enabled,channels_bitmask) VALUES(?,?,?,?) "
                + onConflictUpdate("uuid,check_name",
                    "enabled=excluded.enabled,channels_bitmask=excluded.channels_bitmask");
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, pref.staffUuid().toString());
                ps.setString(2, pref.checkName());
                ps.setBoolean(3, pref.enabled());
                ps.setInt(4, pref.channelBitmask());
                ps.executeUpdate();
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] saveAlertPreference failed: " + ex.getMessage());
            }
        }, executor);
    }

    // ─── Macro evidence ───────────────────────────────────────────────────────

    @Override
    public @NotNull CompletableFuture<Void> upsertMacroEvidence(
            @NotNull final MacroEvidence ev) {
        return CompletableFuture.runAsync(() -> {
            final String sql = "INSERT INTO assasin_macro_evidence "
                + "(uuid,pattern_hash,occurrences,avg_delta_ms,std_dev,last_seen,evidence_json) "
                + "VALUES(?,?,?,?,?,?,?) "
                + onConflictUpdate("uuid,pattern_hash",
                    "occurrences=excluded.occurrences,avg_delta_ms=excluded.avg_delta_ms,"
                    + "std_dev=excluded.std_dev,last_seen=excluded.last_seen,"
                    + "evidence_json=excluded.evidence_json");
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, ev.playerUuid().toString());
                ps.setLong(2, ev.patternHash());
                ps.setInt(3, ev.occurrences());
                ps.setDouble(4, ev.avgDeltaMs());
                ps.setDouble(5, ev.stdDev());
                ps.setLong(6, ev.lastSeenMs());
                ps.setString(7, ev.evidenceJson());
                ps.executeUpdate();
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] upsertMacroEvidence failed: " + ex.getMessage());
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<MacroEvidence>> getMacroEvidence(
            @NotNull final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT id,uuid,pattern_hash,occurrences,avg_delta_ms,"
                + "std_dev,last_seen,evidence_json FROM assasin_macro_evidence WHERE uuid=?";
            final List<MacroEvidence> list = new ArrayList<>();
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new MacroEvidence(
                            rs.getLong("id"),
                            UUID.fromString(rs.getString("uuid")),
                            rs.getLong("pattern_hash"),
                            rs.getInt("occurrences"),
                            rs.getDouble("avg_delta_ms"),
                            rs.getDouble("std_dev"),
                            rs.getLong("last_seen"),
                            rs.getString("evidence_json")
                        ));
                    }
                }
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] getMacroEvidence failed: " + ex.getMessage());
            }
            return list;
        }, executor);
    }

    // ─── Maintenance ──────────────────────────────────────────────────────────

    @Override
    public @NotNull CompletableFuture<Integer> purgeOldViolations(final long olderThanMs) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "DELETE FROM assasin_violations WHERE timestamp_ms < ?";
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, olderThanMs);
                return ps.executeUpdate();
            } catch (final SQLException ex) {
                logger.warning("[ASSASIN] purgeOldViolations failed: " + ex.getMessage());
                return 0;
            }
        }, executor);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns the SQL upsert conflict clause appropriate for this backend.
     * SQLite uses {@code ON CONFLICT DO UPDATE SET}; MySQL/MariaDB use
     * {@code ON DUPLICATE KEY UPDATE}.
     *
     * <p>Default implementation uses SQLite/PostgreSQL syntax.
     * MySQL/MariaDB subclasses override this.
     *
     * @param conflictColumns comma-separated conflict column names
     * @param updateClause    the SET clause for the update
     * @return SQL fragment
     */
    protected String onConflictUpdate(final String conflictColumns,
                                      final String updateClause) {
        return "ON CONFLICT(" + conflictColumns + ") DO UPDATE SET " + updateClause;
    }
}
