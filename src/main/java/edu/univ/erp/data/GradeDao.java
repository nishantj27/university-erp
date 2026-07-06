package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes assessment scores (erp_main.grades). */
public class GradeDao {

    /** Scores for one enrollment, joined with the assessment name/weight/max so the final can be computed. */
    public List<Grade> findByEnrollment(int enrollmentId) {
        String sql = "SELECT g.id, g.enrollment_id, g.assessment_id, g.score, "
                + "a.name AS name, a.weight AS weight, a.max_score AS max_score "
                + "FROM grades g JOIN assessments a ON a.id = g.assessment_id "
                + "WHERE g.enrollment_id = ? ORDER BY a.id";
        List<Grade> out = new ArrayList<>();
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Grade g = new Grade(
                            rs.getInt("id"),
                            rs.getInt("enrollment_id"),
                            rs.getInt("assessment_id"),
                            rs.getDouble("score"));
                    g.setAssessmentName(rs.getString("name"));
                    g.setWeight(rs.getDouble("weight"));
                    g.setMaxScore(rs.getDouble("max_score"));
                    out.add(g);
                }
            }
            return out;
        } catch (SQLException e) {
            throw new DataException("could not list grades for enrollment " + enrollmentId, e);
        }
    }

    /**
     * Save a score for (enrollment, assessment). Because of the unique key on that pair, entering a
     * score a second time updates the existing row instead of creating a duplicate.
     */
    public void upsert(int enrollmentId, int assessmentId, double score) {
        String sql = "INSERT INTO grades (enrollment_id, assessment_id, score) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE score = VALUES(score)";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.setInt(2, assessmentId);
            ps.setDouble(3, score);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not save score", e);
        }
    }
}
