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

import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * MariaDB storage provider using HikariCP.
 *
 * <p>Functionally identical to {@link MySQLProvider} but uses the
 * MariaDB Connector/J driver for better compatibility with MariaDB-specific
 * features and performance optimizations.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MariaDBProvider extends AbstractSqlProvider {

    private final String host;
    private final int    port;
    private final String database;
    private final String username;
    private final String password;

    public MariaDBProvider(final String host, final int port, final String database,
                           final String username, final String password,
                           final Logger logger, final Executor executor) {
        super(logger, executor);
        this.host     = host;
        this.port     = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    protected HikariConfig buildHikariConfig() {
        final HikariConfig cfg = new HikariConfig();
        // Try MariaDB driver first, fall back to MySQL driver
        cfg.setDriverClassName("org.mariadb.jdbc.Driver");
        cfg.setJdbcUrl(String.format(
            "jdbc:mariadb://%s:%d/%s?useSSL=false&autoReconnect=true"
            + "&characterEncoding=utf8",
            host, port, database));
        cfg.setUsername(username);
        cfg.setPassword(password);
        return cfg;
    }

    /** MariaDB uses the same ON DUPLICATE KEY UPDATE syntax as MySQL. */
    @Override
    protected String onConflictUpdate(final String conflictColumns,
                                      final String updateClause) {
        final String mariaUpdate = updateClause.replaceAll(
            "(\\w+)=excluded\\.(\\w+)", "$1=VALUES($2)");
        return "ON DUPLICATE KEY UPDATE " + mariaUpdate;
    }

    @Override
    public @org.jetbrains.annotations.NotNull String getType() {
        return "mariadb";
    }
}
