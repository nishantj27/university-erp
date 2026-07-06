package edu.univ.erp.service;

import edu.univ.erp.data.InstructorDao;
import edu.univ.erp.data.StudentDao;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;

import java.util.Optional;

/**
 * Loads the ERP-side profile for a logged-in user. This is the step that ties the two databases
 * together after login: the auth db said "user 3 is a STUDENT", and here we fetch student #3's
 * profile from the ERP db using that shared user_id.
 */
public class ProfileService {

    private final StudentDao studentDao;
    private final InstructorDao instructorDao;

    public ProfileService() {
        this(new StudentDao(), new InstructorDao());
    }

    public ProfileService(StudentDao studentDao, InstructorDao instructorDao) {
        this.studentDao = studentDao;
        this.instructorDao = instructorDao;
    }

    public Optional<Student> student(User user) {
        return studentDao.findByUserId(user.getUserId());
    }

    public Optional<Instructor> instructor(User user) {
        return instructorDao.findByUserId(user.getUserId());
    }

    /** A friendly display name for the header, falling back to the username if there's no profile. */
    public String displayName(User user) {
        switch (user.getRole()) {
            case STUDENT:
                return student(user).map(Student::getFullName).orElse(user.getUsername());
            case INSTRUCTOR:
                return instructor(user).map(Instructor::getFullName).orElse(user.getUsername());
            default:
                return user.getUsername();
        }
    }
}
