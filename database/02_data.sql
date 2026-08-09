SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- Dữ liệu seed chỉ dùng cho môi trường phát triển.
-- Mật khẩu ban đầu của nhân viên và khách hàng là 123456 ở dạng chưa băm.
-- AuthService sẽ tự chuyển mật khẩu sang BCrypt sau lần đăng nhập thành công đầu tiên.
DECLARE @NOW DATETIME2 = SYSDATETIME();
DECLARE @N TABLE (n INT PRIMARY KEY);
INSERT INTO @N (n) VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10);

INSERT INTO nhan_vien (
    id, ma, ten_dang_nhap, ho_ten, email, mat_khau, sdt, gioi_tinh,
    dia_chi, tinh_thanh, phuong_xa,
    vai_tro, trang_thai, bat_buoc_doi_mat_khau, ngay_tao
)
SELECT
    CONVERT(UNIQUEIDENTIFIER, CONCAT('10000000-0000-0000-0000-', RIGHT('000000000000' + CAST(n AS VARCHAR(12)), 12))),
    CONCAT('NV', RIGHT('0000' + CAST(n AS VARCHAR(4)), 4)),
    CASE WHEN n = 1 THEN 'admin' ELSE CONCAT('nhanvien', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)) END,
    CONCAT(N'Nhân viên ', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)),
    CONCAT('nhanvien', RIGHT('00' + CAST(n AS VARCHAR(2)), 2), '@sportshoe.local'),
    '123456', CONCAT('09010000', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)),
    CASE WHEN n % 2 = 0 THEN N'Nữ' ELSE N'Nam' END,
    CONCAT(N'Số ', n, N' đường Trần Thái Tông'), N'Hà Nội', N'Phường Cầu Giấy',
    CASE WHEN n = 1 THEN 1 ELSE 2 END,
    1, 0, @NOW
FROM @N;

INSERT INTO khach_hang (
    id, ten_dang_nhap, mat_khau, ho_ten, email, sdt, gioi_tinh, trang_thai, ngay_tao
)
SELECT
    CONVERT(UNIQUEIDENTIFIER, CONCAT('20000000-0000-0000-0000-', RIGHT('000000000000' + CAST(n AS VARCHAR(12)), 12))),
    CONCAT('khachhang', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), '123456',
    CONCAT(N'Khách hàng ', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)),
    CONCAT('khachhang', RIGHT('00' + CAST(n AS VARCHAR(2)), 2), '@example.com'),
    CONCAT('09020000', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), CASE WHEN n % 2 = 0 THEN 2 ELSE 1 END, 1, @NOW
FROM @N;

INSERT INTO dia_chi_khach_hang (
    khach_hang_id, ho_ten, sdt, tinh_thanh, phuong_xa,
    dia_chi_cu_the, la_mac_dinh, trang_thai, ngay_tao
)
SELECT
    CONVERT(UNIQUEIDENTIFIER, CONCAT('20000000-0000-0000-0000-', RIGHT('000000000000' + CAST(n AS VARCHAR(12)), 12))),
    CONCAT(N'Khách hàng ', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)),
    CONCAT('09020000', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), N'Hà Nội', N'Phường Cầu Giấy',
    CONCAT(N'Số ', n, N' đường Trần Thái Tông'), 1, 1, @NOW
FROM @N;

INSERT INTO tai_khoan_ngan_hang (
    khach_hang_id, ten_ngan_hang, so_tai_khoan, ten_chu_tai_khoan,
    chi_nhanh, la_mac_dinh, deleted, ngay_tao
)
SELECT
    CONVERT(UNIQUEIDENTIFIER, CONCAT('20000000-0000-0000-0000-', RIGHT('000000000000' + CAST(n AS VARCHAR(12)), 12))),
    N'Vietcombank', CONCAT('01234567', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)),
    CONCAT('KHACH HANG ', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), N'Hà Nội', 1, 0, @NOW
FROM @N;

