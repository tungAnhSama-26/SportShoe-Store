USE giay;
GO

SET NOCOUNT ON;
GO

-- Chuyển dữ liệu hóa đơn cũ khỏi trạng thái 8.
-- Trạng thái cần hoàn tiền tiếp tục được quản lý tại thanh_toan.trang_thai = 4.
UPDATE hd
SET hd.trang_thai =
    CASE
        WHEN EXISTS (
            SELECT 1
            FROM dbo.van_chuyen vc
            WHERE vc.hoa_don_id = hd.id
              AND vc.trang_thai = 4
        ) THEN 10
        WHEN EXISTS (
            SELECT 1
            FROM dbo.phieu_tra_hang pth
            WHERE pth.hoa_don_id = hd.id
        ) THEN 5
        ELSE 6
    END,
    hd.ngay_cap_nhat = SYSDATETIME()
FROM dbo.hoa_don hd
WHERE hd.trang_thai = 8;
GO

IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'ck_hoa_don_trang_thai'
      AND parent_object_id = OBJECT_ID(N'dbo.hoa_don')
)
BEGIN
    ALTER TABLE dbo.hoa_don DROP CONSTRAINT ck_hoa_don_trang_thai;
END;
GO

ALTER TABLE dbo.hoa_don WITH CHECK
ADD CONSTRAINT ck_hoa_don_trang_thai
CHECK (trang_thai IN (0, 1, 2, 3, 4, 5, 6, 7, 9, 10));
GO

ALTER TABLE dbo.hoa_don CHECK CONSTRAINT ck_hoa_don_trang_thai;
GO
