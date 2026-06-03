USE giay;
GO

SET NOCOUNT ON;
GO

-- ============================================================
-- TEST DATA - ALL INVOICE FLOWS
-- Run after:
--   01_schema_tables_rules.sql
--   02_seed_full_10_records.sql
--
-- This script is idempotent for the HD17797265394xx records below.
-- Constraint recreation uses WITH NOCHECK so old dirty data in an existing DB
-- does not block this test dataset; the recreated constraints still protect
-- new inserts/updates after this script runs.
-- ============================================================

IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'ck_hoa_don_trang_thai'
      AND parent_object_id = OBJECT_ID(N'dbo.hoa_don')
)
BEGIN
    ALTER TABLE dbo.hoa_don DROP CONSTRAINT ck_hoa_don_trang_thai;
END;

ALTER TABLE dbo.hoa_don WITH NOCHECK
ADD CONSTRAINT ck_hoa_don_trang_thai
CHECK (trang_thai IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'ck_tt_hinh_thuc'
      AND parent_object_id = OBJECT_ID(N'dbo.thanh_toan')
)
BEGIN
    ALTER TABLE dbo.thanh_toan DROP CONSTRAINT ck_tt_hinh_thuc;
END;

ALTER TABLE dbo.thanh_toan WITH NOCHECK
ADD CONSTRAINT ck_tt_hinh_thuc
CHECK (hinh_thuc IN (1, 2, 3, 4));

IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'ck_tt_trang_thai'
      AND parent_object_id = OBJECT_ID(N'dbo.thanh_toan')
)
BEGIN
    ALTER TABLE dbo.thanh_toan DROP CONSTRAINT ck_tt_trang_thai;
END;

ALTER TABLE dbo.thanh_toan WITH NOCHECK
ADD CONSTRAINT ck_tt_trang_thai
CHECK (trang_thai IN (0, 1, 2, 3, 4, 5));
GO

DECLARE @baseTime DATETIME2 = DATEADD(HOUR, 8, CONVERT(DATETIME2, CONVERT(date, SYSDATETIME())));

DROP TABLE IF EXISTS #Flow; CREATE TABLE #Flow (
    ma NVARCHAR(150) NOT NULL,
    mo_ta NVARCHAR(300) NOT NULL,
    kenh_ban INT NOT NULL,
    khach_user VARCHAR(50) NOT NULL,
    nv_ma VARCHAR(20) NULL,
    trang_thai INT NOT NULL,
    tong_hang DECIMAL(18,2) NOT NULL,
    tien_giam DECIMAL(18,2) NOT NULL,
    tong_thanh_toan DECIMAL(18,2) NOT NULL,
    gct_ma NVARCHAR(150) NOT NULL,
    so_luong INT NOT NULL,
    gia_don_vi DECIMAL(18,2) NOT NULL,
    vc_trang_thai INT NULL,
    vc_phi DECIMAL(18,2) NOT NULL,
    tt_hinh_thuc INT NOT NULL,
    tt_trang_thai INT NOT NULL,
    tt_ngay DATETIME2 NULL,
    tt_ghi_chu NVARCHAR(500) NOT NULL,
    ngay_tao DATETIME2 NOT NULL
);

