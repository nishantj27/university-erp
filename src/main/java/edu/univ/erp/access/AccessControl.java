package edu.univ.erp.access;

import edu.univ.erp.data.SettingsDao;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

/**
 * The single place that answers two questions for every action that changes data:
 *   1. "Is this user allowed to do this?"   (role check)
 *   2. "Is the system writable right now?"   (maintenance-mode check)
 *
 * The service layer calls these guards before it writes anything, so the rules are enforced in one
 * spot instead of being re-checked (and possibly forgotten) on each screen.
 */
public class AccessControl {

    public static final String MAINTENANCE_KEY = "maintenance_on";

    private final SettingsDao settingsDao;

    public AccessControl() {
        this(new SettingsDao());
    }

    public AccessControl(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }

    // --- maintenance mode -------------------------------------------------

    public boolean isMaintenanceOn() {
        return settingsDao.get(MAINTENANCE_KEY)
                .map(v -> v.equalsIgnoreCase("true"))
                .orElse(false);
    }

    /**
     * Block writes while maintenance is ON - but the admin is exempt, since the admin is the one
     * who turns it on and off.
     */
    public void ensureWritable(User user) {
        if (isMaintenanceOn() && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "The system is in maintenance mode. You can view data but not make changes right now.");
        }
    }

    // --- role checks ------------------------------------------------------

    public void ensureAdmin(User user) {
        require(user.getRole() == Role.ADMIN, "You are not allowed to do this.");
    }

    public void ensureStudent(User user) {
        require(user.getRole() == Role.STUDENT, "You are not allowed to do this.");
    }

    public void ensureInstructor(User user) {
        require(user.getRole() == Role.INSTRUCTOR, "You are not allowed to do this.");
    }

    /** An instructor may only touch sections they are assigned to. */
    public void ensureOwnsSection(User user, Section section) {
        boolean owns = section.getInstructorId() != null
                && section.getInstructorId() == user.getUserId();
        require(owns, "Not your section.");
    }

    private void require(boolean allowed, String message) {
        if (!allowed) {
            throw new AccessDeniedException(message);
        }
    }
}
