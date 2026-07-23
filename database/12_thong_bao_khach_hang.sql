-- Bảng thông báo cho KHÁCH HÀNG (chuông ở header màn khách).
-- Loại thông báo: DON_HANG (trạng thái đơn đổi), VOUCHER (phiếu mới - công khai hoặc tặng riêng),
--                 GIAM_GIA (đợt giảm giá mới), DANH_GIA (đánh giá bị ẩn).
-- Thông báo quá 3 ngày sẽ bị scheduler xóa tự động.
CREATE TABLE dbo.thong_bao_khach_hang (
    id            INT IDENTITY (1, 1) PRIMARY KEY,
    khach_hang_id UNIQUEIDENTIFIER NOT NULL,
    loai          VARCHAR(30)      NOT NULL,
    tieu_de       NVARCHAR(200)    NOT NULL,
    noi_dung      NVARCHAR(500)    NULL,
    lien_ket      NVARCHAR(200)    NULL,
    da_xem        BIT              NOT NULL DEFAULT 0,
    ngay_tao      DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_tbkh_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES dbo.khach_hang (id)
);
GO

CREATE INDEX ix_tbkh_khach_ngay ON dbo.thong_bao_khach_hang (khach_hang_id, ngay_tao DESC);
GO
