package edu.univ.erp.auth;

import edu.univ.erp.domain.User;

/**
 * Outcome of a login attempt: either success with the user, or failure with a message to show.
 * Keeping both in one object means the UI just checks {@link #isOk()} and reads the message.
 */
public class AuthResult {

    private final boolean ok;
    private final User user;
    private final String message;

    private AuthResult(boolean ok, User user, String message) {
        this.ok = ok;
        this.user = user;
        this.message = message;
    }

    public static AuthResult success(User user) {
        return new AuthResult(true, user, "Login successful");
    }

    public static AuthResult failure(String message) {
        return new AuthResult(false, null, message);
    }

    public boolean isOk() {
        return ok;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }
}
