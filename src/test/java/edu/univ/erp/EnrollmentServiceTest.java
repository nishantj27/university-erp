package edu.univ.erp;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.data.EnrollmentDao;
import edu.univ.erp.data.GradeDao;
import edu.univ.erp.data.SectionDao;
import edu.univ.erp.data.SettingsDao;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.EnrollmentService;
import edu.univ.erp.service.ServiceException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The registration rules (seats, duplicates, deadline, role) checked with stub DAOs so the logic
 * is tested on its own, without a database.
 */
class EnrollmentServiceTest {

    private final User student = new User(3, "stu1", Role.STUDENT, "ACTIVE");

    // a stub section DAO that always returns one section we control
    private static class StubSectionDao extends SectionDao {
        Section section;
        @Override public Optional<Section> findById(int id) { return Optional.ofNullable(section); }
    }

    // a stub enrollment DAO whose answers we can preset, and that records an insert
    private static class StubEnrollmentDao extends EnrollmentDao {
        boolean alreadyEnrolled = false;
        int enrolledCount = 0;
        Enrollment existing = null;
        boolean insertCalled = false;
        boolean deleteCalled = false;

        @Override public boolean exists(int s, int sec) { return alreadyEnrolled; }
        @Override public int countBySection(int sec) { return enrolledCount; }
        @Override public int insert(int s, int sec) { insertCalled = true; return 1; }
        @Override public Optional<Enrollment> find(int s, int sec) { return Optional.ofNullable(existing); }
        @Override public void deleteById(int id) { deleteCalled = true; }
    }

    private AccessControl accessMaintenanceOff() {
        return new AccessControl(new SettingsDao() {
            @Override public Optional<String> get(String key) { return Optional.of("false"); }
        });
    }

    private Section section(int capacity, LocalDate deadline) {
        Section s = new Section();
        s.setId(1);
        s.setCapacity(capacity);
        s.setAddDropDeadline(deadline);
        s.setCourseCode("CS101");
        return s;
    }

    private EnrollmentService service(StubSectionDao sd, StubEnrollmentDao ed) {
        return new EnrollmentService(sd, ed, new GradeDao(), accessMaintenanceOff());
    }

    @Test
    void registersWhenSeatsAvailableAndNotDuplicate() {
        StubSectionDao sd = new StubSectionDao();
        sd.section = section(30, LocalDate.now().plusDays(10));
        StubEnrollmentDao ed = new StubEnrollmentDao();
        ed.enrolledCount = 5;

        service(sd, ed).register(student, 1);
        assertTrue(ed.insertCalled, "should have inserted the enrollment");
    }

    @Test
    void blocksDuplicateRegistration() {
        StubSectionDao sd = new StubSectionDao();
        sd.section = section(30, LocalDate.now().plusDays(10));
        StubEnrollmentDao ed = new StubEnrollmentDao();
        ed.alreadyEnrolled = true;

        ServiceException ex = assertThrows(ServiceException.class, () -> service(sd, ed).register(student, 1));
        assertTrue(ex.getMessage().toLowerCase().contains("already registered"));
    }

    @Test
    void blocksWhenSectionFull() {
        StubSectionDao sd = new StubSectionDao();
        sd.section = section(2, LocalDate.now().plusDays(10));
        StubEnrollmentDao ed = new StubEnrollmentDao();
        ed.enrolledCount = 2;   // capacity reached

        ServiceException ex = assertThrows(ServiceException.class, () -> service(sd, ed).register(student, 1));
        assertEquals("Section full.", ex.getMessage());
    }

    @Test
    void blocksRegistrationAfterDeadline() {
        StubSectionDao sd = new StubSectionDao();
        sd.section = section(30, LocalDate.now().minusDays(1));  // deadline was yesterday
        StubEnrollmentDao ed = new StubEnrollmentDao();

        ServiceException ex = assertThrows(ServiceException.class, () -> service(sd, ed).register(student, 1));
        assertTrue(ex.getMessage().toLowerCase().contains("deadline"));
    }

    @Test
    void nonStudentCannotRegister() {
        StubSectionDao sd = new StubSectionDao();
        sd.section = section(30, LocalDate.now().plusDays(10));
        User instructor = new User(2, "inst1", Role.INSTRUCTOR, "ACTIVE");

        assertThrows(AccessDeniedException.class, () -> service(sd, new StubEnrollmentDao()).register(instructor, 1));
    }

    @Test
    void dropRemovesTheEnrollmentBeforeDeadline() {
        StubSectionDao sd = new StubSectionDao();
        sd.section = section(30, LocalDate.now().plusDays(10));
        StubEnrollmentDao ed = new StubEnrollmentDao();
        ed.existing = new Enrollment(7, 3, 1, "REGISTERED", null);

        service(sd, ed).drop(student, 1);
        assertTrue(ed.deleteCalled, "should have deleted the enrollment");
    }
}
