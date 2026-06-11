USE giay;
GO

IF OBJECT_ID('lich_lam_viec', 'U') IS NULL
BEGIN
    CREATE TABLE lich_lam_viec (
        id             UNIQUEIDENTIFIER NOT NULL CONSTRAINT pk_lich_lam_viec PRIMARY KEY DEFAULT NEWID(),
        nhan_vien_id   UNIQUEIDENTIFIER NOT NULL,
        ngay           DATE             NOT NULL,
        ca             VARCHAR(10)      NOT NULL,
        CONSTRAINT uq_lich_lam_viec_nv_ngay UNIQUE (nhan_vien_id, ngay),
        CONSTRAINT ck_lich_lam_viec_ca CHECK (ca IN ('sang', 'chieu', 'toi')),
        CONSTRAINT fk_lich_lam_viec_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id) ON DELETE CASCADE
    );
    CREATE INDEX ix_lich_lam_viec_ngay_ca ON lich_lam_viec(ngay, ca);
END;
GO
