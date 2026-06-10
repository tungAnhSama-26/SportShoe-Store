-- ============================================================
-- Bổ sung cho luồng "đã nhận hàng" + đánh giá theo hóa đơn chi tiết.
-- DB: SQL Server. Script idempotent.
-- ============================================================

-- 1. Cờ khách đã xác nhận nhận hàng (trên hóa đơn).
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'hoa_don' AND COLUMN_NAME = 'da_nhan_hang'
)
BEGIN
    ALTER TABLE hoa_don ADD da_nhan_hang BIT NOT NULL CONSTRAINT df_hoa_don_da_nhan_hang DEFAULT 0;
END;
GO

-- 2. Liên kết đánh giá với dòng hóa đơn chi tiết (để mỗi lần mua chỉ đánh giá 1 lần).
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'danh_gia' AND COLUMN_NAME = 'hoa_don_chi_tiet_id'
)
BEGIN
    ALTER TABLE danh_gia ADD hoa_don_chi_tiet_id INT NULL;
    ALTER TABLE danh_gia ADD CONSTRAINT fk_danhgia_hoadonchitiet
        FOREIGN KEY (hoa_don_chi_tiet_id) REFERENCES hoa_don_chi_tiet(id);
END;
GO