INSERT INTO #Flow
(ma, mo_ta, kenh_ban, khach_user, nv_ma, trang_thai, tong_hang, tien_giam, tong_thanh_toan, gct_ma, so_luong, gia_don_vi, vc_trang_thai, vc_phi, tt_hinh_thuc, tt_trang_thai, tt_ngay, tt_ghi_chu, ngay_tao)
VALUES
(N'HD1779726539414', N'Online moi tao, cho xac nhan', 2, 'khach1', NULL, 1, 1690000, 0, 1720000, N'GCT001', 1, 1690000, 1, 30000, 4, 0, NULL, N'COD cho thu tien khi nhan hang.', @baseTime),
(N'HD1779726539415', N'Cua hang thanh toan tai quay hoan thanh', 1, 'khach2', 'NV002', 5, 1690000, 0, 1690000, N'GCT001', 1, 1690000, NULL, 0, 1, 1, DATEADD(MINUTE, 35, @baseTime), N'Tien mat tai quay da thu.', DATEADD(MINUTE, 30, @baseTime)),
(N'HD1779726539416', N'Online da thanh toan, dang giao hang', 2, 'khach3', 'NV002', 3, 1690000, 0, 1720000, N'GCT001', 1, 1690000, 2, 30000, 2, 1, DATEADD(MINUTE, 60, @baseTime), N'Chuyen khoan online thanh cong.', DATEADD(MINUTE, 60, @baseTime)),
(N'HD1779726539417', N'COD da giao hang, cho xac nhan thu tien', 2, 'khach4', 'NV002', 4, 1690000, 0, 1720000, N'GCT001', 1, 1690000, 3, 30000, 4, 0, NULL, N'COD da giao, cho admin xac nhan thanh toan.', DATEADD(MINUTE, 90, @baseTime)),
(N'HD1779726539418', N'COD giao hang that bai, huy giao dich cho thu tien', 2, 'khach5', 'NV002', 10, 1690000, 0, 1720000, N'GCT001', 1, 1690000, 4, 30000, 4, 3, NULL, N'Khach khong nhan hang, huy giao dich COD.', DATEADD(MINUTE, 120, @baseTime)),
(N'HD1779726539419', N'Online da thanh toan nhung giao that bai, can hoan tien', 2, 'khach6', 'NV002', 10, 1690000, 0, 1720000, N'GCT001', 1, 1690000, 4, 30000, 2, 4, DATEADD(MINUTE, 150, @baseTime), N'Da thu tien truoc, giao that bai nen can hoan tien.', DATEADD(MINUTE, 150, @baseTime)),
(N'HD1779726539420', N'Don dang can hoan tien', 2, 'khach7', 'NV002', 8, 1690000, 0, 1720000, N'GCT001', 1, 1690000, 3, 30000, 2, 4, DATEADD(MINUTE, 180, @baseTime), N'Cho admin xac nhan hoan tien.', DATEADD(MINUTE, 180, @baseTime)),
(N'HD1779726539421', N'Don da huy sau khi hoan tien xong', 2, 'khach8', 'NV002', 6, 1690000, 0, 1720000, N'GCT001', 1, 1690000, 5, 30000, 2, 5, DATEADD(MINUTE, 215, @baseTime), N'Da hoan tien cho khach.', DATEADD(MINUTE, 210, @baseTime)),
(N'HD1779726539422', N'Khach gui yeu cau huy don', 2, 'khach9', NULL, 7, 1690000, 0, 1720000, N'GCT001', 1, 1690000, 1, 30000, 4, 0, NULL, N'Cho nhan vien xu ly yeu cau huy.', DATEADD(MINUTE, 240, @baseTime)),
(N'HD1779726539423', N'Cua hang tao ho khach va co giao hang', 1, 'khach10', 'NV002', 3, 1690000, 0, 1720000, N'GCT001', 1, 1690000, 2, 30000, 1, 1, DATEADD(MINUTE, 275, @baseTime), N'Thu tien tai quay, tiep tuc giao hang.', DATEADD(MINUTE, 270, @baseTime));

IF EXISTS (
    SELECT 1
    FROM #Flow f
    LEFT JOIN khach_hang kh ON kh.ten_dang_nhap = f.khach_user
    LEFT JOIN giay_chi_tiet gct ON gct.ma_bien_the = f.gct_ma
    WHERE kh.id IS NULL OR gct.id IS NULL
)
BEGIN
    THROW 51000, 'Missing seed data. Run 02_seed_full_10_records.sql first.', 1;
END;

IF EXISTS (
    SELECT 1
    FROM #Flow f
    LEFT JOIN nhan_vien nv ON nv.ma = f.nv_ma
    WHERE f.nv_ma IS NOT NULL AND nv.id IS NULL
)
BEGIN
    THROW 51001, 'Missing employee seed data. Check nhan_vien.ma in #Flow.', 1;
END;

DELETE tt
FROM thanh_toan tt
JOIN hoa_don hd ON hd.id = tt.hoa_don_id
JOIN #Flow f ON f.ma = hd.ma;

DELETE ls
FROM lich_su_hoa_don ls
JOIN hoa_don hd ON hd.id = ls.hoa_don_id
JOIN #Flow f ON f.ma = hd.ma;

DELETE vc
FROM van_chuyen vc
JOIN hoa_don hd ON hd.id = vc.hoa_don_id
JOIN #Flow f ON f.ma = hd.ma;

DELETE pthct
FROM phieu_tra_hang_chi_tiet pthct
JOIN phieu_tra_hang pth ON pth.id = pthct.phieu_tra_hang_id
JOIN hoa_don hd ON hd.id = pth.hoa_don_id
JOIN #Flow f ON f.ma = hd.ma;

DELETE pth
FROM phieu_tra_hang pth
JOIN hoa_don hd ON hd.id = pth.hoa_don_id
JOIN #Flow f ON f.ma = hd.ma;

DELETE hdct
FROM hoa_don_chi_tiet hdct
JOIN hoa_don hd ON hd.id = hdct.hoa_don_id
JOIN #Flow f ON f.ma = hd.ma;

DELETE hd
FROM hoa_don hd
JOIN #Flow f ON f.ma = hd.ma;

INSERT INTO hoa_don
(ma, kenh_ban, khach_hang_id, nhan_vien_id, phieu_giam_gia_id, ten_nguoi_nhan, sdt_nguoi_nhan, dia_chi_giao_hang, ngay_lap, ngay_thanh_toan, trang_thai, tong_tien_hang, tien_giam, tong_tien_thanh_toan, ghi_chu, ngay_tao, ngay_cap_nhat)
SELECT
    f.ma,
    f.kenh_ban,
    kh.id,
    nv.id,
    NULL,
    kh.ho_ten,
    COALESCE(kh.sdt, '0987654321'),
    CASE WHEN f.kenh_ban = 1 AND f.vc_trang_thai IS NULL THEN N'Mua tai quay' ELSE N'123 Duong Test, Phuong Test, Quan Test, Ha Noi' END,
    f.ngay_tao,
    f.tt_ngay,
    f.trang_thai,
    f.tong_hang,
    f.tien_giam,
    f.tong_thanh_toan,
    f.mo_ta,
    f.ngay_tao,
    DATEADD(MINUTE, 5, f.ngay_tao)
