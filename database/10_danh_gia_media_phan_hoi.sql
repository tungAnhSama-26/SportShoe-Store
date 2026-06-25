-- ============================================================================
-- Tinh nang DANH GIA nang cao (4 phan):
--   1. media        : anh/video kem danh gia (chuoi JSON [{"url":..,"loai":..}])
--   2. phan_hoi     : phan hoi cua shop cho danh gia (moi danh gia 1 lan)
--   3. ngay_phan_hoi: thoi diem shop phan hoi
--   4. da_xem       : admin da xem danh gia chua (cho chuong thong bao)
--
-- Chay 1 lan tren database "giay" (chay lai cung an toan).
-- ============================================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.danh_gia') AND name = 'media'
)
BEGIN
    ALTER TABLE dbo.danh_gia ADD media NVARCHAR(MAX) NULL;
    PRINT N'Da them cot danh_gia.media';
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.danh_gia') AND name = 'phan_hoi'
)
BEGIN
    ALTER TABLE dbo.danh_gia ADD phan_hoi NVARCHAR(1000) NULL;
    PRINT N'Da them cot danh_gia.phan_hoi';
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.danh_gia') AND name = 'ngay_phan_hoi'
)
BEGIN
    ALTER TABLE dbo.danh_gia ADD ngay_phan_hoi DATETIME2 NULL;
    PRINT N'Da them cot danh_gia.ngay_phan_hoi';
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.danh_gia') AND name = 'da_xem'
)
BEGIN
    ALTER TABLE dbo.danh_gia ADD da_xem BIT NOT NULL CONSTRAINT df_danh_gia_da_xem DEFAULT 0;
    PRINT N'Da them cot danh_gia.da_xem';
END
GO

-- Danh gia da co tu truoc coi nhu "da xem" de chuong khong bao nham hang loat.
UPDATE dbo.danh_gia SET da_xem = 1 WHERE da_xem = 0;
PRINT N'Da backfill da_xem=1 cho danh gia cu.';
GO
