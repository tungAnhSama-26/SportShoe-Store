package com.example.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrator implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Check if column exists
            String checkSql = "SELECT COUNT(*) FROM sys.columns WHERE Name = N'face_descriptor' AND Object_ID = Object_ID(N'nhan_vien')";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                System.out.println("====== Thêm cột face_descriptor vào bảng nhan_vien ======");
                jdbcTemplate.execute("ALTER TABLE nhan_vien ADD face_descriptor NVARCHAR(MAX)");
                System.out.println("====== Hoàn tất thêm cột ======");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm cột: " + e.getMessage());
        }
    }
}
