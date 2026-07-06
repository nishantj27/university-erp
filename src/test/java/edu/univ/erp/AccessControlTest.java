package edu.univ.erp;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.data.SettingsDao;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The access rules are the heart of the project, so they get their own tests. We feed the
 * maintenance flag in through a tiny in-memory SettingsDao so no database is needed.
 */
class AccessControlTest {

    /** A SettingsDao that just returns a fixed maintenance value. */
    private AccessControl withMaintenance(boolean on) {
        SettingsDao stub = new SettingsDao() {
            @Override
            public Optional<String> get(String key) {
                return Optional.of(String.valueOf(on));
            }
        };
        return new AccessControl(stub);
    }

    private User user(Role role, int id) {
        return new User(id, role.name().toLowerCase(), role, "ACTIVE");
    }

    @Test
    void maintenanceBlocksStudentAndInstructorWrites() {
        AccessControl access = withMaintenance(true);
        assertThrows(AccessDeniedException.class, () -> access.ensureWritable(user(Role.STUDENT, 3)));
        assertThrows(AccessDeniedException.class, () -> access.ensureWritable(user(Role.INSTRUCTOR, 2)));
    }

    @Test
    void maintenanceDoesNotBlockAdmin() {
        AccessControl access = withMaintenance(true);
        assertDoesNotThrow(() -> access.ensureWritable(user(Role.ADMIN, 1)));
    }

    @Test
    void writesAllowedWhenMaintenanceOff() {
        AccessControl access = withMaintenance(false);
        assertDoesNotThrow(() -> access.ensureWritable(user(Role.STUDENT, 3)));
    }

    @Test
    void roleChecksRejectTheWrongRole() {
        AccessControl access = withMaintenance(false);
        assertThrows(AccessDeniedException.class, () -> access.ensureAdmin(user(Role.STUDENT, 3)));
        assertThrows(AccessDeniedException.class, () -> access.ensureInstructor(user(Role.STUDENT, 3)));
        assertDoesNotThrow(() -> access.ensureStudent(user(Role.STUDENT, 3)));
    }

    @Test
    void instructorCanOnlyTouchOwnSection() {
        AccessControl access = withMaintenance(false);
        User instructor = user(Role.INSTRUCTOR, 2);

        Section owned = new Section();
        owned.setInstructorId(2);
        assertDoesNotThrow(() -> access.ensureOwnsSection(instructor, owned));

        Section someoneElses = new Section();
        someoneElses.setInstructorId(99);
        assertThrows(AccessDeniedException.class, () -> access.ensureOwnsSection(instructor, someoneElses));

        Section unassigned = new Section();
        unassigned.setInstructorId(null);
        assertThrows(AccessDeniedException.class, () -> access.ensureOwnsSection(instructor, unassigned));
    }
}
