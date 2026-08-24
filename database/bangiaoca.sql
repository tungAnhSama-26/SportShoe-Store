USE giay;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

/* ============================================================
   GIAI ĐOẠN 1: TẠO CÁC CỘT MỚI Ở BATCH RIÊNG
   ============================================================ */

/* Xử lý ca_chua_ket_thuc nếu trước đây là computed column */
IF EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.giao_ca')
      AND name = N'ca_chua_ket_thuc'
      AND is_computed = 1
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.giao_ca')
          AND name = N'ux_giao_ca_mot_ca_chua_ket_thuc'
    )
    BEGIN
        DROP INDEX ux_giao_ca_mot_ca_chua_ket_thuc
        ON dbo.giao_ca;
    END;

    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.giao_ca')
          AND name = N'ux_giao_ca_mot_ca_chua_ket_thuc_theo_nv'
    )
    BEGIN
        DROP INDEX ux_giao_ca_mot_ca_chua_ket_thuc_theo_nv
        ON dbo.giao_ca;
    END;

    IF OBJECT_ID(N'dbo.ck_giao_ca_chua_ket_thuc', N'C') IS NOT NULL
    BEGIN
        ALTER TABLE dbo.giao_ca
        DROP CONSTRAINT ck_giao_ca_chua_ket_thuc;
    END;

    ALTER TABLE dbo.giao_ca
    DROP COLUMN ca_chua_ket_thuc;
END;
GO

IF COL_LENGTH(N'dbo.giao_ca', N'ca_lam_id') IS NULL
BEGIN
    ALTER TABLE dbo.giao_ca
    ADD ca_lam_id VARCHAR(50) NULL;
END;
GO

IF COL_LENGTH(N'dbo.giao_ca', N'thoi_gian_xac_nhan') IS NULL
BEGIN
    ALTER TABLE dbo.giao_ca
    ADD thoi_gian_xac_nhan DATETIME2 NULL;
END;
GO

IF COL_LENGTH(N'dbo.giao_ca', N'tien_nhan_kiem_dem') IS NULL
BEGIN
    ALTER TABLE dbo.giao_ca
    ADD tien_nhan_kiem_dem DECIMAL(18,2) NULL;
END;
GO

IF COL_LENGTH(N'dbo.giao_ca', N'ca_chua_ket_thuc') IS NULL
BEGIN
    ALTER TABLE dbo.giao_ca
    ADD ca_chua_ket_thuc TINYINT NULL;
END;
GO


/* ============================================================
   GIAI ĐOẠN 2: CHUYỂN ĐỔI DỮ LIỆU VÀ CONSTRAINT
   ============================================================ */

