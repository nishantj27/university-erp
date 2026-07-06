package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.data.CourseDao;
import edu.univ.erp.data.EnrollmentDao;
import edu.univ.erp.data.InstructorDao;
import edu.univ.erp.data.SectionDao;
import edu.univ.erp.data.SettingsDao;
import edu.univ.erp.data.StudentDao;
import edu.univ.erp.data.UserAuthDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Admin-facing actions: add users, build the catalog, assign instructors and flip maintenance mode.
 * Adding a user is the interesting one - it writes to BOTH databases (the login row in auth, the
 * profile in ERP), and since they can't share a transaction we undo the auth row if the profile
 * write fails, so we never leave a login with no profile behind it.
 */
public class AdminService {

    private final UserAuthDao userAuthDao;
    private final StudentDao studentDao;
    private final InstructorDao instructorDao;
    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final SettingsDao settingsDao;
    private final AccessControl access;

    public AdminService() {
        this(new UserAuthDao(), new StudentDao(), new InstructorDao(), new CourseDao(),
                new SectionDao(), new EnrollmentDao(), new SettingsDao(), new AccessControl());
    }

    public AdminService(UserAuthDao userAuthDao, StudentDao studentDao, InstructorDao instructorDao,
                        CourseDao courseDao, SectionDao sectionDao, EnrollmentDao enrollmentDao,
                        SettingsDao settingsDao, AccessControl access) {
        this.userAuthDao = userAuthDao;
        this.studentDao = studentDao;
        this.instructorDao = instructorDao;
        this.courseDao = courseDao;
        this.sectionDao = sectionDao;
        this.enrollmentDao = enrollmentDao;
        this.settingsDao = settingsDao;
        this.access = access;
    }

    // --- users ------------------------------------------------------------

    public void addStudent(User admin, String username, String password,
                           String rollNo, String fullName, String program, int year) {
        access.ensureAdmin(admin);
        checkNewLogin(username, password);
        if (rollNo == null || rollNo.isBlank()) {
            throw new ServiceException("Roll number is required.");
        }
        if (studentDao.rollNoExists(rollNo.trim())) {
            throw new ServiceException("That roll number is already in use.");
        }
        requireText(fullName, "Full name is required.");

        int userId = userAuthDao.insert(username.trim(), Role.STUDENT, PasswordHasher.hash(password));
        try {
            studentDao.insert(new Student(userId, rollNo.trim(), fullName.trim(), safe(program), year));
        } catch (RuntimeException ex) {
            userAuthDao.deleteById(userId);   // roll back the orphaned login
            throw ex;
        }
    }

    public void addInstructor(User admin, String username, String password,
                              String fullName, String department) {
        access.ensureAdmin(admin);
        checkNewLogin(username, password);
        requireText(fullName, "Full name is required.");

        int userId = userAuthDao.insert(username.trim(), Role.INSTRUCTOR, PasswordHasher.hash(password));
        try {
            instructorDao.insert(new Instructor(userId, fullName.trim(), safe(department)));
        } catch (RuntimeException ex) {
            userAuthDao.deleteById(userId);
            throw ex;
        }
    }

    /** An extra admin account. Admins have no ERP profile - they only live in the auth db. */
    public void addAdmin(User admin, String username, String password) {
        access.ensureAdmin(admin);
        checkNewLogin(username, password);
        userAuthDao.insert(username.trim(), Role.ADMIN, PasswordHasher.hash(password));
    }

    // --- catalog ----------------------------------------------------------

    public int createCourse(User admin, String code, String title, int credits) {
        access.ensureAdmin(admin);
        requireText(code, "Course code is required.");
        requireText(title, "Course title is required.");
        if (credits <= 0) {
            throw new ServiceException("Credits must be greater than zero.");
        }
        if (courseDao.codeExists(code.trim())) {
            throw new ServiceException("A course with that code already exists.");
        }
        return courseDao.insert(new Course(0, code.trim(), title.trim(), credits));
    }

    public int createSection(User admin, int courseId, Integer instructorId, String dayTime, String room,
                             int capacity, String semester, int year, LocalDate addDropDeadline) {
        access.ensureAdmin(admin);
        if (courseDao.findById(courseId).isEmpty()) {
            throw new ServiceException("Pick a valid course for this section.");
        }
        if (capacity <= 0) {
            throw new ServiceException("Capacity must be greater than zero.");
        }
        if (instructorId != null && instructorDao.findByUserId(instructorId).isEmpty()) {
            throw new ServiceException("That instructor does not exist.");
        }
        Section s = new Section();
        s.setCourseId(courseId);
        s.setInstructorId(instructorId);
        s.setDayTime(safe(dayTime));
        s.setRoom(safe(room));
        s.setCapacity(capacity);
        s.setSemester(safe(semester));
        s.setYear(year);
        s.setAddDropDeadline(addDropDeadline);
        return sectionDao.insert(s);
    }

    public void assignInstructor(User admin, int sectionId, Integer instructorId) {
        access.ensureAdmin(admin);
        if (sectionDao.findById(sectionId).isEmpty()) {
            throw new ServiceException("Section not found.");
        }
        if (instructorId != null && instructorDao.findByUserId(instructorId).isEmpty()) {
            throw new ServiceException("That instructor does not exist.");
        }
        sectionDao.assignInstructor(sectionId, instructorId);
    }

    public void deleteSection(User admin, int sectionId) {
        access.ensureAdmin(admin);
        // don't orphan enrolled students - block the delete and explain why
        if (enrollmentDao.countBySection(sectionId) > 0) {
            throw new ServiceException("Can't delete this section - students are still enrolled in it.");
        }
        sectionDao.delete(sectionId);
    }

    // --- maintenance mode -------------------------------------------------

    public void setMaintenance(User admin, boolean on) {
        access.ensureAdmin(admin);
        settingsDao.set(AccessControl.MAINTENANCE_KEY, on ? "true" : "false");
    }

    public boolean isMaintenanceOn() {
        return access.isMaintenanceOn();
    }

    // --- read helpers for the admin screens -------------------------------

    public List<Student> listStudents(User admin) {
        access.ensureAdmin(admin);
        return studentDao.findAll();
    }

    public List<Instructor> listInstructors(User admin) {
        access.ensureAdmin(admin);
        return instructorDao.findAll();
    }

    public List<Course> listCourses(User admin) {
        access.ensureAdmin(admin);
        return courseDao.findAll();
    }

    public List<Section> listSections(User admin) {
        access.ensureAdmin(admin);
        return sectionDao.findAll();
    }

    // --- validation helpers -----------------------------------------------

    private void checkNewLogin(String username, String password) {
        requireText(username, "Username is required.");
        if (password == null || password.length() < 6) {
            throw new ServiceException("Password must be at least 6 characters.");
        }
        if (userAuthDao.usernameExists(username.trim())) {
            throw new ServiceException("That username is already taken.");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ServiceException(message);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