INSERT INTO ca_lam (id, ten, gio_bat_dau, gio_ket_thuc, trang_thai) VALUES
('sang', N'Ca sáng', '08:00', '12:00', 1),
('chieu', N'Ca chiều', '13:00', '17:00', 1),
('toi', N'Ca tối', '17:30', '21:30', 1);

INSERT INTO thuong_hieu (ma, ten, xuat_xu, mo_ta, website, trang_thai, ngay_tao)
SELECT CONCAT('TH', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), CONCAT(N'Thương hiệu ', n),
       CASE WHEN n <= 5 THEN N'Việt Nam' ELSE N'Quốc tế' END,
       CONCAT(N'Thương hiệu giày thể thao số ', n), CONCAT('https://brand', n, '.example'), 1, @NOW FROM @N;

INSERT INTO loai_giay (ma, ten, mo_ta, nhom_muc_dich, nhom_phong_cach, trang_thai, ngay_tao)
SELECT CONCAT('LG', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), CONCAT(N'Loại giày ', n),
       CONCAT(N'Nhóm sản phẩm giày số ', n), CONCAT('PURPOSE_', n), CONCAT('STYLE_', n), 1, @NOW FROM @N;

INSERT INTO chat_lieu_giay (ma, ten, mo_ta, trang_thai, ngay_tao)
SELECT CONCAT('CL', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), CONCAT(N'Chất liệu ', n), CONCAT(N'Chất liệu giày số ', n), 1, @NOW FROM @N;

INSERT INTO co_giay (ma, ten, mo_ta, trang_thai, ngay_tao)
SELECT CONCAT('CG', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), CONCAT(N'Cổ giày ', n), CONCAT(N'Kiểu cổ giày số ', n), 1, @NOW FROM @N;

INSERT INTO de_giay (ma, ten, mo_ta, trang_thai, ngay_tao)
SELECT CONCAT('DG', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), CONCAT(N'Đế giày ', n), CONCAT(N'Kiểu đế giày số ', n), 1, @NOW FROM @N;

INSERT INTO cong_nghe_dem (ma, ten, mo_ta, trang_thai, ngay_tao)
SELECT CONCAT('CN', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), CONCAT(N'Công nghệ đệm ', n), CONCAT(N'Công nghệ đệm số ', n), 1, @NOW FROM @N;

INSERT INTO trong_luong (ma, gia_tri, mo_ta, trang_thai, ngay_tao)
SELECT CONCAT('TL', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), 200 + n * 20, CONCAT(N'Trọng lượng mẫu ', n), 1, @NOW FROM @N;

INSERT INTO mau_sac (ma, ten, ma_mau_hex, trang_thai, ngay_tao)
SELECT CONCAT('MS', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), CONCAT(N'Màu ', n),
       CHOOSE(n,
           '#111111', '#FFFFFF', '#DC2626', '#2563EB', '#16A34A',
           '#F59E0B', '#7C3AED', '#EC4899', '#6B7280', '#92400E'
       ), 1, @NOW
FROM @N;

INSERT INTO kich_co (gia_tri, ghi_chu, trang_thai, ngay_tao)
SELECT CAST(34 + n AS NVARCHAR(20)), CONCAT(N'Kích cỡ EU ', 34 + n), 1, @NOW FROM @N;

INSERT INTO giay (ma, ten, mo_ta, gioi_tinh, trang_thai, thuong_hieu_id, loai_giay_id, ngay_tao)
SELECT CONCAT('SP', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), CONCAT(N'SportShoe Mẫu ', n),
       CONCAT(N'Sản phẩm giày cố định số ', n), 3, 1,
       (SELECT id FROM thuong_hieu WHERE ma = CONCAT('TH', RIGHT('00' + CAST(n AS VARCHAR(2)), 2))),
       (SELECT id FROM loai_giay WHERE ma = CONCAT('LG', RIGHT('00' + CAST(n AS VARCHAR(2)), 2))), @NOW
FROM @N;

