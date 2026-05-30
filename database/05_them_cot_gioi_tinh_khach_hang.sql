-- =============================================================
-- Migration: Thêm cột gioi_tinh vào bảng khach_hang
-- Quy ước: 0 = Nữ, 1 = Nam, 2 = Khác (NULL = chưa cập nhật)
-- Script an toàn để chạy lại nhiều lần (idempotent).
-- =============================================================

-- 1) Thêm cột gioi_tinh nếu chưa tồn tại
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.khach_hang') AND name = N'gioi_tinh'
)
BEGIN
    ALTER TABLE khach_hang ADD gioi_tinh INT NULL;
END
GO

-- 2) Thêm ràng buộc CHECK nếu chưa tồn tại
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'ck_khach_hang_gioi_tinh'
)
BEGIN
    ALTER TABLE khach_hang
        ADD CONSTRAINT ck_khach_hang_gioi_tinh
        CHECK (gioi_tinh IS NULL OR gioi_tinh IN (0, 1, 2));
END
GO
