package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reads and writes enrollments (erp_main.enrollments). */
public class EnrollmentDao {

    public boolean exists(int studentId, int sectionId) {
        String sql = "SELECT 1 FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataException("could not check enrollment", e);
        }
    }

    public int countBySection(int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataException("could not count enrollments", e);
        }
    }

    public int insert(int studentId, int sectionId) {
        String sql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'REGISTERED')";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataException("could not register student", e);
        }
    }

    public Optional<Enrollment> findById(int id) {
        String sql = "SELECT id, student_id, section_id, status, final_grade FROM enrollments WHERE id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapBasic(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataException("could not load enrollment " + id, e);
        }
    }

    public Optional<Enrollment> find(int studentId, int sectionId) {
        String sql = "SELECT id, student_id, section_id, status, final_grade "
                + "FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapBasic(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataException("could not load enrollment", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM enrollments WHERE id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not drop enrollment " + id, e);
        }
    }

    public void updateFinalGrade(int enrollmentId, Double finalGrade) {
        String sql = "UPDATE enrollments SET final_grade = ? WHERE id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (finalGrade == null) {
                ps.setNull(1, Types.DOUBLE);
            } else {
                ps.setDouble(1, finalGrade);
            }
            ps.setInt(2, enrollmentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not save final grade", e);
        }
    }

    /** A student's enrollments with course info - used for grades and the transcript. */
    public List<Enrollment> findByStudent(int studentId) {
        String sql = "SELECT e.id, e.student_id, e.section_id, e.status, e.final_grade, "
                + "c.code AS course_code, c.title AS course_title, c.credits AS credits "
                + "FROM enrollments e "
                + "JOIN sections s ON s.id = e.section_id "
                + "JOIN courses c ON c.id = s.course_id "
                + "WHERE e.student_id = ? ORDER BY c.code";
        List<Enrollment> out = new ArrayList<>();
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enrollment e = mapBasic(rs);
                    e.setCourseCode(rs.getString("course_code"));
                    e.setCourseTitle(rs.getString("course_title"));
                    e.setCredits(rs.getInt("credits"));
                    out.add(e);
                }
            }
            return out;
        } catch (SQLException e) {
            throw new DataException("could not list student's enrollments", e);
        }
    }

    /** Everyone enrolled in a section with student name/roll - used by the instructor gradebook. */
    public List<Enrollment> findBySection(int sectionId) {
        String sql = "SELECT e.id, e.student_id, e.section_id, e.status, e.final_grade, "
                + "st.roll_no AS roll_no, st.full_name AS student_name "
                + "FROM enrollments e "
                + "JOIN students st ON st.user_id = e.student_id "
                + "WHERE e.section_id = ? ORDER BY st.roll_no";
        List<Enrollment> out = new ArrayList<>();
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enrollment e = mapBasic(rs);
                    e.setStudentRollNo(rs.getString("roll_no"));
                    e.setStudentName(rs.getString("student_name"));
                    out.add(e);
                }
            }
            return out;
        } catch (SQLException e) {
            throw new DataException("could not list section's enrollments", e);
        }
    }

    private Enrollment mapBasic(ResultSet rs) throws SQLException {
        double fg = rs.getDouble("final_grade");
        Double finalGrade = rs.wasNull() ? null : fg;
        return new Enrollment(
                rs.getInt("id"),
                rs.getInt("student_id"),
                rs.getInt("section_id"),
                rs.getString("status"),
                finalGrade);
    }
}
