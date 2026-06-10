USE giay;
GO

SET XACT_ABORT ON;
GO

BEGIN TRANSACTION;

IF COL_LENGTH('dbo.phieu_tra_hang', 'loai_yeu_cau') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD loai_yeu_cau INT NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'ly_do_ma') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD ly_do_ma NVARCHAR(50) NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'mo_ta') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD mo_ta NVARCHAR(1000) NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'tong_tien_du_kien') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD tong_tien_du_kien DECIMAL(18,2) NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'tong_tien_thuc_te') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD tong_tien_thuc_te DECIMAL(18,2) NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'don_vi_van_chuyen') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD don_vi_van_chuyen NVARCHAR(100) NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'ma_van_don_hoan') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD ma_van_don_hoan NVARCHAR(150) NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'trang_thai_van_chuyen') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD trang_thai_van_chuyen INT NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'ly_do_tu_choi') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD ly_do_tu_choi NVARCHAR(500) NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'ngay_duyet') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD ngay_duyet DATETIMEOFFSET NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'ngay_gui_hang') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD ngay_gui_hang DATETIMEOFFSET NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'ngay_nhan_hang') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD ngay_nhan_hang DATETIMEOFFSET NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'ngay_kiem_tra') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD ngay_kiem_tra DATETIMEOFFSET NULL;

IF COL_LENGTH('dbo.phieu_tra_hang', 'ngay_hoan_tat') IS NULL
    ALTER TABLE dbo.phieu_tra_hang ADD ngay_hoan_tat DATETIMEOFFSET NULL;
GO

UPDATE dbo.phieu_tra_hang
SET loai_yeu_cau = COALESCE(loai_yeu_cau, 2),
    ly_do_ma = COALESCE(ly_do_ma, N'KHAC'),
    mo_ta = COALESCE(mo_ta, ly_do),
    tong_tien_du_kien = COALESCE(tong_tien_du_kien, tong_tien_hoan),
    tong_tien_thuc_te = COALESCE(
        tong_tien_thuc_te,
        CASE WHEN trang_thai = 2 THEN tong_tien_hoan ELSE 0 END
    ),
    ngay_hoan_tat = CASE
        WHEN trang_thai = 2 THEN COALESCE(ngay_hoan_tat, ngay_cap_nhat)
        ELSE ngay_hoan_tat
    END;

ALTER TABLE dbo.phieu_tra_hang ALTER COLUMN khach_hang_id UNIQUEIDENTIFIER NULL;
ALTER TABLE dbo.phieu_tra_hang ALTER COLUMN loai_yeu_cau INT NOT NULL;
ALTER TABLE dbo.phieu_tra_hang ALTER COLUMN tong_tien_du_kien DECIMAL(18,2) NOT NULL;
ALTER TABLE dbo.phieu_tra_hang ALTER COLUMN tong_tien_thuc_te DECIMAL(18,2) NOT NULL;

DECLARE @constraintName SYSNAME;
DECLARE @dropConstraintSql NVARCHAR(500);

SELECT @constraintName = name
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.phieu_tra_hang')
  AND name = 'ck_pth_trang_thai';

IF @constraintName IS NOT NULL
BEGIN
    SET @dropConstraintSql = N'ALTER TABLE dbo.phieu_tra_hang DROP CONSTRAINT ' + QUOTENAME(@constraintName);
    EXEC sp_executesql @dropConstraintSql;
END;

ALTER TABLE dbo.phieu_tra_hang
    ADD CONSTRAINT ck_pth_trang_thai
    CHECK (trang_thai IN (1,2,3,4,5,6,7,8,9,10));

UPDATE dbo.phieu_tra_hang
SET trang_thai = CASE
    WHEN trang_thai = 2 THEN 7
    WHEN trang_thai = 3 THEN 8
    ELSE trang_thai