BEGIN TRY
    BEGIN TRANSACTION;

    /* --------------------------------------------------------
       1. Kiểm tra bảng bắt buộc
       -------------------------------------------------------- */

    IF OBJECT_ID(N'dbo.giao_ca', N'U') IS NULL
        THROW 50001, N'Không tìm thấy bảng giao_ca.', 1;

    IF OBJECT_ID(N'dbo.ca_lam', N'U') IS NULL
        THROW 50002, N'Không tìm thấy bảng ca_lam.', 1;

    IF NOT EXISTS (SELECT 1 FROM dbo.ca_lam)
        THROW 50003, N'Bảng ca_lam chưa có dữ liệu.', 1;


    /* --------------------------------------------------------
       2. Xóa index cũ
       -------------------------------------------------------- */

    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.giao_ca')
          AND name = N'ux_giao_ca_mot_ca_dang_mo'
    )
    BEGIN
        DROP INDEX ux_giao_ca_mot_ca_dang_mo
        ON dbo.giao_ca;
    END;

    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.giao_ca')
          AND name = N'ux_giao_ca_mot_ca_cho_ban_giao'
    )
    BEGIN
        DROP INDEX ux_giao_ca_mot_ca_cho_ban_giao
        ON dbo.giao_ca;
    END;

    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.giao_ca')
          AND name = N'ux_giao_ca_mot_ca_chua_ket_thuc'
    )
    BEGIN
        DROP INDEX ux_giao_ca_mot_ca_chua_ket_thuc
        ON dbo.giao_ca;
    END;

    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.giao_ca')
          AND name = N'ux_giao_ca_mot_ca_chua_ket_thuc_theo_nv'
    )
    BEGIN
        DROP INDEX ux_giao_ca_mot_ca_chua_ket_thuc_theo_nv
        ON dbo.giao_ca;
    END;


    /* --------------------------------------------------------
       3. Xóa constraint cũ và constraint từ lần chạy trước
       -------------------------------------------------------- */

    IF OBJECT_ID(N'dbo.ck_giao_ca_trang_thai', N'C') IS NOT NULL
    BEGIN
        ALTER TABLE dbo.giao_ca
        DROP CONSTRAINT ck_giao_ca_trang_thai;
    END;

    IF OBJECT_ID(N'dbo.ck_giao_ca_tien', N'C') IS NOT NULL
    BEGIN
        ALTER TABLE dbo.giao_ca
        DROP CONSTRAINT ck_giao_ca_tien;
    END;

    IF OBJECT_ID(N'dbo.ck_giao_ca_chua_ket_thuc', N'C') IS NOT NULL
    BEGIN
        ALTER TABLE dbo.giao_ca
        DROP CONSTRAINT ck_giao_ca_chua_ket_thuc;
    END;

    IF OBJECT_ID(N'dbo.fk_giao_ca_ca_lam', N'F') IS NOT NULL
    BEGIN
        ALTER TABLE dbo.giao_ca
        DROP CONSTRAINT fk_giao_ca_ca_lam;
    END;


    /* --------------------------------------------------------
       4. Chuyển trạng thái DA_DONG cũ
       -------------------------------------------------------- */

    UPDATE dbo.giao_ca
    SET trang_thai = 'DA_KET_THUC'
    WHERE trang_thai = 'DA_DONG';


    /* --------------------------------------------------------
       5. Mỗi nhân viên chỉ giữ một ca mới nhất chưa kết thúc
       Những ca mở cũ hơn của cùng nhân viên được chuyển sang DA_KET_THUC
       -------------------------------------------------------- */

    ;WITH danh_sach_ca AS (
        SELECT
            id,
            ROW_NUMBER() OVER (
                PARTITION BY nhan_vien_trong_ca_id
                ORDER BY thoi_gian_vao DESC, id DESC
            ) AS thu_tu
        FROM dbo.giao_ca
        WHERE trang_thai IN ('MO_CA', 'CHO_BAN_GIAO')
    )
    UPDATE gc
    SET
        gc.trang_thai = 'DA_KET_THUC',
        gc.thoi_gian_ra =
            COALESCE(gc.thoi_gian_ra, SYSDATETIME()),
        gc.nhan_vien_nhan_id = NULL
    FROM dbo.giao_ca gc
    INNER JOIN danh_sach_ca ds
        ON ds.id = gc.id
    WHERE ds.thu_tu > 1;


    /* --------------------------------------------------------
       6. Xác định ca_lam_id cho dữ liệu giao ca cũ
       -------------------------------------------------------- */

    UPDATE gc
    SET gc.ca_lam_id = ca_phu_hop.id
    FROM dbo.giao_ca gc
    CROSS APPLY (
        SELECT TOP (1)
            cl.id
        FROM dbo.ca_lam cl
        ORDER BY
            CASE
                WHEN CAST(gc.thoi_gian_vao AS TIME)
                        >= CAST(cl.gio_bat_dau AS TIME)
                 AND CAST(gc.thoi_gian_vao AS TIME)
                        <= CAST(cl.gio_ket_thuc AS TIME)
                THEN 0
                ELSE 1
            END,
            ABS(
                DATEDIFF(
                    MINUTE,
                    CAST(cl.gio_bat_dau AS TIME),
                    CAST(gc.thoi_gian_vao AS TIME)
                )
            )
    ) ca_phu_hop
    WHERE gc.ca_lam_id IS NULL;


    IF EXISTS (
        SELECT 1
        FROM dbo.giao_ca
        WHERE ca_lam_id IS NULL
    )
    BEGIN
        THROW 50004, N'Có giao ca cũ không xác định được ca làm.', 1;
    END;


    /* --------------------------------------------------------
       7. Đồng bộ dữ liệu xác nhận của lịch sử cũ
       -------------------------------------------------------- */

    UPDATE dbo.giao_ca
    SET
        thoi_gian_xac_nhan =
            CASE
                WHEN trang_thai = 'DA_BAN_GIAO'
                THEN COALESCE(
                    thoi_gian_xac_nhan,
                    thoi_gian_ra
                )
                ELSE thoi_gian_xac_nhan
            END,
        tien_nhan_kiem_dem =
            CASE
                WHEN trang_thai = 'DA_BAN_GIAO'
                THEN COALESCE(
                    tien_nhan_kiem_dem,
                    tien_cuoi_ca_thuc_te
                )
                ELSE tien_nhan_kiem_dem
            END;


    /* --------------------------------------------------------
       8. Đồng bộ ca_chua_ket_thuc
       -------------------------------------------------------- */

    UPDATE dbo.giao_ca
    SET ca_chua_ket_thuc =
        CASE
            WHEN trang_thai IN ('MO_CA', 'CHO_BAN_GIAO')
            THEN 1
            ELSE NULL
        END;


    /* --------------------------------------------------------
       9. Mở rộng ghi chú
       -------------------------------------------------------- */

    ALTER TABLE dbo.giao_ca
    ALTER COLUMN ghi_chu NVARCHAR(500) NULL;


    /* --------------------------------------------------------
       10. ca_lam_id trở thành bắt buộc
       -------------------------------------------------------- */

    ALTER TABLE dbo.giao_ca
    ALTER COLUMN ca_lam_id VARCHAR(50) NOT NULL;


    /* --------------------------------------------------------
       11. Thêm khóa ngoại ca_lam
       -------------------------------------------------------- */

    ALTER TABLE dbo.giao_ca WITH CHECK
    ADD CONSTRAINT fk_giao_ca_ca_lam
        FOREIGN KEY (ca_lam_id)
        REFERENCES dbo.ca_lam(id);

    ALTER TABLE dbo.giao_ca
    CHECK CONSTRAINT fk_giao_ca_ca_lam;


    /* --------------------------------------------------------
       12. Constraint trạng thái
       -------------------------------------------------------- */

    ALTER TABLE dbo.giao_ca WITH CHECK
    ADD CONSTRAINT ck_giao_ca_trang_thai
    CHECK (
        trang_thai IN (
            'MO_CA',
            'CHO_BAN_GIAO',
            'DA_BAN_GIAO',
            'DA_KET_THUC'
        )
    );


    /* --------------------------------------------------------
       13. Constraint tiền
       -------------------------------------------------------- */

    ALTER TABLE dbo.giao_ca WITH CHECK
    ADD CONSTRAINT ck_giao_ca_tien
    CHECK (
        tien_dau_ca >= 0
        AND (
            tien_cuoi_ca_thuc_te IS NULL
            OR tien_cuoi_ca_thuc_te >= 0
        )
        AND (
            tien_nhan_kiem_dem IS NULL
            OR tien_nhan_kiem_dem >= 0
        )
    );


    /* --------------------------------------------------------
       14. Constraint đồng bộ ca chưa kết thúc
       -------------------------------------------------------- */

    ALTER TABLE dbo.giao_ca WITH CHECK
    ADD CONSTRAINT ck_giao_ca_chua_ket_thuc
    CHECK (
        (
            trang_thai IN ('MO_CA', 'CHO_BAN_GIAO')
            AND ca_chua_ket_thuc = 1
        )
        OR
        (
            trang_thai IN ('DA_BAN_GIAO', 'DA_KET_THUC')
            AND ca_chua_ket_thuc IS NULL
        )
    );


    /* --------------------------------------------------------
       15. Unique filtered index
       -------------------------------------------------------- */

    CREATE UNIQUE INDEX ux_giao_ca_mot_ca_chua_ket_thuc_theo_nv
    ON dbo.giao_ca(nhan_vien_trong_ca_id)
    WHERE ca_chua_ket_thuc = 1;


    /* --------------------------------------------------------
       16. Index ca làm và trạng thái
       -------------------------------------------------------- */

    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.giao_ca')
          AND name = N'ix_giao_ca_ca_lam'
    )
    BEGIN
        CREATE INDEX ix_giao_ca_ca_lam
        ON dbo.giao_ca(ca_lam_id, thoi_gian_vao);
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.giao_ca')
          AND name = N'ix_giao_ca_trang_thai_thoi_gian'
    )
    BEGIN
        CREATE INDEX ix_giao_ca_trang_thai_thoi_gian
        ON dbo.giao_ca(trang_thai, thoi_gian_vao DESC);
    END;


    /* --------------------------------------------------------
       17. Xóa default constraint của các cột tiền cũ
       -------------------------------------------------------- */

    DECLARE @sqlXoaDefault NVARCHAR(MAX) = N'';

    SELECT
        @sqlXoaDefault =
            @sqlXoaDefault
            + N'ALTER TABLE dbo.giao_ca DROP CONSTRAINT '
            + QUOTENAME(dc.name)
            + N';'
    FROM sys.default_constraints dc
    INNER JOIN sys.columns c
        ON c.object_id = dc.parent_object_id
       AND c.column_id = dc.parent_column_id
    WHERE dc.parent_object_id = OBJECT_ID(N'dbo.giao_ca')
      AND c.name IN (
          N'tien_mat_trong_ca',
          N'tien_chuyen_khoan_trong_ca',
          N'tien_cuoi_ca_he_thong',
          N'tien_chenh_lech'
      );

    IF @sqlXoaDefault <> N''
    BEGIN
        EXEC sys.sp_executesql @sqlXoaDefault;
    END;


    /* --------------------------------------------------------
       18. Xóa các cột tiền dẫn xuất cũ bằng SQL động
       -------------------------------------------------------- */

    DECLARE @sqlXoaCot NVARCHAR(MAX) = N'';

    IF COL_LENGTH(N'dbo.giao_ca', N'tien_mat_trong_ca') IS NOT NULL
        SET @sqlXoaCot +=
            N'ALTER TABLE dbo.giao_ca DROP COLUMN tien_mat_trong_ca;';

    IF COL_LENGTH(
        N'dbo.giao_ca',
        N'tien_chuyen_khoan_trong_ca'
    ) IS NOT NULL
        SET @sqlXoaCot +=
            N'ALTER TABLE dbo.giao_ca DROP COLUMN tien_chuyen_khoan_trong_ca;';

    IF COL_LENGTH(
        N'dbo.giao_ca',
        N'tien_cuoi_ca_he_thong'
    ) IS NOT NULL
        SET @sqlXoaCot +=
            N'ALTER TABLE dbo.giao_ca DROP COLUMN tien_cuoi_ca_he_thong;';

    IF COL_LENGTH(
        N'dbo.giao_ca',
        N'tien_chenh_lech'
    ) IS NOT NULL
        SET @sqlXoaCot +=
            N'ALTER TABLE dbo.giao_ca DROP COLUMN tien_chenh_lech;';

    IF @sqlXoaCot <> N''
    BEGIN
        EXEC sys.sp_executesql @sqlXoaCot;
    END;


    COMMIT TRANSACTION;

    PRINT N'================================================';
    PRINT N'NÂNG CẤP BẢNG GIAO_CA THÀNH CÔNG';
    PRINT N'Không xóa database và không xóa dữ liệu hệ thống';
    PRINT N'================================================';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    SELECT
        ERROR_NUMBER() AS ma_loi,
        ERROR_LINE() AS dong_loi,
        ERROR_MESSAGE() AS noi_dung_loi;

    THROW;
END CATCH;
GO


/* ============================================================
   GIAI ĐOẠN 3: KIỂM TRA KẾT QUẢ
   ============================================================ */

SELECT
    c.name AS ten_cot,
    TYPE_NAME(c.user_type_id) AS kieu_du_lieu,
    c.max_length,
    c.is_nullable,
    c.is_computed
FROM sys.columns c
WHERE c.object_id = OBJECT_ID(N'dbo.giao_ca')
ORDER BY c.column_id;
GO

SELECT
    ma,
    ca_lam_id,
    nhan_vien_trong_ca_id,
    nhan_vien_nhan_id,
    thoi_gian_vao,
    thoi_gian_ra,
    thoi_gian_xac_nhan,
    tien_dau_ca,
    tien_cuoi_ca_thuc_te,
    tien_nhan_kiem_dem,
    trang_thai,
    ca_chua_ket_thuc,
    ghi_chu
FROM dbo.giao_ca
ORDER BY thoi_gian_vao DESC;
GO
