package com.example.server.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        if (isHashed(storedPassword)) {
            try {
                return passwordEncoder.matches(rawPassword, storedPassword);
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }
        return MessageDigest.isEqual(
                rawPassword.getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8)
        );
    }

    public boolean needsRehash(String storedPassword) {
        return !isHashed(storedPassword);
    }

    public boolean isHashed(String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")
                || storedPassword.startsWith("{bcrypt}");
    }
}
