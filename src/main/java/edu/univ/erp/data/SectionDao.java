package edu.univ.erp.data;

import edu.univ.erp.domain.Section;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads and writes sections (erp_main.sections). The read queries join in the course code/title,
 * the instructor name, and a live count of how many students are enrolled, so the UI can show a
 * useful catalog row without extra round trips.
 */
public class SectionDao {

    // shared projection used by every read below
    private static final String BASE_SELECT =
            "SELECT s.id, s.course_id, s.instructor_id, s.day_time, s.room, s.capacity, "
          + "       s.semester, s.year, s.add_drop_deadline, "
          + "       c.code AS course_code, c.title AS course_title, c.credits AS credits, "
          + "       i.full_name AS instructor_name, "
          + "       (SELECT COUNT(*) FROM enrollments e WHERE e.section_id = s.id) AS enrolled_count "
          + "FROM sections s "
          + "JOIN courses c ON c.id = s.course_id "
          + "LEFT JOIN instructors i ON i.user_id = s.instructor_id ";

    public List<Section> findAll() {
        return query(BASE_SELECT + "ORDER BY c.code");
    }

    public List<Section> findByInstructor(int instructorId) {
        String sql = BASE_SELECT + "WHERE s.instructor_id = ? ORDER BY c.code";
        List<Section> out = new ArrayList<>();
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, instructorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
            return out;
        } catch (SQLException e) {
            throw new DataException("could not list sections for instructor " + instructorId, e);
        }
    }

    /** Sections a student is currently registered in - drives the timetable and "my registrations". */
    public List<Section> findRegisteredByStudent(int studentId) {
        String sql = BASE_SELECT
                + "JOIN enrollments en ON en.section_id = s.id "
                + "WHERE en.student_id = ? ORDER BY c.code";
        List<Section> out = new ArrayList<>();
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
            return out;
        } catch (SQLException e) {
            throw new DataException("could not list registered sections for student " + studentId, e);
        }
    }

    public Optional<Section> findById(int id) {
        String sql = BASE_SELECT + "WHERE s.id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataException("could not load section " + id, e);
        }
    }

    public int insert(Section s) {
        String sql = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, "
                + "semester, year, add_drop_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getCourseId());
            setNullableInt(ps, 2, s.getInstructorId());
            ps.setString(3, s.getDayTime());
            ps.setString(4, s.getRoom());
            ps.setInt(5, s.getCapacity());
            ps.setString(6, s.getSemester());
            ps.setInt(7, s.getYear());
            ps.setDate(8, s.getAddDropDeadline() == null ? null : Date.valueOf(s.getAddDropDeadline()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataException("could not save section", e);
        }
    }

    public void update(Section s) {
        String sql = "UPDATE sections SET course_id = ?, instructor_id = ?, day_time = ?, room = ?, "
                + "capacity = ?, semester = ?, year = ?, add_drop_deadline = ? WHERE id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getCourseId());
            setNullableInt(ps, 2, s.getInstructorId());
            ps.setString(3, s.getDayTime());
            ps.setString(4, s.getRoom());
            ps.setInt(5, s.getCapacity());
            ps.setString(6, s.getSemester());
            ps.setInt(7, s.getYear());
            ps.setDate(8, s.getAddDropDeadline() == null ? null : Date.valueOf(s.getAddDropDeadline()));
            ps.setInt(9, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not update section " + s.getId(), e);
        }
    }

    public void assignInstructor(int sectionId, Integer instructorId) {
        String sql = "UPDATE sections SET instructor_id = ? WHERE id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setNullableInt(ps, 1, instructorId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not assign instructor to section " + sectionId, e);
        }
    }

    /**
     * Delete a section. This will fail (throw) if students are still enrolled, because of the
     * foreign key from enrollments - the service turns that into a friendly "students are enrolled"
     * message rather than crashing.
     */
    public void delete(int sectionId) {
        String sql = "DELETE FROM sections WHERE id = ?";
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("could not delete section " + sectionId, e);
        }
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private List<Section> query(String sql) {
        List<Section> out = new ArrayList<>();
        try (Connection c = Db.erp();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new DataException("could not list sections", e);
        }
    }

    private Section map(ResultSet rs) throws SQLException {
        Section s = new Section();
        s.setId(rs.getInt("id"));
        s.setCourseId(rs.getInt("course_id"));
        int instructorId = rs.getInt("instructor_id");
        s.setInstructorId(rs.wasNull() ? null : instructorId);
        s.setDayTime(rs.getString("day_time"));
        s.setRoom(rs.getString("room"));
        s.setCapacity(rs.getInt("capacity"));
        s.setSemester(rs.getString("semester"));
        s.setYear(rs.getInt("year"));
        Date deadline = rs.getDate("add_drop_deadline");
        s.setAddDropDeadline(deadline == null ? null : deadline.toLocalDate());
        s.setCourseCode(rs.getString("course_code"));
        s.setCourseTitle(rs.getString("course_title"));
        s.setCredits(rs.getInt("credits"));
        s.setInstructorName(rs.getString("instructor_name"));
        s.setEnrolledCount(rs.getInt("enrolled_count"));
        return s;
    }
}
