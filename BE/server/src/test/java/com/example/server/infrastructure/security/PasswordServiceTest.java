package com.example.server.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void matchesLegacyPlainTextPasswordAndMarksItForRehash() {
        assertTrue(passwordService.matches("111111", "111111"));
        assertTrue(passwordService.needsRehash("111111"));
    }

    @Test
    void hashesAndMatchesBcryptPassword() {
        String hash = passwordService.hash("111111");

        assertNotEquals("111111", hash);
        assertTrue(passwordService.isHashed(hash));
        assertTrue(passwordService.matches("111111", hash));
        assertFalse(passwordService.matches("222222", hash));
        assertFalse(passwordService.needsRehash(hash));
    }
}
