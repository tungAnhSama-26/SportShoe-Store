package com.example.server.bugfix;

import com.example.server.entity.KhachHang;
import com.example.server.repository.KhachHangRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition Exploration Test for Email Lookup Case-Sensitivity
 * 
 * **Validates: Requirements 1.1, 1.2, 1.4**
 * 
 * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists.
 * DO NOT attempt to fix the test or the code when it fails.
 * 
 * NOTE: This test encodes the expected behavior - it will validate the fix when it passes after implementation.
 * 
 * GOAL: Surface counterexamples that demonstrate the bug exists.
 * 
 * Bug Description:
 * The service layer normalizes email to lowercase before calling repository.findByEmail(),
 * but the repository method uses case-sensitive matching. This causes email lookup to fail
 * when the input email case differs from the database email case.
 * 
 * Expected Behavior (from Property 1 in design):
 * For any email input where the email exists in database (case-insensitive),
 * the findByEmail() method SHALL find the customer record regardless of the case
 * of the input email, enabling successful voucher creation and update operations.
 */
@SpringBootTest
class EmailLookupCaseSensitivityBugTest {

    @Autowired
    private KhachHangRepository khachHangRepository;

    private KhachHang testCustomer1;
    private KhachHang testCustomer2;
    private KhachHang testCustomer3;

    @BeforeEach
    void setUp() {
        // Note: We don't delete all data due to foreign key constraints
        // Tests will create unique customers with unique usernames
    }

    @AfterEach
    void tearDown() {
        // Clean up only the test customers we created
        if (testCustomer1 != null && testCustomer1.getId() != null) {
            try {
                khachHangRepository.deleteById(testCustomer1.getId());
            } catch (Exception e) {
                // Ignore if already deleted or has foreign key constraints
            }
        }
        if (testCustomer2 != null && testCustomer2.getId() != null) {
            try {
                khachHangRepository.deleteById(testCustomer2.getId());
            } catch (Exception e) {
                // Ignore if already deleted or has foreign key constraints
            }
        }
        if (testCustomer3 != null && testCustomer3.getId() != null) {
            try {
                khachHangRepository.deleteById(testCustomer3.getId());
            } catch (Exception e) {
                // Ignore if already deleted or has foreign key constraints
            }
        }
    }

    /**
     * Test Case 1: Uppercase Email Lookup
     * 
     * Create customer with email "Test@Example.Com" (mixed case in DB), lookup with "TEST@EXAMPLE.COM"
     * 
     * EXPECTED ON UNFIXED CODE: NOT FOUND (bug - this test will FAIL)
     * EXPECTED ON FIXED CODE: FOUND (test will PASS)
     */
    @Test
    void testUppercaseEmailLookup_shouldFindCustomer() {
        // Arrange: Create customer with MIXED CASE email in database
        testCustomer1 = createTestCustomer("testuser1", "Test User 1", "Test@Example.Com");
        testCustomer1 = khachHangRepository.save(testCustomer1);
        String savedEmail = testCustomer1.getEmail(); // Get the actual saved email (with timestamp)

        // Act: Lookup with lowercase version of the saved email
        String normalizedEmail = savedEmail.toLowerCase();
        Optional<KhachHang> result = khachHangRepository.findByEmail(normalizedEmail);

        // Assert: Should find the customer (Expected Behavior from Property 1)
        assertTrue(result.isPresent(), 
            String.format("Bug detected: Customer with email '%s' should be found when searching with '%s' (normalized to lowercase)", 
                savedEmail, normalizedEmail));
        assertEquals(testCustomer1.getId(), result.get().getId(),
            "Found customer should match the created customer");
    }

    /**
     * Test Case 2: Mixed Case Email Lookup
     * 
     * Create customer with email "User@Gmail.Com" (mixed case in DB), lookup with "user@gmail.com"
     * 
     * EXPECTED ON UNFIXED CODE: NOT FOUND (bug - this test will FAIL)
     * EXPECTED ON FIXED CODE: FOUND (test will PASS)
     */
    @Test
    void testMixedCaseEmailLookup_shouldFindCustomer() {
        // Arrange: Create customer with MIXED CASE email in database
        testCustomer2 = createTestCustomer("testuser2", "Test User 2", "User@Gmail.Com");
        testCustomer2 = khachHangRepository.save(testCustomer2);
        String savedEmail = testCustomer2.getEmail(); // Get the actual saved email (with timestamp)

        // Act: Lookup with lowercase version of the saved email
        String normalizedEmail = savedEmail.toLowerCase();
        Optional<KhachHang> result = khachHangRepository.findByEmail(normalizedEmail);

        // Assert: Should find the customer (Expected Behavior from Property 1)
        assertTrue(result.isPresent(),
            String.format("Bug detected: Customer with email '%s' should be found when searching with '%s' (normalized to lowercase)", 
                savedEmail, normalizedEmail));
        assertEquals(testCustomer2.getId(), result.get().getId(),
            "Found customer should match the created customer");
    }