FROM #Flow f
JOIN khach_hang kh ON kh.ten_dang_nhap = f.khach_user
LEFT JOIN nhan_vien nv ON nv.ma = f.nv_ma;

INSERT INTO hoa_don_chi_tiet
(hoa_don_id, giay_chi_tiet_id, so_luong, gia_don_vi, thanh_tien, trang_thai, ngay_tao)
SELECT
    hd.id,
    gct.id,
    f.so_luong,
    f.gia_don_vi,
    f.so_luong * f.gia_don_vi,
    CASE WHEN f.trang_thai = 6 THEN 0 ELSE 1 END,
    f.ngay_tao
FROM #Flow f
JOIN hoa_don hd ON hd.ma = f.ma
JOIN giay_chi_tiet gct ON gct.ma_bien_the = f.gct_ma;

INSERT INTO van_chuyen
(hoa_don_id, don_vi_van_chuyen, ma_van_don, phi_van_chuyen, ngay_gui, ngay_du_kien, ngay_giao_that, trang_thai, ghi_chu, ngay_tao, ngay_cap_nhat)
SELECT
    hd.id,
    N'GHN',
    CONCAT(N'GHN_', RIGHT(f.ma, 2)),
    f.vc_phi,
    CASE WHEN f.vc_trang_thai IN (2, 3, 4, 5) THEN DATEADD(MINUTE, 10, f.ngay_tao) ELSE NULL END,
    DATEADD(DAY, 3, f.ngay_tao),
    CASE WHEN f.vc_trang_thai IN (3, 4, 5) THEN DATEADD(DAY, 1, f.ngay_tao) ELSE NULL END,
    f.vc_trang_thai,
    f.mo_ta,
    f.ngay_tao,
    DATEADD(MINUTE, 5, f.ngay_tao)
FROM #Flow f
JOIN hoa_don hd ON hd.ma = f.ma
WHERE f.vc_trang_thai IS NOT NULL;

INSERT INTO thanh_toan
(hoa_don_id, nhan_vien_id, ma_giao_dich, hinh_thuc, so_tien, tien_thoi_lai, ngan_hang, noi_dung_ck, cong_thanh_toan, ngay_thanh_toan, trang_thai, ghi_chu, ngay_tao)
SELECT
    hd.id,
    nv.id,
    CONCAT(N'TT_', RIGHT(f.ma, 2)),
    f.tt_hinh_thuc,
    f.tong_thanh_toan,
    NULL,
    CASE WHEN f.tt_hinh_thuc = 2 THEN N'MB Bank' ELSE NULL END,
    CONCAT(N'Thanh toan ', f.ma),
    CASE
        WHEN f.tt_hinh_thuc = 1 THEN N'POS'
        WHEN f.tt_hinh_thuc = 2 THEN N'Bank Transfer'
        WHEN f.tt_hinh_thuc = 3 THEN N'VNPay'
        WHEN f.tt_hinh_thuc = 4 THEN N'COD'
    END,
    f.tt_ngay,
    f.tt_trang_thai,
    f.tt_ghi_chu,
    f.ngay_tao
FROM #Flow f
JOIN hoa_don hd ON hd.ma = f.ma
LEFT JOIN nhan_vien nv ON nv.ma = COALESCE(f.nv_ma, 'NV002');

INSERT INTO lich_su_hoa_don
(hoa_don_id, nhan_vien_id, trang_thai, ghi_chu, ngay_tao)
SELECT
    hd.id,
    nv.id,
    CASE f.trang_thai
        WHEN 1 THEN N'Cho xac nhan'
        WHEN 2 THEN N'Cho lay hang'
        WHEN 3 THEN N'Cho giao hang'
        WHEN 4 THEN N'Da giao hang'
        WHEN 5 THEN N'Hoan thanh'
        WHEN 6 THEN N'Huy'
        WHEN 7 THEN N'Yeu cau huy'
        WHEN 8 THEN N'Can hoan tien'
        WHEN 9 THEN N'Da xac nhan'
        WHEN 10 THEN N'Giao hang that bai'
    END,
    f.mo_ta,
    f.ngay_tao
FROM #Flow f
JOIN hoa_don hd ON hd.ma = f.ma
LEFT JOIN nhan_vien nv ON nv.ma = f.nv_ma;

SELECT
    hd.ma,
    hd.trang_thai AS hoa_don_trang_thai,
    hd.kenh_ban,
    hd.tong_tien_thanh_toan,
    tt.hinh_thuc AS thanh_toan_hinh_thuc,
    tt.trang_thai AS thanh_toan_trang_thai,
    vc.trang_thai AS van_chuyen_trang_thai
FROM #Flow f
JOIN hoa_don hd ON hd.ma = f.ma
LEFT JOIN thanh_toan tt ON tt.hoa_don_id = hd.id
LEFT JOIN van_chuyen vc ON vc.hoa_don_id = hd.id
ORDER BY hd.ma;
GO
