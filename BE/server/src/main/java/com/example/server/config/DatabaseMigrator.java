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
            System.err.println("Lỗi khi thêm cột nhan_vien: " + e.getMessage());
        }

        try {
            // Tạo bảng cuoc_hoi_thoai nếu chưa có
            jdbcTemplate.execute("""
                IF OBJECT_ID(N'cuoc_hoi_thoai', N'U') IS NULL
                BEGIN
                    CREATE TABLE cuoc_hoi_thoai (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        ten_khach_hang NVARCHAR(250),
                        so_dien_thoai VARCHAR(20),
                        trang_thai INT DEFAULT 1,
                        ngay_tao DATETIME2 DEFAULT SYSDATETIME(),
                        ngay_cap_nhat DATETIME2
                    );
                END
            """);

            // Tạo bảng tin_nhan nếu chưa có
            jdbcTemplate.execute("""
                IF OBJECT_ID(N'tin_nhan', N'U') IS NULL
                BEGIN
                    CREATE TABLE tin_nhan (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        cuoc_hoi_thoai_id INT FOREIGN KEY REFERENCES cuoc_hoi_thoai(id) ON DELETE CASCADE,
                        nguoi_gui NVARCHAR(50),
                        noi_dung NVARCHAR(MAX),
                        ngay_tao DATETIME2 DEFAULT SYSDATETIME()
                    );
                END
                ELSE
                BEGIN
                    -- Đổi kiểu cột noi_dung sang NVARCHAR(MAX) nếu nó là varchar/text/char/ntext
                    DECLARE @col_type NVARCHAR(50);
                    SELECT @col_type = DATA_TYPE
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_NAME = 'tin_nhan' AND COLUMN_NAME = 'noi_dung';

                    IF @col_type IN ('text', 'varchar', 'char', 'ntext')
                    BEGIN
                        -- Xóa dữ liệu lỗi cũ để tránh lỗi Msg 515 khi chuyển đổi cột sang NOT NULL
                        DELETE FROM tin_nhan;
                        -- Tiến hành chuyển đổi kiểu cột
                        ALTER TABLE tin_nhan ALTER COLUMN noi_dung NVARCHAR(MAX) NOT NULL;
                    END
                END
            """);
        } catch (Exception e) {
            System.err.println("Lỗi khi di cư bảng Chatbot: " + e.getMessage());
        }

    }
}

