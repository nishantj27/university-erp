package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

/**
 * All password hashing goes through here so the rest of the app never touches bcrypt directly.
 * We only ever store the hash; the plain password is used just long enough to hash or verify it.
 */
public final class PasswordHasher {

    // work factor: higher = slower = harder to brute force. 10 is a sensible default.
    private static final int COST = 10;

    private PasswordHasher() {
    }

    /** Hash a new/changed password. Each call uses a fresh random salt (bcrypt bakes it into the hash). */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST));
    }

    /** True if the typed password matches a stored hash. */
    public static boolean matches(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            // stored value wasn't a valid bcrypt hash - treat as a non-match rather than crashing
            return false;
        }
    }
}
