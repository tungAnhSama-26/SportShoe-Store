-- ============================================================
-- Gỡ bỏ bảng giỏ hàng riêng (gio_hang, gio_hang_chi_tiet).
-- Giỏ hàng nay là một HÓA ĐƠN đang mở (kenh_ban = 2, trang_thai = 0),
-- nên không cần bảng giỏ riêng nữa.
-- DB: SQL Server. Script idempotent.
-- ============================================================

IF EXISTS (SELECT 1 FROM sys.tables WHERE name = 'gio_hang_chi_tiet')
    DROP TABLE gio_hang_chi_tiet;

IF EXISTS (SELECT 1 FROM sys.tables WHERE name = 'gio_hang')
    DROP TABLE gio_hang;