INSERT INTO giay_chi_tiet (
    giay_id, kich_co_id, mau_sac_id, ma_bien_the, sku, gia_goc, gia_ban,
    so_luong, kich_hoat, ngay_tao
)
SELECT g.id, k.id, m.id, CONCAT('BT', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2)),
       CONCAT('SKU-SP', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2)),
       1000000 + n.n * 100000, 900000 + n.n * 100000, 10 + n.n, 1, @NOW
FROM @N n
JOIN giay g ON g.ma = CONCAT('SP', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2))
JOIN kich_co k ON k.gia_tri = CAST(34 + n.n AS NVARCHAR(20))
JOIN mau_sac m ON m.ma = CONCAT('MS', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2));

INSERT INTO giay_thuoc_tinh (
    giay_id, chat_lieu_giay_id, co_giay_id, cong_nghe_dem_id, de_giay_id,
    trong_luong_id, trang_thai, ngay_tao
)
SELECT g.id, cl.id, cg.id, cn.id, dg.id, tl.id, 1, @NOW
FROM @N n
JOIN giay g ON g.ma = CONCAT('SP', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2))
JOIN chat_lieu_giay cl ON cl.ma = CONCAT('CL', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2))
JOIN co_giay cg ON cg.ma = CONCAT('CG', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2))
JOIN cong_nghe_dem cn ON cn.ma = CONCAT('CN', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2))
JOIN de_giay dg ON dg.ma = CONCAT('DG', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2))
JOIN trong_luong tl ON tl.ma = CONCAT('TL', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2));

INSERT INTO hinh_anh_giay (giay_chi_tiet_id, url, loai_hinh, la_hinh_chinh, trang_thai, ngay_tao)
SELECT id, CONCAT(N'/uploads/products/', LOWER(sku), N'.jpg'), 1, 1, 1, @NOW FROM giay_chi_tiet;

INSERT INTO dot_giam_gia (ma, ten, mo_ta, loai_giam, gia_tri_giam, ngay_bat_dau, ngay_ket_thuc, kich_hoat, ngay_tao)
SELECT CONCAT('DGG', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), CONCAT(N'Đợt giảm giá ', n),
       CONCAT(N'Giảm giá cố định đợt ', n), 1, 5 + n,
       CAST('2025-01-01T00:00:00' AS DATETIME2), CAST('2035-12-31T23:59:59' AS DATETIME2), 1, @NOW FROM @N;

INSERT INTO dot_giam_gia_san_pham (dot_giam_gia_id, giay_chi_tiet_id, trang_thai, ngay_tao)
SELECT d.id, v.id, 1, @NOW FROM @N n
JOIN dot_giam_gia d ON d.ma = CONCAT('DGG', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2))
JOIN giay_chi_tiet v ON v.sku = CONCAT('SKU-SP', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2));

INSERT INTO phieu_giam_gia (
    ma, ten, loai, gia_tri, gia_tri_toi_thieu, giam_toi_da, loai_phieu,
    so_luong, so_luong_da_dung, trang_thai, ngay_bat_dau, ngay_ket_thuc, ngay_tao
)
SELECT CONCAT('PGG', RIGHT('00' + CAST(n AS VARCHAR(2)), 2)), CONCAT(N'Phiếu giảm giá ', n),
       1, 5 + n, 300000 + n * 50000, 100000 + n * 10000, 2,
       100, 0, 1, CAST('2025-01-01T00:00:00' AS DATETIME2), CAST('2035-12-31T23:59:59' AS DATETIME2), @NOW
FROM @N;

INSERT INTO phieu_giam_gia_khach_hang (phieu_giam_gia_id, khach_hang_id, trang_thai, ngay_tao)
SELECT p.id,
       CONVERT(UNIQUEIDENTIFIER, CONCAT('20000000-0000-0000-0000-', RIGHT('000000000000' + CAST(n.n AS VARCHAR(12)), 12))),
       1, @NOW
FROM @N n
JOIN phieu_giam_gia p ON p.ma = CONCAT('PGG', RIGHT('00' + CAST(n.n AS VARCHAR(2)), 2));

COMMIT TRANSACTION;
