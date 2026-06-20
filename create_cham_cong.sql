USE giay;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'cham_cong')
BEGIN
    CREATE TABLE cham_cong (
        id               UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        nhan_vien_id     UNIQUEIDENTIFIER NOT NULL,
        lich_lam_viec_id UNIQUEIDENTIFIER NULL,
        ngay             DATE             NOT NULL,
        ca               VARCHAR(10)      NOT NULL,
        thoi_gian_vao    DATETIME2        NULL,
        thoi_gian_ra     DATETIME2        NULL,
        trang_thai_vao   VARCHAR(20)      NULL,
        trang_thai_ra    VARCHAR(20)      NULL,
        ghi_chu          NVARCHAR(255)    NULL,
        CONSTRAINT pk_cham_cong PRIMARY KEY (id),
        CONSTRAINT fk_cham_cong_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id) ON DELETE CASCADE,
        CONSTRAINT fk_cham_cong_lich_lam_viec FOREIGN KEY (lich_lam_viec_id) REFERENCES lich_lam_viec(id),
        CONSTRAINT uq_cham_cong_nv_ngay_ca UNIQUE (nhan_vien_id, ngay, ca)
    );
    PRINT 'Table cham_cong created successfully.';
END
ELSE
BEGIN
    PRINT 'Table cham_cong already exists.';
END
GO
