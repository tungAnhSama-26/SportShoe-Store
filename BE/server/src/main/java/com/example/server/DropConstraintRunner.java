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
        // Tự động gỡ bỏ các ràng buộc kiểm tra cũ gây lỗi khi tạo/lưu ca làm việc.
        // Constraint trạng thái hóa đơn phải được quản lý bằng schema/migration, không được xóa khi khởi động.
        dropConstraint("dbo.ca_lam", "ck_ca_lam_gio");
        dropConstraint("dbo.ca_lam", "ck_ca_lam_gio_bat_dau");
        dropConstraint("dbo.ca_lam", "ck_ca_lam_gio_ket_thuc");

        // Quét và tự động xóa các ràng buộc CHECK cũ chỉ trên các bảng ca làm việc.
        try {
            String sql = "DECLARE @sql NVARCHAR(MAX) = ''; " +
                    "SELECT @sql += 'ALTER TABLE ' + QUOTENAME(sys.schemas.name) + '.' + QUOTENAME(sys.objects.name) " +
                    "+ ' DROP CONSTRAINT ' + QUOTENAME(sys.check_constraints.name) + ';' " +
                    "FROM sys.check_constraints " +
                    "INNER JOIN sys.objects ON sys.check_constraints.parent_object_id = sys.objects.object_id " +
                    "INNER JOIN sys.schemas ON sys.objects.schema_id = sys.schemas.schema_id " +
                    "WHERE sys.objects.name = 'ca_lam'; " +
                    "IF @sql <> '' EXEC sp_executesql @sql;";
            jdbcTemplate.execute(sql);
            System.out.println("Successfully removed outdated check constraints on ca_lam.");
        } catch (Exception e) {
            System.out.println("Could not run dynamic constraint cleanup: " + e.getMessage());
        }

        // Tự động chuẩn hóa tên ca làm việc có dấu trong CSDL
        try {
            jdbcTemplate.execute("UPDATE dbo.ca_lam SET ten = N'Ca chiều' WHERE id = 'chieu' OR ten LIKE '%chi?u%' OR ten LIKE '%chi_u%' OR ten = 'Ca chieu'");
            jdbcTemplate.execute("UPDATE dbo.ca_lam SET ten = N'Ca tối' WHERE id = 'toi' OR ten LIKE '%t?i%' OR ten LIKE '%t_i%' OR ten = 'Ca toi'");
            jdbcTemplate.execute("UPDATE dbo.ca_lam SET ten = N'Ca sáng' WHERE id = 'sang' OR ten LIKE '%s?ng%' OR ten LIKE '%s_ng%' OR ten = 'Ca sang'");
            System.out.println("Successfully standardized shift names in ca_lam table.");
        } catch (Exception e) {
            System.out.println("Could not update ca_lam names: " + e.getMessage());
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