END;

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.phieu_tra_hang')
      AND name = 'ck_pth_loai_yeu_cau'
)
    ALTER TABLE dbo.phieu_tra_hang
        ADD CONSTRAINT ck_pth_loai_yeu_cau CHECK (loai_yeu_cau IN (1,2));

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.phieu_tra_hang')
      AND name = 'ck_pth_tong_tien_du_kien'
)
    ALTER TABLE dbo.phieu_tra_hang
        ADD CONSTRAINT ck_pth_tong_tien_du_kien CHECK (tong_tien_du_kien >= 0);

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.phieu_tra_hang')
      AND name = 'ck_pth_tong_tien_thuc_te'
)
    ALTER TABLE dbo.phieu_tra_hang
        ADD CONSTRAINT ck_pth_tong_tien_thuc_te CHECK (tong_tien_thuc_te >= 0);

IF COL_LENGTH('dbo.phieu_tra_hang_chi_tiet', 'so_luong_nhan') IS NULL
    ALTER TABLE dbo.phieu_tra_hang_chi_tiet ADD so_luong_nhan INT NULL;

IF COL_LENGTH('dbo.phieu_tra_hang_chi_tiet', 'so_luong_chap_nhan') IS NULL
    ALTER TABLE dbo.phieu_tra_hang_chi_tiet ADD so_luong_chap_nhan INT NULL;

IF COL_LENGTH('dbo.phieu_tra_hang_chi_tiet', 'so_luong_tu_choi') IS NULL
    ALTER TABLE dbo.phieu_tra_hang_chi_tiet ADD so_luong_tu_choi INT NULL;

IF COL_LENGTH('dbo.phieu_tra_hang_chi_tiet', 'tinh_trang_san_pham') IS NULL
    ALTER TABLE dbo.phieu_tra_hang_chi_tiet ADD tinh_trang_san_pham NVARCHAR(500) NULL;

IF COL_LENGTH('dbo.phieu_tra_hang_chi_tiet', 'so_tien_hoan') IS NULL
    ALTER TABLE dbo.phieu_tra_hang_chi_tiet ADD so_tien_hoan DECIMAL(18,2) NULL;

IF COL_LENGTH('dbo.phieu_tra_hang_chi_tiet', 'nhap_lai_ton_kho') IS NULL
    ALTER TABLE dbo.phieu_tra_hang_chi_tiet ADD nhap_lai_ton_kho BIT NULL;

IF COL_LENGTH('dbo.phieu_tra_hang_chi_tiet', 'da_cap_nhat_ton') IS NULL
    ALTER TABLE dbo.phieu_tra_hang_chi_tiet ADD da_cap_nhat_ton BIT NULL;
GO

UPDATE ct
SET so_luong_nhan = COALESCE(ct.so_luong_nhan, CASE WHEN pth.trang_thai IN (7,8) THEN ct.so_luong_tra ELSE 0 END),
    so_luong_chap_nhan = COALESCE(ct.so_luong_chap_nhan, CASE WHEN pth.trang_thai = 7 AND ct.trang_thai = 1 THEN ct.so_luong_tra ELSE 0 END),
    so_luong_tu_choi = COALESCE(ct.so_luong_tu_choi, CASE WHEN pth.trang_thai = 8 OR ct.trang_thai = 0 THEN ct.so_luong_tra ELSE 0 END),
    so_tien_hoan = COALESCE(ct.so_tien_hoan, CASE WHEN pth.trang_thai = 7 AND ct.trang_thai = 1 THEN ct.thanh_tien ELSE 0 END),
    nhap_lai_ton_kho = COALESCE(ct.nhap_lai_ton_kho, 0),
    da_cap_nhat_ton = COALESCE(ct.da_cap_nhat_ton, 0)
FROM dbo.phieu_tra_hang_chi_tiet ct
JOIN dbo.phieu_tra_hang pth ON pth.id = ct.phieu_tra_hang_id;

ALTER TABLE dbo.phieu_tra_hang_chi_tiet ALTER COLUMN so_luong_nhan INT NOT NULL;
ALTER TABLE dbo.phieu_tra_hang_chi_tiet ALTER COLUMN so_luong_chap_nhan INT NOT NULL;
ALTER TABLE dbo.phieu_tra_hang_chi_tiet ALTER COLUMN so_luong_tu_choi INT NOT NULL;

-- Xóa default constraint cũ của cột so_tien_hoan nếu có
DECLARE @dfName SYSNAME;
DECLARE @dropDfSql NVARCHAR(MAX);

