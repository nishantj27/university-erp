package edu.univ.erp.data;

import edu.univ.erp.domain.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reads and writes the course catalog (erp_main.courses). */
public class CourseDao {

    public List<Course> findAll() {
        String sql = "SELECT id, code, title, credits FROM courses ORDER BY code";
        List<Course> out = new ArrayList<>();
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new DataException("could not list courses", e);
        }
    }

    public Optional<Course> findById(int id) {
        String sql = "SELECT id, code, title, credits FROM courses WHERE id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataException("could not load course " + id, e);
        }
    }

    public boolean codeExists(String code) {
        String sql = "SELECT 1 FROM courses WHERE code = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataException("could not check course code", e);
        }
    }

    public int insert(Course course) {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getCode());
            ps.setString(2, course.getTitle());
            ps.setInt(3, course.getCredits());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataException("could not save course " + course.getCode(), e);
        }
    }

    public void update(Course course) {
        String sql = "UPDATE courses SET code = ?, title = ?, credits = ? WHERE id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, course.getCode());
            ps.setString(2, course.getTitle());
            ps.setInt(3, course.getCredits());
            ps.setInt(4, course.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not update course " + course.getCode(), e);
        }
    }

    private Course map(ResultSet rs) throws SQLException {
        return new Course(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("title"),
                rs.getInt("credits"));
    }
}
