package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import edu.univ.erp.util.Config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Owns the JDBC connections. There are two independent connection pools because there are two
 * databases: one for auth, one for the ERP data. The rest of the data layer asks this class for a
 * connection and never builds its own.
 */
public final class Db {

    private static HikariDataSource authPool;
    private static HikariDataSource erpPool;

    private Db() {
    }

    /** Build both pools. Safe to call more than once; it only sets things up on the first call. */
    public static synchronized void init() {
        if (authPool != null) {
            return;
        }
        authPool = buildPool(
                Config.get("auth.jdbc.url"),
                Config.get("auth.jdbc.user"),
                Config.get("auth.jdbc.password"),
                "auth-pool");
        erpPool = buildPool(
                Config.get("erp.jdbc.url"),
                Config.get("erp.jdbc.user"),
                Config.get("erp.jdbc.password"),
                "erp-pool");
    }

    private static HikariDataSource buildPool(String url, String user, String pass, String name) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setPoolName(name);
        cfg.setMaximumPoolSize(5);
        cfg.setConnectionTimeout(5000);
        return new HikariDataSource(cfg);
    }

    /** Connection to the AUTH database. Caller is responsible for closing it (use try-with-resources). */
    public static Connection auth() throws SQLException {
        ensureReady();
        return authPool.getConnection();
    }

    /** Connection to the ERP database. Caller is responsible for closing it (use try-with-resources). */
    public static Connection erp() throws SQLException {
        ensureReady();
        return erpPool.getConnection();
    }

    private static void ensureReady() {
        if (authPool == null) {
            init();
        }
    }

    /** Close both pools on shutdown. */
    public static synchronized void shutdown() {
        if (authPool != null) {
            authPool.close();
            authPool = null;
        }
        if (erpPool != null) {
            erpPool.close();
            erpPool = null;
        }
    }
}
