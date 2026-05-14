-- ============================================================
-- Insert hóa đơn ONLINE, trạng thái CHỜ XÁC NHẬN (trang_thai = 1, kenh_ban = 2)
-- ============================================================

INSERT INTO hoa_don (ma, kenh_ban, khach_hang_id, nhan_vien_id, phieu_giam_gia_id, ten_nguoi_nhan, sdt_nguoi_nhan, dia_chi_giao_hang, trang_thai, tong_tien_hang, tien_giam, tong_tien_thanh_toan, ghi_chu)
VALUES
(
    'HD-CXN-001', 2,
    (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach1'),
    NULL, NULL,
    N'Nguyễn Thị Lan', '0911111111', N'12 Hàng Đào, Hà Nội',
    1, 2290000, 0, 2290000, N'Đơn online chờ xác nhận'
),
(
    'HD-CXN-002', 2,
    (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach2'),
    NULL, NULL,
    N'Trần Văn Hải', '0988888888', N'45 Nguyễn Huệ, HCM',
    1, 3980000, 0, 3980000, N'Đơn online chờ xác nhận'
),
(
    'HD-CXN-003', 2,
    (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach3'),
    NULL, NULL,
    N'Lê Thị Mai', '0977777777', N'78 Trần Phú, Đà Nẵng',
    1, 1590000, 0, 1590000, N'Đơn online chờ xác nhận'
),
(
    'HD-CXN-004', 2,
    (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach4'),
    NULL, NULL,
    N'Phạm Minh Quân', '0909999999', N'123 Xuân Thủy, Hà Nội',
    1, 5180000, 500000, 4680000, N'Đơn online chờ xác nhận'
),
(
    'HD-CXN-005', 2,
    (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach5'),
    NULL, NULL,
    N'Hoàng Thị Ngọc', '0933333333', N'67 Nguyễn Lương Bằng, HCM',
    1, 2980000, 0, 2980000, N'Đơn online chờ xác nhận'
);
GO

-- ============================================================
-- Insert chi tiết hóa đơn (hoa_don_chi_tiet)
-- thanh_tien = so_luong * gia_don_vi (bắt buộc theo constraint)
-- ============================================================

INSERT INTO hoa_don_chi_tiet (hoa_don_id, giay_chi_tiet_id, so_luong, gia_don_vi, thanh_tien, trang_thai)
VALUES
-- HD-CXN-001: 1 đôi giày id=1 (2290000)
((SELECT id FROM hoa_don WHERE ma = 'HD-CXN-001'), 1, 1, 2290000, 2290000, 1),

-- HD-CXN-002: 2 đôi giày id=2 (1990000 x2 = 3980000)
((SELECT id FROM hoa_don WHERE ma = 'HD-CXN-002'), 2, 2, 1990000, 3980000, 1),

-- HD-CXN-003: 1 đôi giày id=4 (1590000)
((SELECT id FROM hoa_don WHERE ma = 'HD-CXN-003'), 4, 1, 1590000, 1590000, 1),

-- HD-CXN-004: 1 đôi giày id=5 (3890000) + 1 đôi id=6 (1390000) = 5280000 -> tien_giam 500k -> 4780000
-- Lưu ý: tong_tien_hang = 3890000 + 1390000 = 5280000, nhưng đã set 5180000 ở trên
-- Sửa lại cho khớp: 1 đôi id=5 (3890000) + 1 đôi id=7 (1490000) = 5380000 -> tien_giam 500k -> 4880000
-- Dùng đơn giản: 1 đôi id=3 (2590000) + 1 đôi id=6 (1390000) = 3980000 -> tien_giam 0 -> 3980000
-- Thực tế set tong_tien_hang=5180000 nên dùng: id=5 (3890000) + id=6 (1390000) = 5280000 -> không khớp
-- Dùng: id=3 (2590000) + id=5 (3890000) = 6480000 -> không khớp
-- Đơn giản nhất: 1 đôi id=5 (3890000) + 1 đôi id=4 (1590000) = 5480000 -> không khớp
-- Fix: dùng 2 đôi id=3 (2590000 x2 = 5180000)
((SELECT id FROM hoa_don WHERE ma = 'HD-CXN-004'), 3, 2, 2590000, 5180000, 1),

-- HD-CXN-005: 1 đôi giày id=7 (1490000) + 1 đôi id=6 (1390000) = 2880000 -> không khớp 2980000
-- Fix: 2 đôi id=7 (1490000 x2 = 2980000)
((SELECT id FROM hoa_don WHERE ma = 'HD-CXN-005'), 7, 2, 1490000, 2980000, 1);
GO

-- ============================================================
-- Insert lịch sử hóa đơn
-- ============================================================

INSERT INTO lich_su_hoa_don (hoa_don_id, nhan_vien_id, trang_thai, ghi_chu)
SELECT id, NULL, N'Chờ xác nhận', N'Khách hàng đặt hàng online, chờ xác nhận'
FROM hoa_don
WHERE ma IN ('HD-CXN-001', 'HD-CXN-002', 'HD-CXN-003', 'HD-CXN-004', 'HD-CXN-005');
GO
