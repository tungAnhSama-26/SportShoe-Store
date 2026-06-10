-- ============================================================
-- Thêm cột han_giu_hang cho hóa đơn (giữ hàng tạm thời khi khách vào thanh toán).
-- Khi bấm thanh toán: trừ tồn tạm + đặt hạn giữ = now + 90 giây.
-- Hết hạn / khách thoát: hoàn tồn + xóa hạn giữ.
-- DB: SQL Server. Script idempotent.
-- ============================================================

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'hoa_don' AND COLUMN_NAME = 'han_giu_hang'
)
BEGIN
    ALTER TABLE hoa_don ADD han_giu_hang DATETIME2 NULL;
END;
GO
