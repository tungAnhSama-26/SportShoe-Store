IF COL_LENGTH('dbo.nhan_vien', 'cccd') IS NULL
BEGIN
    ALTER TABLE dbo.nhan_vien ADD cccd VARCHAR(12) NULL;
END;
GO

UPDATE dbo.nhan_vien
SET cccd = NULL
WHERE cccd IS NOT NULL
  AND (LEN(LTRIM(RTRIM(cccd))) <> 12 OR LTRIM(RTRIM(cccd)) LIKE '%[^0-9]%');
GO

ALTER TABLE dbo.nhan_vien ALTER COLUMN cccd VARCHAR(12) NULL;
GO
