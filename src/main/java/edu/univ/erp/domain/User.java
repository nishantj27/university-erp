package edu.univ.erp.domain;

/**
 * An account from the auth db (erp_auth.users_auth). This is the "who are you" record:
 * username + role + the login status. The password hash is intentionally NOT kept here so
 * it never gets passed around the app or shown on a screen.
 */
public class User {

    private int userId;
    private String username;
    private Role role;
    private String status;   // "ACTIVE" or "LOCKED"

    public User() {
    }

    public User(int userId, String username, Role role, String status) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLocked() {
        return "LOCKED".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
