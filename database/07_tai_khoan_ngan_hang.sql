/*
    Migration 07 - Tai khoan ngan hang cua khach hang
    - Chay an toan tren database da co du lieu.
    - Co the chay lai nhieu lan.
*/

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.tai_khoan_ngan_hang', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.tai_khoan_ngan_hang (
        id                   INT              NOT NULL IDENTITY(1,1),
        khach_hang_id        UNIQUEIDENTIFIER NOT NULL,
        ten_ngan_hang        NVARCHAR(100)    NOT NULL,
        so_tai_khoan         VARCHAR(50)      NOT NULL,
        ten_chu_tai_khoan    NVARCHAR(100)    NOT NULL,
        chi_nhanh            NVARCHAR(150)    NULL,
        la_mac_dinh          BIT              NOT NULL
            CONSTRAINT df_tknh_la_mac_dinh DEFAULT 0,
        ngay_tao             DATETIME2        NOT NULL
            CONSTRAINT df_tknh_ngay_tao DEFAULT SYSDATETIME(),
        ngay_cap_nhat        DATETIME2        NULL,
        deleted              BIT              NOT NULL
            CONSTRAINT df_tknh_deleted DEFAULT 0,
        CONSTRAINT pk_tai_khoan_ngan_hang PRIMARY KEY (id),
        CONSTRAINT fk_tknh_khach_hang
            FOREIGN KEY (khach_hang_id) REFERENCES dbo.khach_hang(id) ON DELETE CASCADE,
        CONSTRAINT ck_tknh_ten_ngan_hang
            CHECK (LEN(LTRIM(RTRIM(ten_ngan_hang))) > 0),
        CONSTRAINT ck_tknh_so_tai_khoan
            CHECK (LEN(LTRIM(RTRIM(so_tai_khoan))) > 0),
        CONSTRAINT ck_tknh_ten_chu_tai_khoan
            CHECK (LEN(LTRIM(RTRIM(ten_chu_tai_khoan))) > 0)
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'ux_tknh_khach_hang_tai_khoan_active'
      AND object_id = OBJECT_ID(N'dbo.tai_khoan_ngan_hang')
)
BEGIN
    CREATE UNIQUE INDEX ux_tknh_khach_hang_tai_khoan_active
        ON dbo.tai_khoan_ngan_hang(khach_hang_id, ten_ngan_hang, so_tai_khoan)
        WHERE deleted = 0;
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'ux_tknh_khach_hang_mac_dinh'
      AND object_id = OBJECT_ID(N'dbo.tai_khoan_ngan_hang')
)
BEGIN
    CREATE UNIQUE INDEX ux_tknh_khach_hang_mac_dinh
        ON dbo.tai_khoan_ngan_hang(khach_hang_id)
        WHERE la_mac_dinh = 1 AND deleted = 0;
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'ix_tknh_khach_hang'
      AND object_id = OBJECT_ID(N'dbo.tai_khoan_ngan_hang')
)
BEGIN
    CREATE INDEX ix_tknh_khach_hang
        ON dbo.tai_khoan_ngan_hang(khach_hang_id, deleted, la_mac_dinh);
END;

COMMIT TRANSACTION;