SELECT @dfName = dc.name
FROM sys.default_constraints dc
JOIN sys.columns c 
    ON c.default_object_id = dc.object_id
WHERE dc.parent_object_id = OBJECT_ID('dbo.phieu_tra_hang_chi_tiet')
  AND c.name = 'so_tien_hoan';

IF @dfName IS NOT NULL
BEGIN
    SET @dropDfSql = N'ALTER TABLE dbo.phieu_tra_hang_chi_tiet DROP CONSTRAINT ' + QUOTENAME(@dfName);
    EXEC sp_executesql @dropDfSql;
END;

ALTER TABLE dbo.phieu_tra_hang_chi_tiet ALTER COLUMN so_tien_hoan DECIMAL(18,2) NOT NULL;

-- Thêm lại default constraint cho so_tien_hoan
IF NOT EXISTS (
    SELECT 1
    FROM sys.default_constraints dc
    JOIN sys.columns c 
        ON c.default_object_id = dc.object_id
    WHERE dc.parent_object_id = OBJECT_ID('dbo.phieu_tra_hang_chi_tiet')
      AND c.name = 'so_tien_hoan'
)
BEGIN
    ALTER TABLE dbo.phieu_tra_hang_chi_tiet
    ADD CONSTRAINT DF_pthct_so_tien_hoan DEFAULT 0 FOR so_tien_hoan;
END;

ALTER TABLE dbo.phieu_tra_hang_chi_tiet ALTER COLUMN nhap_lai_ton_kho BIT NOT NULL;
ALTER TABLE dbo.phieu_tra_hang_chi_tiet ALTER COLUMN da_cap_nhat_ton BIT NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.phieu_tra_hang_chi_tiet')
      AND name = 'ck_pthct_so_luong_xu_ly'
)
    ALTER TABLE dbo.phieu_tra_hang_chi_tiet
        ADD CONSTRAINT ck_pthct_so_luong_xu_ly CHECK (
            so_luong_nhan >= 0
            AND so_luong_chap_nhan >= 0
            AND so_luong_tu_choi >= 0
            AND so_luong_nhan <= so_luong_tra
            AND so_luong_chap_nhan + so_luong_tu_choi <= so_luong_nhan
        );

