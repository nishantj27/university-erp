package edu.univ.erp.auth;

import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.User;

/**
 * Who is logged in right now. Created on a successful login and handed to the dashboards so every
 * screen knows the current user's id and role. Deliberately holds no password or hash.
 */
public class Session {

    private final User user;

    public Session(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public int getUserId() {
        return user.getUserId();
    }

    public Role getRole() {
        return user.getRole();
    }

    public boolean isAdmin() {
        return user.getRole() == Role.ADMIN;
    }

    public boolean isInstructor() {
        return user.getRole() == Role.INSTRUCTOR;
    }

    public boolean isStudent() {
        return user.getRole() == Role.STUDENT;
    }
}
