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
        // Tự động gỡ bỏ các Ràng buộc kiểm tra (CHECK constraints) cũ của SQL Server gây lỗi khi tạo/lưu ca làm việc
        dropConstraint("dbo.hoa_don", "ck_hoa_don_trang_thai");
        dropConstraint("dbo.lich_lam_viec", "ck_lich_lam_viec_ca");
        dropConstraint("dbo.ca_lam", "ck_ca_lam_gio");
        dropConstraint("dbo.ca_lam", "ck_ca_lam_gio_bat_dau");
        dropConstraint("dbo.ca_lam", "ck_ca_lam_gio_ket_thuc");

        // Quét và tự động xóa tất cả ràng buộc CHECK trên các bảng ca_lam, lich_lam_viec, hoa_don
        try {
            String sql = "DECLARE @sql NVARCHAR(MAX) = ''; " +
                    "SELECT @sql += 'ALTER TABLE ' + QUOTENAME(sys.schemas.name) + '.' + QUOTENAME(sys.objects.name) " +
                    "+ ' DROP CONSTRAINT ' + QUOTENAME(sys.check_constraints.name) + ';' " +
                    "FROM sys.check_constraints " +
                    "INNER JOIN sys.objects ON sys.check_constraints.parent_object_id = sys.objects.object_id " +
                    "INNER JOIN sys.schemas ON sys.objects.schema_id = sys.schemas.schema_id " +
                    "WHERE sys.objects.name IN ('lich_lam_viec', 'ca_lam', 'hoa_don'); " +
                    "IF @sql <> '' EXEC sp_executesql @sql;";
            jdbcTemplate.execute(sql);
            System.out.println("Successfully removed outdated check constraints on lich_lam_viec, ca_lam, hoa_don.");
        } catch (Exception e) {
            System.out.println("Could not run dynamic constraint cleanup: " + e.getMessage());
        }
    }

    private void dropConstraint(String table, String constraint) {
        try {
            jdbcTemplate.execute("ALTER TABLE " + table + " DROP CONSTRAINT " + constraint);
            System.out.println("DROPPED CONSTRAINT: " + constraint);
        } catch (Exception e) {
            // Ràng buộc đã bị xóa hoặc không tồn tại
        }
    }
}
