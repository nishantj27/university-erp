package edu.univ.erp;

import edu.univ.erp.auth.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The password hashing should verify the right password, reject the wrong one, and never store plaintext. */
class PasswordHasherTest {

    @Test
    void matchesTheCorrectPassword() {
        String hash = PasswordHasher.hash("Secret@123");
        assertTrue(PasswordHasher.matches("Secret@123", hash));
    }

    @Test
    void rejectsTheWrongPassword() {
        String hash = PasswordHasher.hash("Secret@123");
        assertFalse(PasswordHasher.matches("secret@123", hash));
        assertFalse(PasswordHasher.matches("something-else", hash));
    }

    @Test
    void hashIsNotThePlainPassword() {
        String hash = PasswordHasher.hash("Secret@123");
        assertNotEquals("Secret@123", hash);
        assertTrue(hash.startsWith("$2a$"), "should be a bcrypt hash");
    }

    @Test
    void samePasswordHashesDifferentlyEachTime() {
        // bcrypt uses a random salt, so two hashes of the same password should differ
        assertNotEquals(PasswordHasher.hash("Secret@123"), PasswordHasher.hash("Secret@123"));
    }

    @Test
    void handlesGarbageHashWithoutCrashing() {
        assertFalse(PasswordHasher.matches("anything", "not-a-real-hash"));
    }
}
