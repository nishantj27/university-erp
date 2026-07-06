package edu.univ.erp;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.AssessmentDao;
import edu.univ.erp.data.EnrollmentDao;
import edu.univ.erp.data.GradeDao;
import edu.univ.erp.data.SectionDao;
import edu.univ.erp.data.SettingsDao;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.GradeService;
import edu.univ.erp.service.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The final-grade weighting is the trickiest bit of business logic, so it gets a focused test:
 * scores 18/25/40 against a 20/30/50 scheme (all out of 100) must come out to 31.1.
 */
class GradeServiceTest {

    private final User instructor = new User(2, "inst1", Role.INSTRUCTOR, "ACTIVE");

    private Section ownedSection() {
        Section s = new Section();
        s.setId(1);
        s.setInstructorId(2);   // owned by our instructor
        return s;
    }

    private List<Assessment> scheme() {
        return List.of(
                new Assessment(1, 1, "Quiz", 20, 100),
                new Assessment(2, 1, "Midterm", 30, 100),
                new Assessment(3, 1, "End-sem", 50, 100));
    }

    private AccessControl accessMaintenanceOff() {
        return new AccessControl(new SettingsDao() {
            @Override public Optional<String> get(String key) { return Optional.of("false"); }
        });
    }

    @Test
    void computesWeightedFinalCorrectly() {
        SectionDao sd = new SectionDao() {
            @Override public Optional<Section> findById(int id) { return Optional.of(ownedSection()); }
        };
        AssessmentDao ad = new AssessmentDao() {
            @Override public List<Assessment> findBySection(int sec) { return scheme(); }
        };
        final Double[] savedFinal = new Double[1];
        EnrollmentDao ed = new EnrollmentDao() {
            @Override public List<Enrollment> findBySection(int sec) {
                return List.of(new Enrollment(1, 3, 1, "REGISTERED", null));
            }
            @Override public void updateFinalGrade(int enrollmentId, Double finalGrade) {
                savedFinal[0] = finalGrade;
            }
        };
        GradeDao gd = new GradeDao() {
            @Override public List<Grade> findByEnrollment(int enrollmentId) {
                return List.of(
                        new Grade(1, 1, 1, 18),
                        new Grade(2, 1, 2, 25),
                        new Grade(3, 1, 3, 40));
            }
        };

        GradeService service = new GradeService(sd, ed, ad, gd, accessMaintenanceOff());
        int count = service.computeFinals(instructor, 1);

        assertEquals(1, count);
        assertNotNull(savedFinal[0]);
        assertEquals(31.1, savedFinal[0], 0.0001);   // 3.6 + 7.5 + 20.0
    }

    @Test
    void missingScoreCountsAsZero() {
        SectionDao sd = new SectionDao() {
            @Override public Optional<Section> findById(int id) { return Optional.of(ownedSection()); }
        };
        AssessmentDao ad = new AssessmentDao() {
            @Override public List<Assessment> findBySection(int sec) { return scheme(); }
        };
        final Double[] savedFinal = new Double[1];
        EnrollmentDao ed = new EnrollmentDao() {
            @Override public List<Enrollment> findBySection(int sec) {
                return List.of(new Enrollment(1, 3, 1, "REGISTERED", null));
            }
            @Override public void updateFinalGrade(int enrollmentId, Double finalGrade) { savedFinal[0] = finalGrade; }
        };
        GradeDao gd = new GradeDao() {
            @Override public List<Grade> findByEnrollment(int enrollmentId) {
                // only the quiz was graded: 20/100 * 20 = 4.0, the rest count as 0
                return List.of(new Grade(1, 1, 1, 20));
            }
        };

        GradeService service = new GradeService(sd, ed, ad, gd, accessMaintenanceOff());
        service.computeFinals(instructor, 1);
        assertEquals(4.0, savedFinal[0], 0.0001);
    }

    @Test
    void rejectsScoreAboveMax() {
        SectionDao sd = new SectionDao() {
            @Override public Optional<Section> findById(int id) { return Optional.of(ownedSection()); }
        };
        AssessmentDao ad = new AssessmentDao() {
            @Override public List<Assessment> findBySection(int sec) { return scheme(); }
        };
        EnrollmentDao ed = new EnrollmentDao() {
            @Override public Optional<Enrollment> findById(int id) {
                return Optional.of(new Enrollment(1, 3, 1, "REGISTERED", null));
            }
        };
        GradeService service = new GradeService(sd, ed, ad, new GradeDao(), accessMaintenanceOff());

        // quiz is out of 100; entering 150 should be rejected
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.enterScore(instructor, 1, 1, 1, 150));
        assertEquals("Score must be between 0 and 100.0.", ex.getMessage());
    }
}
