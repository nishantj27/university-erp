package edu.univ.erp.data;

import edu.univ.erp.domain.Assessment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes the assessment scheme for a section (erp_main.assessments). */
public class AssessmentDao {

    public List<Assessment> findBySection(int sectionId) {
        String sql = "SELECT id, section_id, name, weight, max_score FROM assessments "
                + "WHERE section_id = ? ORDER BY id";
        List<Assessment> out = new ArrayList<>();
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
            return out;
        } catch (SQLException e) {
            throw new DataException("could not list assessments for section " + sectionId, e);
        }
    }

    public int insert(Assessment a) {
        String sql = "INSERT INTO assessments (section_id, name, weight, max_score) VALUES (?, ?, ?, ?)";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getSectionId());
            ps.setString(2, a.getName());
            ps.setDouble(3, a.getWeight());
            ps.setDouble(4, a.getMaxScore());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataException("could not save assessment", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM assessments WHERE id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not delete assessment " + id, e);
        }
    }

    private Assessment map(ResultSet rs) throws SQLException {
        return new Assessment(
                rs.getInt("id"),
                rs.getInt("section_id"),
                rs.getString("name"),
                rs.getDouble("weight"),
                rs.getDouble("max_score"));
    }
}