    /**
     * Test Case 3: Update Voucher with Different Case Email
     * 
     * Create customer with email "ADMIN@TEST.COM" (uppercase in DB), lookup with "admin@test.com"
     * 
     * EXPECTED ON UNFIXED CODE: NOT FOUND (bug - this test will FAIL)
     * EXPECTED ON FIXED CODE: FOUND (test will PASS)
     */
    @Test
    void testUpdateVoucherWithDifferentCaseEmail_shouldFindCustomer() {
        // Arrange: Create customer with UPPERCASE email in database
        testCustomer3 = createTestCustomer("adminuser", "Admin User", "ADMIN@TEST.COM");
        testCustomer3 = khachHangRepository.save(testCustomer3);
        String savedEmail = testCustomer3.getEmail(); // Get the actual saved email (with timestamp)

        // Act: Lookup with lowercase version of the saved email
        String normalizedEmail = savedEmail.toLowerCase();
        Optional<KhachHang> result = khachHangRepository.findByEmail(normalizedEmail);

        // Assert: Should find the customer (Expected Behavior from Property 1)
        assertTrue(result.isPresent(),
            String.format("Bug detected: Customer with email '%s' should be found when searching with '%s' (normalized to lowercase)", 
                savedEmail, normalizedEmail));
        assertEquals(testCustomer3.getId(), result.get().getId(),
            "Found customer should match the created customer");
    }

    /**
     * Test Case 4: Baseline - Lowercase Email Lookup (Should Pass on Unfixed Code)
     * 
     * Create customer with email "lower@case.com", lookup with "lower@case.com"
     * 
     * EXPECTED ON UNFIXED CODE: FOUND (baseline - this test should PASS)
     * EXPECTED ON FIXED CODE: FOUND (test should still PASS)
     * 
     * This test confirms that the current implementation works when email cases match.
     */
    @Test
    void testLowercaseEmailLookup_shouldFindCustomer_baseline() {
        // Arrange: Create customer with lowercase email
        KhachHang baselineCustomer = createTestCustomer("baseline", "Baseline User", "lower@case.com");
        baselineCustomer = khachHangRepository.save(baselineCustomer);
        String savedEmail = baselineCustomer.getEmail(); // Get the actual saved email (with timestamp)

        // Act: Lookup with same lowercase email
        String normalizedEmail = savedEmail.toLowerCase();
        Optional<KhachHang> result = khachHangRepository.findByEmail(normalizedEmail);

        // Assert: Should find the customer (baseline behavior)
        assertTrue(result.isPresent(),
            String.format("Baseline: Customer with email '%s' should be found when searching with '%s'", 
                savedEmail, normalizedEmail));
        assertEquals(baselineCustomer.getId(), result.get().getId(),
            "Found customer should match the created customer");
    }

    /**
     * Helper method to create a test customer
     */
    private KhachHang createTestCustomer(String tenDangNhap, String hoTen, String email) {
        KhachHang customer = new KhachHang();
        customer.setId(UUID.randomUUID());
        // Make username unique by appending timestamp
        customer.setTenDangNhap(tenDangNhap + "_" + System.currentTimeMillis());
        customer.setHoTen(hoTen);
        // Make email unique by appending timestamp before @
        String[] emailParts = email.split("@");
        if (emailParts.length == 2) {
            customer.setEmail(emailParts[0] + "_" + System.currentTimeMillis() + "@" + emailParts[1]);
        } else {
            customer.setEmail(email + "_" + System.currentTimeMillis());
        }
        customer.setSdt("0123456789");
        customer.setMatKhau("password123");
        customer.setTrangThai(1);
        customer.setNgayTao(Instant.now());
        return customer;
    }
}
