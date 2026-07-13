package com.example.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DropConstraintRunner implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public DropConstraintRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE dbo.hoa_don DROP CONSTRAINT ck_hoa_don_trang_thai");
            System.out.println("DROPPED CONSTRAINT ck_hoa_don_trang_thai");
        } catch (Exception e) {
            System.out.println("COULD NOT DROP CONSTRAINT: " + e.getMessage());
        }
    }
}
