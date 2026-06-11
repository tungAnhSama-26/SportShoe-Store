-- ============================================================
-- Cho phép trạng thái hóa đơn = 0 (GIỎ HÀNG đang mở của khách online).
-- Giỏ hàng online được lưu dưới dạng hóa đơn kenh_ban=2, trang_thai=0.
-- DB: SQL Server. Script idempotent.
-- ============================================================

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'ck_hoa_don_trang_thai')
    ALTER TABLE hoa_don DROP CONSTRAINT ck_hoa_don_trang_thai;

ALTER TABLE hoa_don ADD CONSTRAINT ck_hoa_don_trang_thai
    CHECK (trang_thai IN (0, 1, 2, 3, 4, 5, 6, 7, 9, 10));
