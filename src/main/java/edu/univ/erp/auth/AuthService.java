package edu.univ.erp.auth;

import edu.univ.erp.data.UserAuthDao;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.util.Config;

import java.util.Optional;

/**
 * Handles login and password changes. This is the ONLY place that reads a password hash, and it
 * talks exclusively to the auth database - it never touches the ERP db. Loading the student /
 * instructor profile afterwards is the ERP side's job.
 */
public class AuthService {

    private final UserAuthDao userAuthDao;
    private final int maxFailedAttempts;

    public AuthService() {
        this(new UserAuthDao());
    }

    public AuthService(UserAuthDao userAuthDao) {
        this.userAuthDao = userAuthDao;
        this.maxFailedAttempts = Config.getInt("auth.max.failed.attempts", 5);
    }

    /**
     * Verify a username/password against the auth db. On success the caller gets a {@link User}
     * (id + role, no hash). We keep the failure message vague on purpose so it doesn't reveal
     * whether it was the username or the password that was wrong.
     */
    public AuthResult login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return AuthResult.failure("Please enter both username and password.");
        }

        Optional<UserAuthDao.AuthRow> found = userAuthDao.findByUsername(username.trim());
        if (found.isEmpty()) {
            return AuthResult.failure("Incorrect username or password.");
        }

        UserAuthDao.AuthRow row = found.get();

        if ("LOCKED".equalsIgnoreCase(row.status)) {
            return AuthResult.failure("This account is locked. Please contact the admin.");
        }

        if (PasswordHasher.matches(password, row.passwordHash)) {
            userAuthDao.markLoginSuccess(row.userId);
            User user = new User(row.userId, row.username, row.role, "ACTIVE");
            return AuthResult.success(user);
        }

        // wrong password - record the attempt and lock the account if it has failed too many times
        int attempts = userAuthDao.bumpFailedAttempts(row.userId);
        if (maxFailedAttempts > 0 && attempts >= maxFailedAttempts) {
            userAuthDao.lock(row.userId);
            return AuthResult.failure("Too many failed attempts - this account is now locked.");
        }
        return AuthResult.failure("Incorrect username or password.");
    }

    /**
     * Change the logged-in user's password after re-checking the current one. Throws a
     * {@link ServiceException} with a friendly message if the current password is wrong or the new
     * one is too short.
     */
    public void changePassword(User user, String currentPassword, String newPassword) {
        Optional<UserAuthDao.AuthRow> found = userAuthDao.findByUsername(user.getUsername());
        if (found.isEmpty()) {
            throw new ServiceException("Account not found.");
        }
        if (!PasswordHasher.matches(currentPassword, found.get().passwordHash)) {
            throw new ServiceException("Your current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ServiceException("New password must be at least 6 characters.");
        }
        userAuthDao.updatePasswordHash(user.getUserId(), PasswordHasher.hash(newPassword));
    }
}
