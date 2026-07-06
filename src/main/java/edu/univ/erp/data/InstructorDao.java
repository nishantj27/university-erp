package edu.univ.erp.data;

import edu.univ.erp.domain.Instructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reads and writes instructor profiles in the ERP database (erp_main.instructors). */
public class InstructorDao {

    public Optional<Instructor> findByUserId(int userId) {
        String sql = "SELECT user_id, full_name, department FROM instructors WHERE user_id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataException("could not load instructor " + userId, e);
        }
    }

    public List<Instructor> findAll() {
        String sql = "SELECT user_id, full_name, department FROM instructors ORDER BY full_name";
        List<Instructor> out = new ArrayList<>();
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new DataException("could not list instructors", e);
        }
    }

    public void insert(Instructor i) {
        String sql = "INSERT INTO instructors (user_id, full_name, department) VALUES (?, ?, ?)";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, i.getUserId());
            ps.setString(2, i.getFullName());
            ps.setString(3, i.getDepartment());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not save instructor " + i.getFullName(), e);
        }
    }

    private Instructor map(ResultSet rs) throws SQLException {
        return new Instructor(
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("department"));
    }
}
