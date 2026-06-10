-- ============================================================
-- Thêm cột ảnh đại diện cho bảng giay (ảnh ở CẤP SẢN PHẨM).
-- Dùng để hiển thị ở trang danh sách và làm ảnh mặc định ở trang chi tiết
-- (khi khách chưa chọn biến thể/size).
-- DB: SQL Server. Chạy thủ công vì spring.jpa.hibernate.ddl-auto=none.
-- Script idempotent.
-- ============================================================

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'giay' AND COLUMN_NAME = 'hinh_anh'
)
BEGIN
    ALTER TABLE giay ADD hinh_anh NVARCHAR(500) NULL;
END;
GO

-- Điền ảnh đại diện cho các sản phẩm chưa có, lấy ảnh chính của biến thể đầu tiên.
UPDATE g
SET g.hinh_anh = sub.url
FROM giay g
CROSS APPLY (
    SELECT TOP 1 ha.url
    FROM hinh_anh_giay ha
    JOIN giay_chi_tiet gct ON gct.id = ha.giay_chi_tiet_id
    WHERE gct.giay_id = g.id AND ha.trang_thai = 1
    ORDER BY ha.la_hinh_chinh DESC, ha.id ASC
) sub
WHERE g.hinh_anh IS NULL;
GO
