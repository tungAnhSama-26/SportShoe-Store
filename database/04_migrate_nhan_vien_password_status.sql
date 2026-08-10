SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.nhan_vien', N'U') IS NULL
        THROW 50002, N'Không tìm thấy bảng dbo.nhan_vien.', 1;

    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE parent_object_id = OBJECT_ID(N'dbo.nhan_vien')
          AND name = N'ck_nhan_vien_trang_thai'
    )
    BEGIN
        ALTER TABLE dbo.nhan_vien
            DROP CONSTRAINT ck_nhan_vien_trang_thai;
    END;

    IF COL_LENGTH(N'dbo.nhan_vien', N'bat_buoc_doi_mat_khau') IS NOT NULL
    BEGIN
        EXEC sp_executesql N'
            UPDATE dbo.nhan_vien
            SET trang_thai = 2
            WHERE vai_tro = 2
              AND trang_thai = 1
              AND bat_buoc_doi_mat_khau = 1;
        ';
    END;

    DECLARE @defaultConstraint SYSNAME;
    SELECT @defaultConstraint = dc.name
    FROM sys.default_constraints dc
    INNER JOIN sys.columns c
        ON c.object_id = dc.parent_object_id
       AND c.column_id = dc.parent_column_id
    WHERE dc.parent_object_id = OBJECT_ID(N'dbo.nhan_vien')
      AND c.name = N'bat_buoc_doi_mat_khau';

    IF @defaultConstraint IS NOT NULL
    BEGIN
        DECLARE @dropConstraintSql NVARCHAR(MAX);
        SET @dropConstraintSql =
            N'ALTER TABLE dbo.nhan_vien DROP CONSTRAINT ' + QUOTENAME(@defaultConstraint) + N';';
        EXEC sp_executesql @dropConstraintSql;
    END;

    IF COL_LENGTH(N'dbo.nhan_vien', N'bat_buoc_doi_mat_khau') IS NOT NULL
        ALTER TABLE dbo.nhan_vien DROP COLUMN bat_buoc_doi_mat_khau;

    IF COL_LENGTH(N'dbo.nhan_vien', N'han_doi_mat_khau') IS NOT NULL
        ALTER TABLE dbo.nhan_vien DROP COLUMN han_doi_mat_khau;

    ALTER TABLE dbo.nhan_vien WITH CHECK
        ADD CONSTRAINT ck_nhan_vien_trang_thai
        CHECK (trang_thai IN (0, 1, 2));

    ALTER TABLE dbo.nhan_vien
        CHECK CONSTRAINT ck_nhan_vien_trang_thai;

    COMMIT TRANSACTION;
    PRINT N'Đã chuyển trạng thái đổi mật khẩu lần đầu sang nhan_vien.trang_thai = 2.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
