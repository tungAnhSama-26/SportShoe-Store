-- Xóa constraint kiểm tra trạng thái cũ
ALTER TABLE phieu_giam_gia DROP CONSTRAINT IF EXISTS ck_pgg_trang_thai;
GO

-- Thêm lại constraint mới cho phép các trạng thái 0, 1, 2, 3, 4
ALTER TABLE phieu_giam_gia ADD CONSTRAINT ck_pgg_trang_thai CHECK (trang_thai IN (0, 1, 2, 3, 4));
GO

INSERT INTO phieu_giam_gia 
(ma, ten, loai, loai_phieu, gia_tri, gia_tri_toi_thieu, giam_toi_da, ngay_bat_dau, ngay_ket_thuc, so_luong, so_luong_da_dung, trang_thai, ngay_tao)
VALUES 
-- 1. Hoạt động
('PGG009', N'Giảm giá 10%', 1, 1, 10.00, 100000.00, 50000.00, DATEADD(day, -1, GETDATE()), DATEADD(day, 5, GETDATE()), 100, 10, 1, GETDATE()),

-- 2. Hết hạn
('PGG010', N'Giảm giá 15%', 1, 1, 15.00, 200000.00, 50000.00, DATEADD(day, -10, GETDATE()), DATEADD(day, -1, GETDATE()), 100, 20, 2, GETDATE()),

-- 3. Hết số lượng
('PGG011', N'Giảm giá 20%', 1, 1, 20.00, 150000.00, 60000.00, DATEADD(day, -5, GETDATE()), DATEADD(day, 5, GETDATE()), 50, 50, 3, GETDATE()),

-- 4. Sắp diễn ra
('PGG012', N'Giảm giá 25%', 1, 1, 25.00, 500000.00, 100000.00, DATEADD(day, 2, GETDATE()), DATEADD(day, 10, GETDATE()), 200, 0, 4, GETDATE()),

-- 5. Ngừng hoạt động
('PGG013', N'Giảm giá 30%', 1, 1, 30.00, 200000.00, 100000.00, DATEADD(day, -2, GETDATE()), DATEADD(day, 8, GETDATE()), 100, 5, 0, GETDATE());