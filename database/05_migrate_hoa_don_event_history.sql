SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.hoa_don', N'U') IS NULL
        THROW 50003, N'Không tìm thấy bảng dbo.hoa_don.', 1;

    IF OBJECT_ID(N'dbo.lich_su_hoa_don', N'U') IS NULL
        THROW 50004, N'Không tìm thấy bảng dbo.lich_su_hoa_don.', 1;

    -- Chuyển cờ khách đã nhận hàng thành sự kiện, không tạo trùng khi chạy lại.
    IF COL_LENGTH(N'dbo.hoa_don', N'da_nhan_hang') IS NOT NULL
    BEGIN
        EXEC sp_executesql N'
            INSERT INTO dbo.lich_su_hoa_don
                (hoa_don_id, nhan_vien_id, trang_thai, ghi_chu, nguoi_thao_tac, ngay_tao)
            SELECT hd.id, NULL, N''KHACH_DA_NHAN_HANG'',
                   N''Chuyển đổi từ dữ liệu xác nhận đã nhận hàng cũ'',
                   N''Hệ thống'', COALESCE(hd.ngay_cap_nhat, hd.ngay_tao, SYSUTCDATETIME())
            FROM dbo.hoa_don hd
            WHERE hd.da_nhan_hang = 1
              AND NOT EXISTS (
                  SELECT 1
                  FROM dbo.lich_su_hoa_don ls
                  WHERE ls.hoa_don_id = hd.id
                    AND ls.trang_thai = N''KHACH_DA_NHAN_HANG''
              );
        ';
    END;

    -- Chuyển lượt khách sửa địa chỉ thành sự kiện. Giới hạn nghiệp vụ hiện tại là một lần.
    IF COL_LENGTH(N'dbo.hoa_don', N'so_lan_sua_dia_chi') IS NOT NULL
    BEGIN
        EXEC sp_executesql N'
            INSERT INTO dbo.lich_su_hoa_don
                (hoa_don_id, nhan_vien_id, trang_thai, ghi_chu, nguoi_thao_tac, ngay_tao)
            SELECT hd.id, NULL, N''KHACH_SUA_DIA_CHI'',
                   N''Chuyển đổi từ dữ liệu lượt sửa địa chỉ cũ'',
                   N''Khách hàng'', COALESCE(hd.ngay_cap_nhat, hd.ngay_tao, SYSUTCDATETIME())
            FROM dbo.hoa_don hd
            WHERE hd.so_lan_sua_dia_chi > 0
              AND NOT EXISTS (
                  SELECT 1
                  FROM dbo.lich_su_hoa_don ls
                  WHERE ls.hoa_don_id = hd.id
                    AND ls.trang_thai = N''KHACH_SUA_DIA_CHI''
              );
        ';
    END;

    -- Chuẩn hóa các nhãn trạng thái cũ thành mã ổn định.
    UPDATE dbo.lich_su_hoa_don
    SET trang_thai = CASE trang_thai
        WHEN N'Chờ xác nhận' THEN N'CHO_XAC_NHAN'
        WHEN N'Đã xác nhận' THEN N'DA_XAC_NHAN'
        WHEN N'Chờ lấy hàng' THEN N'CHO_LAY_HANG'
        WHEN N'Chờ giao hàng' THEN N'DANG_GIAO_HANG'
        WHEN N'Đang giao hàng' THEN N'DANG_GIAO_HANG'
        WHEN N'Đã giao hàng' THEN N'DA_GIAO_HANG'
        WHEN N'Hoàn thành' THEN N'HOAN_THANH'
        WHEN N'Hủy' THEN N'HUY'
        WHEN N'Yêu cầu hủy' THEN N'YEU_CAU_HUY'
        WHEN N'Giao hàng thất bại' THEN N'GIAO_HANG_THAT_BAI'
        WHEN N'Hóa đơn chờ' THEN N'HOA_DON_CHO'
        ELSE trang_thai
    END
    WHERE trang_thai IN (
        N'Chờ xác nhận', N'Đã xác nhận', N'Chờ lấy hàng', N'Chờ giao hàng',
        N'Đang giao hàng', N'Đã giao hàng', N'Hoàn thành', N'Hủy',
        N'Yêu cầu hủy', N'Giao hàng thất bại', N'Hóa đơn chờ'
    );

    -- Bảo toàn trạng thái trước yêu cầu hủy từ cột cũ bằng một sự kiện trạng thái ổn định mới nhất.
    IF COL_LENGTH(N'dbo.hoa_don', N'trang_thai_truoc_yeu_cau_huy') IS NOT NULL
    BEGIN
        EXEC sp_executesql N'
            INSERT INTO dbo.lich_su_hoa_don
                (hoa_don_id, nhan_vien_id, trang_thai, ghi_chu, nguoi_thao_tac, ngay_tao)
            SELECT hd.id, hd.nhan_vien_id,
                   CASE hd.trang_thai_truoc_yeu_cau_huy
                       WHEN 1 THEN N''CHO_XAC_NHAN''
                       WHEN 2 THEN N''CHO_LAY_HANG''
                       WHEN 3 THEN N''DANG_GIAO_HANG''
                       WHEN 4 THEN N''DA_GIAO_HANG''
                       WHEN 5 THEN N''HOAN_THANH''
                       WHEN 6 THEN N''HUY''
                       WHEN 9 THEN N''DA_XAC_NHAN''
                       WHEN 10 THEN N''GIAO_HANG_THAT_BAI''
                       WHEN 11 THEN N''HOA_DON_CHO''
                   END,
                   N''Trạng thái trước yêu cầu hủy được chuyển từ dữ liệu cũ'',
                   N''Hệ thống'', SYSUTCDATETIME()
            FROM dbo.hoa_don hd
            WHERE hd.trang_thai = 7
              AND hd.trang_thai_truoc_yeu_cau_huy IN (1, 2, 3, 4, 5, 6, 9, 10, 11);

            INSERT INTO dbo.lich_su_hoa_don
                (hoa_don_id, nhan_vien_id, trang_thai, ghi_chu, nguoi_thao_tac, ngay_tao)
            SELECT hd.id, hd.nhan_vien_id, N''YEU_CAU_HUY'',
                   N''Yêu cầu hủy được chuyển từ dữ liệu cũ'',
                   N''Hệ thống'', SYSUTCDATETIME()
            FROM dbo.hoa_don hd
            WHERE hd.trang_thai = 7
              AND NOT EXISTS (
                  SELECT 1
                  FROM dbo.lich_su_hoa_don ls
                  WHERE ls.hoa_don_id = hd.id
                    AND ls.trang_thai = N''YEU_CAU_HUY''
              );
        ';
    END;

    -- Gỡ default constraints của các cột sắp xóa.
    DECLARE @sql NVARCHAR(MAX) = N'';
    SELECT @sql = @sql
        + N'ALTER TABLE dbo.hoa_don DROP CONSTRAINT ' + QUOTENAME(dc.name) + N';'
    FROM sys.default_constraints dc
    INNER JOIN sys.columns c
        ON c.object_id = dc.parent_object_id
       AND c.column_id = dc.parent_column_id
    WHERE dc.parent_object_id = OBJECT_ID(N'dbo.hoa_don')
      AND c.name IN (
          N'han_giu_hang', N'da_nhan_hang',
          N'so_lan_sua_dia_chi', N'trang_thai_truoc_yeu_cau_huy'
      );

    IF @sql <> N''
        EXEC sp_executesql @sql;

    IF COL_LENGTH(N'dbo.hoa_don', N'han_giu_hang') IS NOT NULL
        ALTER TABLE dbo.hoa_don DROP COLUMN han_giu_hang;

    IF COL_LENGTH(N'dbo.hoa_don', N'da_nhan_hang') IS NOT NULL
        ALTER TABLE dbo.hoa_don DROP COLUMN da_nhan_hang;

    IF COL_LENGTH(N'dbo.hoa_don', N'so_lan_sua_dia_chi') IS NOT NULL
        ALTER TABLE dbo.hoa_don DROP COLUMN so_lan_sua_dia_chi;

    IF COL_LENGTH(N'dbo.hoa_don', N'trang_thai_truoc_yeu_cau_huy') IS NOT NULL
        ALTER TABLE dbo.hoa_don DROP COLUMN trang_thai_truoc_yeu_cau_huy;

    IF EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.lich_su_hoa_don')
          AND name = N'ix_ls_hd_hoa_don'
    )
        DROP INDEX ix_ls_hd_hoa_don ON dbo.lich_su_hoa_don;

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.lich_su_hoa_don')
          AND name = N'ix_ls_hd_su_kien'
    )
        CREATE INDEX ix_ls_hd_su_kien
            ON dbo.lich_su_hoa_don(hoa_don_id, trang_thai, ngay_tao DESC, id DESC);

    COMMIT TRANSACTION;
    PRINT N'Đã chuyển trạng thái phụ của hóa đơn sang lịch sử sự kiện.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
