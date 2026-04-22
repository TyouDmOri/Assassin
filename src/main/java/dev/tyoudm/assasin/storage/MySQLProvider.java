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
 * MySQL storage provider using HikariCP.
 *
 * <p>Uses the MySQL Connector/J driver. The driver must be available on
 * the server classpath or shaded into the JAR.
 *
 * <h2>MySQL-specific settings</h2>
 * <ul>
 *   <li>Uses {@code ON DUPLICATE KEY UPDATE} for upserts.</li>
 *   <li>UTF-8 charset enforced via connection properties.</li>
 *   <li>Auto-reconnect enabled.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MySQLProvider extends AbstractSqlProvider {

    private final String host;
    private final int    port;
    private final String database;
    private final String username;
    private final String password;

    public MySQLProvider(final String host, final int port, final String database,
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
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        cfg.setJdbcUrl(String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true"
            + "&characterEncoding=utf8&useUnicode=true",
            host, port, database));
        cfg.setUsername(username);
        cfg.setPassword(password);
        return cfg;
    }

    /** MySQL uses ON DUPLICATE KEY UPDATE instead of ON CONFLICT. */
    @Override
    protected String onConflictUpdate(final String conflictColumns,
                                      final String updateClause) {
        // MySQL syntax: ON DUPLICATE KEY UPDATE col=VALUES(col), ...
        // Convert "col=excluded.col" → "col=VALUES(col)"
        final String mysqlUpdate = updateClause.replaceAll(
            "(\\w+)=excluded\\.(\\w+)", "$1=VALUES($2)");
        return "ON DUPLICATE KEY UPDATE " + mysqlUpdate;
    }

    @Override
    public @org.jetbrains.annotations.NotNull String getType() {
        return "mysql";
    }
}
