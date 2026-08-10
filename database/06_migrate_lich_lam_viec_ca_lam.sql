/*
    Chuyển lịch làm việc từ cột ca dạng chuỗi sang khóa ngoại ca_lam_id.

    LƯU Ý:
    - File này chỉ được chạy sau khi đã sao lưu database.
    - Không chạy đồng thời với ứng dụng đang ghi lịch làm việc.
    - Script giữ dữ liệu cũ bằng cách ánh xạ lich_lam_viec.ca -> ca_lam.id.
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.lich_lam_viec', N'U') IS NULL
        THROW 50001, N'Không tìm thấy bảng dbo.lich_lam_viec.', 1;

    IF OBJECT_ID(N'dbo.ca_lam', N'U') IS NULL
        THROW 50002, N'Không tìm thấy bảng dbo.ca_lam.', 1;

    IF COL_LENGTH(N'dbo.lich_lam_viec', N'ca_lam_id') IS NULL
        ALTER TABLE dbo.lich_lam_viec ADD ca_lam_id VARCHAR(50) NULL;

    IF COL_LENGTH(N'dbo.lich_lam_viec', N'ca') IS NOT NULL
    BEGIN
        EXEC sys.sp_executesql N'
            UPDATE dbo.lich_lam_viec
            SET ca_lam_id = LTRIM(RTRIM(ca))
            WHERE ca_lam_id IS NULL;';
    END;

    DECLARE @coCaBiThieu BIT = 0;
    EXEC sys.sp_executesql
        N'SELECT @ketQua = CASE WHEN EXISTS (
              SELECT 1 FROM dbo.lich_lam_viec
              WHERE ca_lam_id IS NULL OR LTRIM(RTRIM(ca_lam_id)) = ''''
          ) THEN 1 ELSE 0 END;',
        N'@ketQua BIT OUTPUT',
        @ketQua = @coCaBiThieu OUTPUT;
    IF @coCaBiThieu = 1
        THROW 50003, N'Tồn tại lịch làm việc chưa có mã ca. Hãy bổ sung dữ liệu trước khi chuyển đổi.', 1;

    DECLARE @coCaKhongHopLe BIT = 0;
    EXEC sys.sp_executesql
        N'SELECT @ketQua = CASE WHEN EXISTS (
              SELECT 1
              FROM dbo.lich_lam_viec AS llv
              LEFT JOIN dbo.ca_lam AS cl ON cl.id = llv.ca_lam_id
              WHERE cl.id IS NULL
          ) THEN 1 ELSE 0 END;',
        N'@ketQua BIT OUTPUT',
        @ketQua = @coCaKhongHopLe OUTPUT;
    IF @coCaKhongHopLe = 1
        THROW 50004, N'Có giá trị ca cũ không khớp với ca_lam.id. Script đã rollback.', 1;

    DECLARE @coLichTrung BIT = 0;
    EXEC sys.sp_executesql
        N'SELECT @ketQua = CASE WHEN EXISTS (
              SELECT 1
              FROM dbo.lich_lam_viec
              GROUP BY nhan_vien_id, ngay, ca_lam_id
              HAVING COUNT(*) > 1
          ) THEN 1 ELSE 0 END;',
        N'@ketQua BIT OUTPUT',
        @ketQua = @coLichTrung OUTPUT;
    IF @coLichTrung = 1
        THROW 50005, N'Có lịch trùng nhân viên, ngày và ca. Hãy xử lý bản ghi trùng trước khi chuyển đổi.', 1;

    IF EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.lich_lam_viec')
          AND name = N'ix_lich_lam_viec_ngay_ca'
    )
        DROP INDEX ix_lich_lam_viec_ngay_ca ON dbo.lich_lam_viec;

    IF EXISTS (
        SELECT 1 FROM sys.key_constraints
        WHERE parent_object_id = OBJECT_ID(N'dbo.lich_lam_viec')
          AND name = N'uq_lich_lam_viec_nv_ngay'
    )
        ALTER TABLE dbo.lich_lam_viec DROP CONSTRAINT uq_lich_lam_viec_nv_ngay;

    IF EXISTS (
        SELECT 1 FROM sys.check_constraints
        WHERE parent_object_id = OBJECT_ID(N'dbo.lich_lam_viec')
          AND name = N'ck_lich_lam_viec_ca'
    )
        ALTER TABLE dbo.lich_lam_viec DROP CONSTRAINT ck_lich_lam_viec_ca;

    IF COL_LENGTH(N'dbo.lich_lam_viec', N'ca') IS NOT NULL
        ALTER TABLE dbo.lich_lam_viec DROP COLUMN ca;

    IF EXISTS (
        SELECT 1 FROM sys.columns
        WHERE object_id = OBJECT_ID(N'dbo.lich_lam_viec')
          AND name = N'ca_lam_id'
          AND is_nullable = 1
    )
        EXEC sys.sp_executesql N'
            ALTER TABLE dbo.lich_lam_viec
            ALTER COLUMN ca_lam_id VARCHAR(50) NOT NULL;';

    IF NOT EXISTS (
        SELECT 1 FROM sys.foreign_keys
        WHERE parent_object_id = OBJECT_ID(N'dbo.lich_lam_viec')
          AND name = N'fk_lich_lam_viec_ca_lam'
    )
        EXEC sys.sp_executesql N'
            ALTER TABLE dbo.lich_lam_viec WITH CHECK
            ADD CONSTRAINT fk_lich_lam_viec_ca_lam
                FOREIGN KEY (ca_lam_id) REFERENCES dbo.ca_lam(id);';

    IF NOT EXISTS (
        SELECT 1 FROM sys.key_constraints
        WHERE parent_object_id = OBJECT_ID(N'dbo.lich_lam_viec')
          AND name = N'uq_lich_lam_viec_nv_ngay_ca'
    )
        EXEC sys.sp_executesql N'
            ALTER TABLE dbo.lich_lam_viec
            ADD CONSTRAINT uq_lich_lam_viec_nv_ngay_ca
                UNIQUE (nhan_vien_id, ngay, ca_lam_id);';

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.lich_lam_viec')
          AND name = N'ix_lich_lam_viec_ngay_ca'
    )
        EXEC sys.sp_executesql N'
            CREATE INDEX ix_lich_lam_viec_ngay_ca
                ON dbo.lich_lam_viec(ngay, ca_lam_id);';

    COMMIT TRANSACTION;
    PRINT N'Đã chuyển lich_lam_viec sang ca_lam_id thành công.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
