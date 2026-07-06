package edu.univ.erp.data;

import edu.univ.erp.domain.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Reads and writes the AUTH database (erp_auth.users_auth). This is the only DAO that ever touches
 * the password hash - and even here it stays inside the {@link AuthRow} that the auth service uses
 * for verification. Nothing outside the auth layer should see a hash.
 */
public class UserAuthDao {

    /** Full auth row, hash included. Kept package-ish: only the auth service asks for it. */
    public static final class AuthRow {
        public final int userId;
        public final String username;
        public final Role role;
        public final String passwordHash;
        public final String status;
        public final int failedAttempts;

        AuthRow(int userId, String username, Role role, String passwordHash, String status, int failedAttempts) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.passwordHash = passwordHash;
            this.status = status;
            this.failedAttempts = failedAttempts;
        }
    }

    public Optional<AuthRow> findByUsername(String username) {
        String sql = "SELECT user_id, username, role, password_hash, status, failed_attempts "
                + "FROM users_auth WHERE username = ?";
        try (Connection c = Db.auth();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new AuthRow(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        Role.from(rs.getString("role")),
                        rs.getString("password_hash"),
                        rs.getString("status"),
                        rs.getInt("failed_attempts")));
            }
        } catch (SQLException e) {
            throw new DataException("could not look up user " + username, e);
        }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users_auth WHERE username = ?";
        try (Connection c = Db.auth();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataException("could not check username " + username, e);
        }
    }

    /** Insert a new account and return the generated user_id. */
    public int insert(String username, Role role, String passwordHash) {
        String sql = "INSERT INTO users_auth (username, role, password_hash, status) VALUES (?, ?, ?, 'ACTIVE')";
        try (Connection c = Db.auth();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, role.name());
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataException("could not create account " + username, e);
        }
    }

    /** Successful login: stamp last_login and clear the failed-attempts counter. */
    public void markLoginSuccess(int userId) {
        String sql = "UPDATE users_auth SET last_login = NOW(), failed_attempts = 0 WHERE user_id = ?";
        runUpdate(sql, userId, "could not update last_login");
    }

    /** Wrong password: bump the counter and return the new value. */
    public int bumpFailedAttempts(int userId) {
        String update = "UPDATE users_auth SET failed_attempts = failed_attempts + 1 WHERE user_id = ?";
        String read = "SELECT failed_attempts FROM users_auth WHERE user_id = ?";
        try (Connection c = Db.auth()) {
            try (PreparedStatement ps = c.prepareStatement(update)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(read)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } catch (SQLException e) {
            throw new DataException("could not record failed attempt", e);
        }
    }

    public void lock(int userId) {
        runUpdate("UPDATE users_auth SET status = 'LOCKED' WHERE user_id = ?", userId, "could not lock account");
    }

    /**
     * Remove an auth account. Used to undo a half-finished user creation: the two databases can't
     * share one transaction, so if writing the ERP profile fails we delete the auth row we just made.
     */
    public void deleteById(int userId) {
        runUpdate("DELETE FROM users_auth WHERE user_id = ?", userId, "could not remove account");
    }

    public void updatePasswordHash(int userId, String newHash) {
        String sql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
        try (Connection c = Db.auth();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not change password", e);
        }
    }

    private void runUpdate(String sql, int userId, String errorMessage) {
        try (Connection c = Db.auth();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException(errorMessage, e);
        }
    }
}
