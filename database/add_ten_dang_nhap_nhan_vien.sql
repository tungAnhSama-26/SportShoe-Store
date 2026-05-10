IF COL_LENGTH('dbo.nhan_vien', 'ten_dang_nhap') IS NULL
BEGIN
    ALTER TABLE dbo.nhan_vien ADD ten_dang_nhap VARCHAR(50) NULL;
END;
GO

UPDATE dbo.nhan_vien
SET ten_dang_nhap = LOWER(ma)
WHERE ten_dang_nhap IS NULL OR LTRIM(RTRIM(ten_dang_nhap)) = '';
GO

ALTER TABLE dbo.nhan_vien ALTER COLUMN ten_dang_nhap VARCHAR(50) NOT NULL;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE name = 'uq_nhan_vien_ten_dang_nhap'
      AND parent_object_id = OBJECT_ID('dbo.nhan_vien')
)
BEGIN
    ALTER TABLE dbo.nhan_vien
    ADD CONSTRAINT uq_nhan_vien_ten_dang_nhap UNIQUE (ten_dang_nhap);
END;
GO
