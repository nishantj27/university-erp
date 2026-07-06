package edu.univ.erp.data;

import edu.univ.erp.domain.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reads and writes student profiles in the ERP database (erp_main.students). */
public class StudentDao {

    public Optional<Student> findByUserId(int userId) {
        String sql = "SELECT user_id, roll_no, full_name, program, year FROM students WHERE user_id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataException("could not load student " + userId, e);
        }
    }

    public List<Student> findAll() {
        String sql = "SELECT user_id, roll_no, full_name, program, year FROM students ORDER BY roll_no";
        List<Student> out = new ArrayList<>();
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new DataException("could not list students", e);
        }
    }

    public boolean rollNoExists(String rollNo) {
        String sql = "SELECT 1 FROM students WHERE roll_no = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataException("could not check roll number", e);
        }
    }

    public void insert(Student s) {
        String sql = "INSERT INTO students (user_id, roll_no, full_name, program, year) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getUserId());
            ps.setString(2, s.getRollNo());
            ps.setString(3, s.getFullName());
            ps.setString(4, s.getProgram());
            ps.setInt(5, s.getYear());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not save student " + s.getRollNo(), e);
        }
    }

    private Student map(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("user_id"),
                rs.getString("roll_no"),
                rs.getString("full_name"),
                rs.getString("program"),
                rs.getInt("year"));
    }
}
