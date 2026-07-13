package com.example.server.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'van_chuyen' AND COLUMN_NAME = 'ly_do_giao_hang_that_bai'",
                Integer.class
            );
            if (count == null || count == 0) {
                System.out.println("--- MIGRATION: Adding column ly_do_giao_hang_that_bai to van_chuyen table ---");
                jdbcTemplate.execute("ALTER TABLE van_chuyen ADD ly_do_giao_hang_that_bai NVARCHAR(500) NULL");
                System.out.println("--- MIGRATION: Column added successfully ---");
            }
        } catch (Exception e) {
            System.err.println("--- MIGRATION ERROR: " + e.getMessage());
        }
    }
}
