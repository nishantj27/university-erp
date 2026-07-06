package edu.univ.erp.access;

/**
 * Thrown when a user tries something their role doesn't allow, or tries to change data while the
 * system is in maintenance mode. The UI catches it and shows the message, then does nothing else.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
