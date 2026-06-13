-- ============================================================================
-- Doi mo hinh tru kho cho ban hang ONLINE:
--   - Dat hang (COD / chuyen khoan): KHONG tru ton kho nua, chi kiem tra con du.
--   - Nhan vien chuyen don sang "Da xac nhan": TRU ton kho (1 lan duy nhat).
--   - Nhan vien "Huy" don da xac nhan: HOAN ton kho.
--
-- Cot hoa_don.da_tru_kho danh dau don da bi tru kho hay chua de khong tru/hoan trung.
-- Chay 1 lan tren database "giay" (chay lai cung an toan).
-- ============================================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.hoa_don') AND name = 'da_tru_kho'
)
BEGIN
    ALTER TABLE dbo.hoa_don
        ADD da_tru_kho BIT NOT NULL CONSTRAINT df_hoa_don_da_tru_kho DEFAULT 0;
    PRINT N'Da them cot hoa_don.da_tru_kho';
END
GO

-- Don cu (tao truoc thay doi nay) da bi tru kho ngay khi dat -> danh dau da tru
-- de nhan vien xac nhan khong tru lap va huy don van hoan ton dung.
UPDATE dbo.hoa_don SET da_tru_kho = 1 WHERE trang_thai <> 0 AND da_tru_kho = 0;
PRINT N'Da backfill da_tru_kho cho don cu.';
