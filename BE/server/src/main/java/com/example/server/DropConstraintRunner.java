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

        // Tự động chuẩn hóa tên ca làm việc có dấu trong CSDL
        try {
            jdbcTemplate.execute("UPDATE dbo.ca_lam SET ten = N'Ca chiều' WHERE id = 'chieu' OR ten LIKE '%chi?u%' OR ten LIKE '%chi_u%' OR ten = 'Ca chieu'");
            jdbcTemplate.execute("UPDATE dbo.ca_lam SET ten = N'Ca tối' WHERE id = 'toi' OR ten LIKE '%t?i%' OR ten LIKE '%t_i%' OR ten = 'Ca toi'");
            jdbcTemplate.execute("UPDATE dbo.ca_lam SET ten = N'Ca sáng' WHERE id = 'sang' OR ten LIKE '%s?ng%' OR ten LIKE '%s_ng%' OR ten = 'Ca sang'");
            System.out.println("Successfully standardized shift names in ca_lam table.");
        } catch (Exception e) {
            System.out.println("Could not update ca_lam names: " + e.getMessage());
        }

        // Tự động xóa khách hàng trùng lặp số 2 (Trần Vũ Tùng Anh không có ảnh đại diện)
        try {
            jdbcTemplate.execute("""
                DELETE FROM dbo.dia_chi_khach_hang WHERE khach_hang_id IN (
                    SELECT id FROM dbo.khach_hang 
                    WHERE (email LIKE 'tunganht26%' OR sdt = '0383854485' OR ho_ten LIKE N'%Trần Vũ Tùng%')
                      AND (hinh_anh IS NULL OR hinh_anh = '')
                );
                DELETE FROM dbo.phieu_giam_gia_khach_hang WHERE khach_hang_id IN (
                    SELECT id FROM dbo.khach_hang 
                    WHERE (email LIKE 'tunganht26%' OR sdt = '0383854485' OR ho_ten LIKE N'%Trần Vũ Tùng%')
                      AND (hinh_anh IS NULL OR hinh_anh = '')
                );
                DELETE FROM dbo.tai_khoan_ngan_hang WHERE khach_hang_id IN (
                    SELECT id FROM dbo.khach_hang 
                    WHERE (email LIKE 'tunganht26%' OR sdt = '0383854485' OR ho_ten LIKE N'%Trần Vũ Tùng%')
                      AND (hinh_anh IS NULL OR hinh_anh = '')
                );
                DELETE FROM dbo.thong_bao_khach_hang WHERE khach_hang_id IN (
                    SELECT id FROM dbo.khach_hang 
                    WHERE (email LIKE 'tunganht26%' OR sdt = '0383854485' OR ho_ten LIKE N'%Trần Vũ Tùng%')
                      AND (hinh_anh IS NULL OR hinh_anh = '')
                );
                DELETE FROM dbo.gio_hang_chi_tiet WHERE gio_hang_id IN (
                    SELECT id FROM dbo.gio_hang WHERE id_khach_hang IN (
                        SELECT id FROM dbo.khach_hang 
                        WHERE (email LIKE 'tunganht26%' OR sdt = '0383854485' OR ho_ten LIKE N'%Trần Vũ Tùng%')
                          AND (hinh_anh IS NULL OR hinh_anh = '')
                    )
                );
                DELETE FROM dbo.gio_hang WHERE id_khach_hang IN (
                    SELECT id FROM dbo.khach_hang 
                    WHERE (email LIKE 'tunganht26%' OR sdt = '0383854485' OR ho_ten LIKE N'%Trần Vũ Tùng%')
                      AND (hinh_anh IS NULL OR hinh_anh = '')
                );
                UPDATE dbo.hoa_don SET id_khach_hang = NULL WHERE id_khach_hang IN (
                    SELECT id FROM dbo.khach_hang 
                    WHERE (email LIKE 'tunganht26%' OR sdt = '0383854485' OR ho_ten LIKE N'%Trần Vũ Tùng%')
                      AND (hinh_anh IS NULL OR hinh_anh = '')
                );
                DELETE FROM dbo.khach_hang 
                WHERE (email LIKE 'tunganht26%' OR sdt = '0383854485' OR ho_ten LIKE N'%Trần Vũ Tùng%')
                  AND (hinh_anh IS NULL OR hinh_anh = '');
            """);
            System.out.println("Successfully removed duplicate customer #2 without avatar.");
        } catch (Exception e) {
            System.out.println("Could not remove duplicate customer: " + e.getMessage());
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
