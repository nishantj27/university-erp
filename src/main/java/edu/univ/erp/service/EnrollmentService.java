package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.EnrollmentDao;
import edu.univ.erp.data.GradeDao;
import edu.univ.erp.data.SectionDao;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Student-facing actions: register, drop, timetable and grades. Every method that changes data
 * first checks the role and that the system is writable (not in maintenance) via {@link AccessControl},
 * then applies the business rules (seats available, no duplicates, deadline not passed).
 */
public class EnrollmentService {

    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;
    private final AccessControl access;

    public EnrollmentService() {
        this(new SectionDao(), new EnrollmentDao(), new GradeDao(), new AccessControl());
    }

    public EnrollmentService(SectionDao sectionDao, EnrollmentDao enrollmentDao,
                             GradeDao gradeDao, AccessControl access) {
        this.sectionDao = sectionDao;
        this.enrollmentDao = enrollmentDao;
        this.gradeDao = gradeDao;
        this.access = access;
    }

    /** Register the student into a section, if the rules allow it. */
    public void register(User student, int sectionId) {
        access.ensureStudent(student);
        access.ensureWritable(student);

        Section section = sectionDao.findById(sectionId)
                .orElseThrow(() -> new ServiceException("That section no longer exists."));

        if (enrollmentDao.exists(student.getUserId(), sectionId)) {
            throw new ServiceException("You are already registered in this section.");
        }
        if (deadlinePassed(section)) {
            throw new ServiceException("The add/drop deadline for this section has passed.");
        }
        int enrolled = enrollmentDao.countBySection(sectionId);
        if (enrolled >= section.getCapacity()) {
            throw new ServiceException("Section full.");
        }

        enrollmentDao.insert(student.getUserId(), sectionId);
    }

    /** Drop a section the student is registered in, if the deadline hasn't passed. */
    public void drop(User student, int sectionId) {
        access.ensureStudent(student);
        access.ensureWritable(student);

        Enrollment enrollment = enrollmentDao.find(student.getUserId(), sectionId)
                .orElseThrow(() -> new ServiceException("You are not registered in this section."));

        Section section = sectionDao.findById(sectionId)
                .orElseThrow(() -> new ServiceException("That section no longer exists."));
        if (deadlinePassed(section)) {
            throw new ServiceException("The drop deadline for this section has passed.");
        }

        enrollmentDao.deleteById(enrollment.getId());
    }

    /** The sections a student is registered in (for "My Registrations" and the timetable). */
    public List<Section> myRegisteredSections(User student) {
        access.ensureStudent(student);
        return sectionDao.findRegisteredByStudent(student.getUserId());
    }

    /** All of a student's enrollments with course info and final grade (for the grades screen). */
    public List<Enrollment> myEnrollments(User student) {
        access.ensureStudent(student);
        return enrollmentDao.findByStudent(student.getUserId());
    }

    /** The per-assessment score breakdown for one of the student's own enrollments. */
    public List<Grade> gradeBreakdown(User student, int enrollmentId) {
        access.ensureStudent(student);
        Enrollment enrollment = enrollmentDao.findById(enrollmentId)
                .orElseThrow(() -> new ServiceException("Enrollment not found."));
        // a student may only look at their own records
        if (enrollment.getStudentId() != student.getUserId()) {
            throw new ServiceException("You can only view your own grades.");
        }
        return gradeDao.findByEnrollment(enrollmentId);
    }

    private boolean deadlinePassed(Section section) {
        LocalDate deadline = section.getAddDropDeadline();
        return deadline != null && LocalDate.now().isAfter(deadline);
    }
}
