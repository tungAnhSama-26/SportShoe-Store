USE giay;
GO

-- 1. Create table cham_cong (Attendance)
IF OBJECT_ID('cham_cong', 'U') IS NULL
BEGIN
    CREATE TABLE cham_cong (
        id               UNIQUEIDENTIFIER NOT NULL CONSTRAINT pk_cham_cong PRIMARY KEY DEFAULT NEWID(),
        nhan_vien_id     UNIQUEIDENTIFIER NOT NULL,
        lich_lam_viec_id UNIQUEIDENTIFIER NULL,
        ngay             DATE             NOT NULL,
        ca               VARCHAR(10)      NOT NULL,
        thoi_gian_vao    DATETIME         NULL,
        thoi_gian_ra     DATETIME         NULL,
        trang_thai_vao   VARCHAR(20)      NULL, -- 'DUNG_GIO', 'MUON'
        trang_thai_ra    VARCHAR(20)      NULL, -- 'DUNG_GIO', 'SOM'
        ghi_chu          NVARCHAR(255)    NULL,
        CONSTRAINT fk_cham_cong_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id) ON DELETE CASCADE,
        CONSTRAINT fk_cham_cong_lich_lam_viec FOREIGN KEY (lich_lam_viec_id) REFERENCES lich_lam_viec(id) ON DELETE SET NULL,
        CONSTRAINT ck_cham_cong_ca CHECK (ca IN ('sang', 'chieu', 'toi')),
        CONSTRAINT uq_cham_cong_nv_ngay_ca UNIQUE (nhan_vien_id, ngay, ca)
    );
    CREATE INDEX ix_cham_cong_ngay ON cham_cong(ngay);
END;
GO

-- 2. Create table giao_ca (Shift Handover)
IF OBJECT_ID('giao_ca', 'U') IS NULL
BEGIN
    CREATE TABLE giao_ca (
        id                          UNIQUEIDENTIFIER NOT NULL CONSTRAINT pk_giao_ca PRIMARY KEY DEFAULT NEWID(),
        ma                          VARCHAR(50)      NOT NULL UNIQUE,
        nhan_vien_trong_ca_id       UNIQUEIDENTIFIER NOT NULL,
        nhan_vien_nhan_id           UNIQUEIDENTIFIER NULL,
        thoi_gian_vao               DATETIME         NOT NULL,
        thoi_gian_ra                DATETIME         NULL,
        tien_dau_ca                 DECIMAL(18, 2)   NOT NULL,
        tien_mat_trong_ca           DECIMAL(18, 2)   NOT NULL DEFAULT 0,
        tien_chuyen_khoan_trong_ca  DECIMAL(18, 2)   NOT NULL DEFAULT 0,
        tien_cuoi_ca_thuc_te        DECIMAL(18, 2)   NULL,
        tien_cuoi_ca_he_thong       DECIMAL(18, 2)   NULL,
        tien_chenh_lech             DECIMAL(18, 2)   NULL,
        ly_do_chenh_lech            NVARCHAR(255)    NULL,
        trang_thai                  VARCHAR(20)      NOT NULL, -- 'MO_CA', 'CHO_BAN_GIAO', 'DA_BAN_GIAO'
        ghi_chu                     NVARCHAR(255)    NULL,
        CONSTRAINT fk_giao_ca_nhan_vien_trong_ca FOREIGN KEY (nhan_vien_trong_ca_id) REFERENCES nhan_vien(id),
        CONSTRAINT fk_giao_ca_nhan_vien_nhan FOREIGN KEY (nhan_vien_nhan_id) REFERENCES nhan_vien(id),
        CONSTRAINT ck_giao_ca_trang_thai CHECK (trang_thai IN ('MO_CA', 'CHO_BAN_GIAO', 'DA_BAN_GIAO'))
    );
    CREATE INDEX ix_giao_ca_trang_thai ON giao_ca(trang_thai);
END;
GO
