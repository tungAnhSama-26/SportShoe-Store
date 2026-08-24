/*
    Cho phép nhiều nhân viên có ca đang hoạt động cùng lúc, nhưng mỗi nhân viên
    chỉ được có một ca chưa kết thúc.

    Có thể chạy lại script an toàn trên database đã triển khai.
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.giao_ca', N'U') IS NULL
        THROW 50001, N'Không tìm thấy bảng dbo.giao_ca.', 1;

    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.giao_ca')
          AND name = N'ux_giao_ca_mot_ca_chua_ket_thuc'
    )
        DROP INDEX ux_giao_ca_mot_ca_chua_ket_thuc ON dbo.giao_ca;

    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.giao_ca')
          AND name = N'ux_giao_ca_mot_ca_chua_ket_thuc_theo_nv'
    )
        DROP INDEX ux_giao_ca_mot_ca_chua_ket_thuc_theo_nv ON dbo.giao_ca;

    CREATE UNIQUE INDEX ux_giao_ca_mot_ca_chua_ket_thuc_theo_nv
        ON dbo.giao_ca(nhan_vien_trong_ca_id)
        WHERE ca_chua_ket_thuc = 1;

    COMMIT TRANSACTION;
    PRINT N'Đã cho phép nhiều nhân viên mở ca đồng thời.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
