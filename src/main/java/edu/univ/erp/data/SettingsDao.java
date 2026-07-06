package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Key/value app settings (erp_main.settings). Used mainly for the maintenance-mode flag. */
public class SettingsDao {

    public Optional<String> get(String key) {
        String sql = "SELECT svalue FROM settings WHERE skey = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.ofNullable(rs.getString(1)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataException("could not read setting " + key, e);
        }
    }

    public void set(String key, String value) {
        String sql = "INSERT INTO settings (skey, svalue) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE svalue = VALUES(svalue)";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not save setting " + key, e);
        }
    }
}
