-- ============================================================
-- Bảng đánh giá sản phẩm của khách hàng (storefront).
-- DB: SQL Server. Chạy thủ công vì spring.jpa.hibernate.ddl-auto=none.
-- Script idempotent.
-- ============================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'danh_gia')
BEGIN
    CREATE TABLE danh_gia (
        id INT IDENTITY(1,1) PRIMARY KEY,
        giay_id INT NOT NULL,
        khach_hang_id UNIQUEIDENTIFIER NOT NULL,
        so_sao INT NOT NULL,
        noi_dung NVARCHAR(1000) NULL,
        trang_thai INT NOT NULL CONSTRAINT df_danhgia_trang_thai DEFAULT 1,
        ngay_tao DATETIME2 NOT NULL CONSTRAINT df_danhgia_ngay_tao DEFAULT SYSDATETIME(),
        ngay_cap_nhat DATETIME2 NULL,
        CONSTRAINT fk_danhgia_giay FOREIGN KEY (giay_id) REFERENCES giay(id),
        CONSTRAINT fk_danhgia_khachhang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id),
        CONSTRAINT ck_danhgia_sosao CHECK (so_sao BETWEEN 1 AND 5)
    );
END;
GO

-- Dữ liệu mẫu để có đánh giá hiển thị ngay (chỉ thêm khi bảng còn trống).
DECLARE @kh1 UNIQUEIDENTIFIER, @kh2 UNIQUEIDENTIFIER;
SELECT TOP 1 @kh1 = id FROM khach_hang ORDER BY ngay_tao;
SELECT TOP 1 @kh2 = id FROM khach_hang WHERE id <> @kh1 ORDER BY ngay_tao;

IF @kh1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM danh_gia)
BEGIN
    INSERT INTO danh_gia (giay_id, khach_hang_id, so_sao, noi_dung) VALUES
        (0, @kh1, 5, N'Giày đẹp, đi rất êm chân và đúng size. Rất hài lòng!'),
        (0, COALESCE(@kh2, @kh1), 4, N'Chất lượng tốt, giao hàng nhanh, sẽ ủng hộ tiếp.'),
        (1, @kh1, 5, N'Mẫu này lên chân đẹp, đáng tiền.');
END;
GO
