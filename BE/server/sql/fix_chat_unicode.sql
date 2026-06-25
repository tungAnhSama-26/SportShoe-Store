-- ============================================================
-- FIX: Chuyển cột noi_dung sang NVARCHAR(MAX) để hỗ trợ
--       đầy đủ ký tự Unicode tiếng Việt trong SQL Server
-- Chạy script này trên database đang dùng (sportshoe / giay)
-- ============================================================

-- Tạo bảng cuoc_hoi_thoai nếu chưa có
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'cuoc_hoi_thoai')
BEGIN
    CREATE TABLE cuoc_hoi_thoai (
        id             INT          NOT NULL PRIMARY KEY IDENTITY(1,1),
        khach_hang_id  INT          NULL,
        nhan_vien_id   INT          NULL,
        ten_khach_hang NVARCHAR(250) NULL,
        so_dien_thoai  NVARCHAR(20)  NULL,
        trang_thai     INT           NOT NULL DEFAULT 1,
        ngay_tao       DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
        ngay_cap_nhat  DATETIME2     NULL,
        CONSTRAINT fk_cuoc_hoi_thoai_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id),
        CONSTRAINT fk_cuoc_hoi_thoai_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id)
    );
    PRINT 'Da tao bang cuoc_hoi_thoai';
END
ELSE
BEGIN
    -- Nếu bảng đã tồn tại, kiểm tra và thêm cột khach_hang_id và nhan_vien_id nếu chưa có
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'cuoc_hoi_thoai' AND COLUMN_NAME = 'khach_hang_id')
    BEGIN
        ALTER TABLE cuoc_hoi_thoai ADD khach_hang_id INT NULL;
        ALTER TABLE cuoc_hoi_thoai ADD CONSTRAINT fk_cuoc_hoi_thoai_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id);
        PRINT 'Da them cot khach_hang_id vao cuoc_hoi_thoai';
    END
    
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'cuoc_hoi_thoai' AND COLUMN_NAME = 'nhan_vien_id')
    BEGIN
        ALTER TABLE cuoc_hoi_thoai ADD nhan_vien_id INT NULL;
        ALTER TABLE cuoc_hoi_thoai ADD CONSTRAINT fk_cuoc_hoi_thoai_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id);
        PRINT 'Da them cot nhan_vien_id vao cuoc_hoi_thoai';
    END
END
GO

-- Tạo bảng tin_nhan nếu chưa có (với NVARCHAR(MAX) ngay từ đầu)
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tin_nhan')
BEGIN
    CREATE TABLE tin_nhan (
        id                 INT               NOT NULL PRIMARY KEY IDENTITY(1,1),
        cuoc_hoi_thoai_id  INT               NOT NULL,
        nhan_vien_id       UNIQUEIDENTIFIER  NULL,
        nguoi_gui          NVARCHAR(50)      NOT NULL,
        noi_dung           NVARCHAR(MAX)     NOT NULL,
        ngay_tao           DATETIME2         NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT fk_tin_nhan_cuoc_hoi_thoai
            FOREIGN KEY (cuoc_hoi_thoai_id) REFERENCES cuoc_hoi_thoai(id) ON DELETE CASCADE,
        CONSTRAINT fk_tin_nhan_nhan_vien
            FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id)
    );
    PRINT 'Da tao bang tin_nhan voi NVARCHAR(MAX)';
END
ELSE
BEGIN
    -- Nếu bảng đã tồn tại, kiểm tra và thêm cột nhan_vien_id nếu chưa có
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tin_nhan' AND COLUMN_NAME = 'nhan_vien_id')
    BEGIN
        ALTER TABLE tin_nhan ADD nhan_vien_id UNIQUEIDENTIFIER NULL;
        ALTER TABLE tin_nhan ADD CONSTRAINT fk_tin_nhan_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id);
        PRINT 'Da them cot nhan_vien_id vao tin_nhan';
    END

    -- Bảng đã tồn tại → kiểm tra và sửa kiểu cột noi_dung
    DECLARE @col_type NVARCHAR(50);
    SELECT @col_type = DATA_TYPE
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'tin_nhan' AND COLUMN_NAME = 'noi_dung';

    IF @col_type IN ('text', 'varchar', 'char', 'ntext')
    BEGIN
        -- Đổi sang NVARCHAR(MAX) để hỗ trợ Unicode đầy đủ
        ALTER TABLE tin_nhan
            ALTER COLUMN noi_dung NVARCHAR(MAX) NOT NULL;
        PRINT 'Da doi cot noi_dung tu ' + @col_type + ' sang NVARCHAR(MAX)';
    END
    ELSE
    BEGIN
        PRINT 'Cot noi_dung da la: ' + ISNULL(@col_type, 'unknown') + ' - khong can sua';
    END
END
GO

-- Kiểm tra kết quả
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME IN ('tin_nhan', 'cuoc_hoi_thoai')
ORDER BY TABLE_NAME, ORDINAL_POSITION;
GO
