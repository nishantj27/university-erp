package edu.univ.erp.domain;

/**
 * The three kinds of user in the system. Stored as a plain string in the auth db
 * ("ADMIN" / "INSTRUCTOR" / "STUDENT") and used all over the access checks.
 */
public enum Role {
    ADMIN,
    INSTRUCTOR,
    STUDENT;

    /** Parse a role coming from the database, being a bit forgiving about case. */
    public static Role from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("role is null");
        }
        return Role.valueOf(value.trim().toUpperCase());
    }
}
