SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.hoa_don', N'U') IS NULL
        THROW 50001, N'Không tìm thấy bảng dbo.hoa_don.', 1;

    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE parent_object_id = OBJECT_ID(N'dbo.hoa_don')
          AND name = N'ck_hoa_don_trang_thai'
    )
    BEGIN
        ALTER TABLE dbo.hoa_don
            DROP CONSTRAINT ck_hoa_don_trang_thai;
    END;

    ALTER TABLE dbo.hoa_don WITH CHECK
        ADD CONSTRAINT ck_hoa_don_trang_thai
        CHECK (trang_thai IN (0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));

    ALTER TABLE dbo.hoa_don
        CHECK CONSTRAINT ck_hoa_don_trang_thai;

    COMMIT TRANSACTION;

    PRINT N'Đã cập nhật ck_hoa_don_trang_thai để hỗ trợ trạng thái 11 (Hóa đơn chờ).';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