IF OBJECT_ID('dbo.lich_su_phieu_tra_hang', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.lich_su_phieu_tra_hang (
        id                  INT              NOT NULL CONSTRAINT pk_lspth PRIMARY KEY IDENTITY(1,1),
        phieu_tra_hang_id   INT              NOT NULL,
        nhan_vien_id        UNIQUEIDENTIFIER NULL,
        trang_thai_cu       INT              NULL,
        trang_thai_moi      INT              NOT NULL,
        hanh_dong           NVARCHAR(150)    NOT NULL,
        ghi_chu             NVARCHAR(1000)   NULL,
        ngay_tao            DATETIMEOFFSET   NOT NULL CONSTRAINT df_lspth_ngay_tao DEFAULT SYSDATETIMEOFFSET(),

        CONSTRAINT fk_lspth_phieu 
            FOREIGN KEY (phieu_tra_hang_id) 
            REFERENCES dbo.phieu_tra_hang(id) 
            ON DELETE CASCADE,

        CONSTRAINT fk_lspth_nhan_vien 
            FOREIGN KEY (nhan_vien_id) 
            REFERENCES dbo.nhan_vien(id)
    );

    CREATE INDEX ix_lspth_phieu 
    ON dbo.lich_su_phieu_tra_hang(phieu_tra_hang_id, ngay_tao DESC);
END;

IF OBJECT_ID('dbo.hinh_anh_tra_hang', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.hinh_anh_tra_hang (
        id                         INT             NOT NULL CONSTRAINT pk_hath PRIMARY KEY IDENTITY(1,1),
        phieu_tra_hang_id          INT             NOT NULL,
        phieu_tra_hang_chi_tiet_id INT             NULL,
        url                        NVARCHAR(1000)  NOT NULL,
        loai_anh                   INT             NOT NULL,
        ghi_chu                    NVARCHAR(500)   NULL,
        ngay_tao                   DATETIMEOFFSET  NOT NULL CONSTRAINT df_hath_ngay_tao DEFAULT SYSDATETIMEOFFSET(),

        CONSTRAINT ck_hath_loai_anh 
            CHECK (loai_anh IN (1,2)),

        CONSTRAINT fk_hath_phieu 
            FOREIGN KEY (phieu_tra_hang_id) 
            REFERENCES dbo.phieu_tra_hang(id) 
            ON DELETE CASCADE,

        CONSTRAINT fk_hath_chi_tiet 
            FOREIGN KEY (phieu_tra_hang_chi_tiet_id) 
            REFERENCES dbo.phieu_tra_hang_chi_tiet(id)
    );

    CREATE INDEX ix_hath_phieu 
    ON dbo.hinh_anh_tra_hang(phieu_tra_hang_id);
END;

IF COL_LENGTH('dbo.thanh_toan', 'loai_giao_dich') IS NULL
    ALTER TABLE dbo.thanh_toan ADD loai_giao_dich INT NULL;

IF COL_LENGTH('dbo.thanh_toan', 'phieu_tra_hang_id') IS NULL
    ALTER TABLE dbo.thanh_toan ADD phieu_tra_hang_id INT NULL;

IF COL_LENGTH('dbo.thanh_toan', 'giao_dich_goc_id') IS NULL
    ALTER TABLE dbo.thanh_toan ADD giao_dich_goc_id INT NULL;
GO

UPDATE dbo.thanh_toan
SET loai_giao_dich = COALESCE(loai_giao_dich, CASE WHEN trang_thai = 5 THEN 2 ELSE 1 END);

ALTER TABLE dbo.thanh_toan ALTER COLUMN loai_giao_dich INT NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.thanh_toan')
      AND name = 'ck_tt_loai_giao_dich'
)
    ALTER TABLE dbo.thanh_toan
        ADD CONSTRAINT ck_tt_loai_giao_dich CHECK (loai_giao_dich IN (1,2));

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys 
    WHERE name = 'fk_tt_phieu_tra_hang'
)
    ALTER TABLE dbo.thanh_toan
        ADD CONSTRAINT fk_tt_phieu_tra_hang
        FOREIGN KEY (phieu_tra_hang_id) 
        REFERENCES dbo.phieu_tra_hang(id);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys 
    WHERE name = 'fk_tt_giao_dich_goc'
)
    ALTER TABLE dbo.thanh_toan
        ADD CONSTRAINT fk_tt_giao_dich_goc
        FOREIGN KEY (giao_dich_goc_id) 
        REFERENCES dbo.thanh_toan(id);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID('dbo.thanh_toan') 
      AND name = 'ix_tt_phieu_tra_hang'
)
    CREATE INDEX ix_tt_phieu_tra_hang 
    ON dbo.thanh_toan(phieu_tra_hang_id);

IF NOT EXISTS (
    SELECT 1 FROM dbo.lich_su_phieu_tra_hang
)
BEGIN
    INSERT INTO dbo.lich_su_phieu_tra_hang
        (phieu_tra_hang_id, nhan_vien_id, trang_thai_cu, trang_thai_moi, hanh_dong, ghi_chu, ngay_tao)
    SELECT id, nhan_vien_id, NULL, trang_thai, N'Khởi tạo dữ liệu lịch sử', ly_do,
           TODATETIMEOFFSET(ngay_tao, '+07:00')
    FROM dbo.phieu_tra_hang;
END;

COMMIT TRANSACTION;
GO

SELECT N'phieu_tra_hang' AS bang, COUNT(*) AS so_ban_ghi 
FROM dbo.phieu_tra_hang

UNION ALL

SELECT N'phieu_tra_hang_chi_tiet', COUNT(*) 
FROM dbo.phieu_tra_hang_chi_tiet

UNION ALL

SELECT N'lich_su_phieu_tra_hang', COUNT(*) 
FROM dbo.lich_su_phieu_tra_hang

UNION ALL

SELECT N'hinh_anh_tra_hang', COUNT(*) 
FROM dbo.hinh_anh_tra_hang;
GO