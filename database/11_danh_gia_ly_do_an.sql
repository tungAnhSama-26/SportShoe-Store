-- ============================================================================
-- AI kiem duyet danh gia:
--   ly_do_an: ly do danh gia bi an (AI tu an / quan tri vien xoa) de admin xem
--             va khoi phuc neu AI an nham.
-- Chay 1 lan tren database "giay" (chay lai cung an toan).
-- ============================================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.danh_gia') AND name = 'ly_do_an'
)
BEGIN
    ALTER TABLE dbo.danh_gia ADD ly_do_an NVARCHAR(500) NULL;
    PRINT N'Da them cot danh_gia.ly_do_an';
END
GO
