USE master;
GO

IF DB_ID(N'giay') IS NOT NULL
BEGIN
    ALTER DATABASE giay SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE giay;
END;
GO

CREATE DATABASE giay;
GO

USE giay;
GO

SET NOCOUNT ON;
GO

-- ============================================================
-- SPORTSHOE STORE - SCHEMA + RULES
-- Run this file first, then run: 02_seed_full_10_records.sql
--
-- Common status rules:
-- 0 = inactive/locked/cancelled where applicable
-- 1 = active/valid/default running state where applicable
-- ============================================================

CREATE TABLE nhan_vien (
    id             UNIQUEIDENTIFIER NOT NULL CONSTRAINT pk_nhan_vien PRIMARY KEY DEFAULT NEWID(),
    ma             VARCHAR(20)      NOT NULL,
    ten_dang_nhap  VARCHAR(50)      NOT NULL,
    ho_ten         NVARCHAR(100)    NOT NULL,
    email          VARCHAR(100)     NOT NULL,
    mat_khau       VARCHAR(255)     NOT NULL,
    sdt            VARCHAR(20)      NULL,
    cccd           VARCHAR(12)      NULL,
    gioi_tinh      NVARCHAR(10)     NULL,
    ngay_sinh      DATE             NULL,
    dia_chi        NVARCHAR(200)    NULL,
    vai_tro        INT              NOT NULL CONSTRAINT df_nhan_vien_vai_tro DEFAULT 2,
    trang_thai     INT              NOT NULL CONSTRAINT df_nhan_vien_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2        NOT NULL CONSTRAINT df_nhan_vien_ngay_tao DEFAULT SYSDATETIME(),
    hinh_anh       NVARCHAR(500)    NULL,
    ngay_cap_nhat  DATETIME2        NULL,
    CONSTRAINT uq_nhan_vien_ma UNIQUE (ma),
    CONSTRAINT uq_nhan_vien_ten_dang_nhap UNIQUE (ten_dang_nhap),
    CONSTRAINT uq_nhan_vien_email UNIQUE (email),
    CONSTRAINT ck_nhan_vien_vai_tro CHECK (vai_tro IN (1, 2, 3)),
    CONSTRAINT ck_nhan_vien_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT ck_nhan_vien_cccd CHECK (cccd IS NULL OR (LEN(cccd) = 12 AND cccd NOT LIKE '%[^0-9]%'))
);
GO

CREATE UNIQUE INDEX ux_nhan_vien_cccd_not_null ON nhan_vien(cccd) WHERE cccd IS NOT NULL;
GO

CREATE TABLE khach_hang (
    id             UNIQUEIDENTIFIER NOT NULL CONSTRAINT pk_khach_hang PRIMARY KEY DEFAULT NEWID(),
    ten_dang_nhap  VARCHAR(50)      NOT NULL,
    ho_ten         NVARCHAR(100)    NOT NULL,
    email          VARCHAR(100)     NULL,
    sdt            VARCHAR(20)      NULL,
    ngay_sinh      DATE             NULL,
    hinh_anh       NVARCHAR(500)    NULL,
    mat_khau       VARCHAR(255)     NOT NULL,
    trang_thai     INT              NOT NULL CONSTRAINT df_khach_hang_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2        NOT NULL CONSTRAINT df_khach_hang_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2        NULL,
    CONSTRAINT uq_khach_hang_ten_dang_nhap UNIQUE (ten_dang_nhap),
    CONSTRAINT uq_khach_hang_email UNIQUE (email),
    CONSTRAINT ck_khach_hang_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE dia_chi_khach_hang (
    id              INT              NOT NULL CONSTRAINT pk_dia_chi_khach_hang PRIMARY KEY IDENTITY(1,1),
    khach_hang_id   UNIQUEIDENTIFIER NOT NULL,
    ho_ten          NVARCHAR(100)    NOT NULL,
    sdt             VARCHAR(20)      NOT NULL,
    tinh_thanh      NVARCHAR(100)    NOT NULL,
    quan_huyen      NVARCHAR(100)    NOT NULL,
    phuong_xa       NVARCHAR(100)    NOT NULL,
    dia_chi_cu_the  NVARCHAR(300)    NOT NULL,
    la_mac_dinh     BIT              NOT NULL CONSTRAINT df_dc_kh_la_mac_dinh DEFAULT 0,
    trang_thai      INT              NOT NULL CONSTRAINT df_dc_kh_trang_thai DEFAULT 1,
    ngay_tao        DATETIME2        NOT NULL CONSTRAINT df_dc_kh_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat   DATETIME2        NULL,
    CONSTRAINT ck_dc_kh_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT fk_dc_kh_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE CASCADE
);
GO

CREATE TABLE kich_co (
    id             INT           NOT NULL CONSTRAINT pk_kich_co PRIMARY KEY IDENTITY(1,1),
    gia_tri        NVARCHAR(20)  NOT NULL,
    ghi_chu        NVARCHAR(200) NULL,
    trang_thai     INT           NOT NULL CONSTRAINT df_kich_co_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_kich_co_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_kich_co_gia_tri UNIQUE (gia_tri),
    CONSTRAINT ck_kich_co_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE mau_sac (
    id             INT           NOT NULL CONSTRAINT pk_mau_sac PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(50)  NOT NULL,
    ten            NVARCHAR(100) NOT NULL,
    ma_mau_hex     NVARCHAR(7)   NULL,
    trang_thai     INT           NOT NULL CONSTRAINT df_mau_sac_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_mau_sac_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_mau_sac_ma UNIQUE (ma),
    CONSTRAINT uq_mau_sac_ten UNIQUE (ten),
    CONSTRAINT ck_mau_sac_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT ck_mau_sac_ma_hex CHECK (
        ma_mau_hex IS NULL OR
        ma_mau_hex LIKE '#[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]'
    )
);
GO

CREATE TABLE thuong_hieu (
    id             INT           NOT NULL CONSTRAINT pk_thuong_hieu PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(50)  NOT NULL,
    ten            NVARCHAR(200) NOT NULL,
    xuat_xu        NVARCHAR(100) NULL,
    mo_ta          NVARCHAR(500) NULL,
    logo_url       NVARCHAR(500) NULL,
    website        NVARCHAR(300) NULL,
    trang_thai     INT           NOT NULL CONSTRAINT df_thuong_hieu_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_thuong_hieu_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_thuong_hieu_ma UNIQUE (ma),
    CONSTRAINT uq_thuong_hieu_ten UNIQUE (ten),
    CONSTRAINT ck_thuong_hieu_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE loai_giay (
    id             INT           NOT NULL CONSTRAINT pk_loai_giay PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(50)  NOT NULL,
    ten            NVARCHAR(200) NOT NULL,
    mo_ta          NVARCHAR(500) NULL,
    trang_thai     INT           NOT NULL CONSTRAINT df_loai_giay_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_loai_giay_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_loai_giay_ma UNIQUE (ma),
    CONSTRAINT uq_loai_giay_ten UNIQUE (ten),
    CONSTRAINT ck_loai_giay_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE de_giay (
    id             INT           NOT NULL CONSTRAINT pk_de_giay PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(50)  NOT NULL,
    ten            NVARCHAR(100) NOT NULL,
    mo_ta          NVARCHAR(300) NULL,
    trang_thai     INT           NOT NULL CONSTRAINT df_de_giay_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_de_giay_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_de_giay_ma UNIQUE (ma),
    CONSTRAINT uq_de_giay_ten UNIQUE (ten),
    CONSTRAINT ck_de_giay_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE co_giay (
    id             INT           NOT NULL CONSTRAINT pk_co_giay PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(50)  NOT NULL,
    ten            NVARCHAR(100) NOT NULL,
    mo_ta          NVARCHAR(300) NULL,
    trang_thai     INT           NOT NULL CONSTRAINT df_co_giay_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_co_giay_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_co_giay_ma UNIQUE (ma),
    CONSTRAINT uq_co_giay_ten UNIQUE (ten),
    CONSTRAINT ck_co_giay_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE chat_lieu_giay (
    id             INT           NOT NULL CONSTRAINT pk_chat_lieu_giay PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(50)  NOT NULL,
    ten            NVARCHAR(100) NOT NULL,
    mo_ta          NVARCHAR(300) NULL,
    trang_thai     INT           NOT NULL CONSTRAINT df_chat_lieu_giay_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_chat_lieu_giay_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_chat_lieu_giay_ma UNIQUE (ma),
    CONSTRAINT uq_chat_lieu_giay_ten UNIQUE (ten),
    CONSTRAINT ck_chat_lieu_giay_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE trong_luong (
    id             INT           NOT NULL CONSTRAINT pk_trong_luong PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(50)  NOT NULL,
    gia_tri        INT           NOT NULL,
    mo_ta          NVARCHAR(300) NULL,
    trang_thai     INT           NOT NULL CONSTRAINT df_trong_luong_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_trong_luong_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_trong_luong_ma UNIQUE (ma),
    CONSTRAINT uq_trong_luong_gia_tri UNIQUE (gia_tri),
    CONSTRAINT ck_trong_luong_gia_tri CHECK (gia_tri > 0),
    CONSTRAINT ck_trong_luong_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE cong_nghe_dem (
    id             INT           NOT NULL CONSTRAINT pk_cong_nghe_dem PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(50)  NOT NULL,
    ten            NVARCHAR(200) NOT NULL,
    mo_ta          NVARCHAR(500) NULL,
    trang_thai     INT           NOT NULL CONSTRAINT df_cong_nghe_dem_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_cong_nghe_dem_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_cong_nghe_dem_ma UNIQUE (ma),
    CONSTRAINT uq_cong_nghe_dem_ten UNIQUE (ten),
    CONSTRAINT ck_cong_nghe_dem_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE dot_giam_gia (
    id             INT           NOT NULL CONSTRAINT pk_dot_giam_gia PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(100) NOT NULL,
    ten            NVARCHAR(200) NOT NULL,
    mo_ta          NVARCHAR(500) NULL,
    loai_giam      INT           NOT NULL,
    gia_tri_giam   DECIMAL(18,2) NOT NULL,
    ngay_bat_dau   DATETIME2     NULL,
    ngay_ket_thuc  DATETIME2     NULL,
    kich_hoat      INT           NOT NULL CONSTRAINT df_dgg_kich_hoat DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_dgg_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_dot_giam_gia_ma UNIQUE (ma),
    CONSTRAINT ck_dot_giam_gia_loai CHECK (loai_giam = 1),
    CONSTRAINT ck_dot_giam_gia_gia_tri CHECK (gia_tri_giam > 0 AND gia_tri_giam <= 100),
    CONSTRAINT ck_dot_giam_gia_kich_hoat CHECK (kich_hoat IN (0, 1, 2, 4)),
    CONSTRAINT ck_dot_giam_gia_thoi_gian CHECK (
        ngay_ket_thuc IS NULL OR ngay_bat_dau IS NULL OR ngay_ket_thuc >= ngay_bat_dau
    )
);
GO

CREATE TABLE giay (
    id             INT           NOT NULL CONSTRAINT pk_giay PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(100) NOT NULL,
    ten            NVARCHAR(300) NOT NULL,
    thuong_hieu_id INT           NOT NULL,
    loai_giay_id   INT           NOT NULL,
    gioi_tinh      INT           NULL,
    chat_lieu      NVARCHAR(100) NULL,
    mo_ta          NVARCHAR(MAX) NULL,
    trang_thai     INT           NOT NULL CONSTRAINT df_giay_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_giay_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_giay_ma UNIQUE (ma),
    CONSTRAINT ck_giay_gioi_tinh CHECK (gioi_tinh IS NULL OR gioi_tinh IN (1, 2, 3)),
    CONSTRAINT ck_giay_trang_thai CHECK (trang_thai IN (0, 1, 2)),
    CONSTRAINT fk_giay_thuong_hieu FOREIGN KEY (thuong_hieu_id) REFERENCES thuong_hieu(id),
    CONSTRAINT fk_giay_loai_giay FOREIGN KEY (loai_giay_id) REFERENCES loai_giay(id)
);
GO

CREATE TABLE giay_thuoc_tinh (
    id                 INT       NOT NULL CONSTRAINT pk_giay_thuoc_tinh PRIMARY KEY IDENTITY(1,1),
    giay_id            INT       NOT NULL,
    de_giay_id         INT       NULL,
    co_giay_id         INT       NULL,
    chat_lieu_giay_id  INT       NULL,
    trong_luong_id     INT       NULL,
    cong_nghe_dem_id   INT       NULL,
    trang_thai         INT       NOT NULL CONSTRAINT df_giay_tt_trang_thai DEFAULT 1,
    ngay_tao           DATETIME2 NOT NULL CONSTRAINT df_giay_tt_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat      DATETIME2 NULL,
    CONSTRAINT uq_giay_thuoc_tinh_giay UNIQUE (giay_id),
    CONSTRAINT ck_giay_tt_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT fk_giay_tt_giay FOREIGN KEY (giay_id) REFERENCES giay(id) ON DELETE CASCADE,
    CONSTRAINT fk_giay_tt_de_giay FOREIGN KEY (de_giay_id) REFERENCES de_giay(id),
    CONSTRAINT fk_giay_tt_co_giay FOREIGN KEY (co_giay_id) REFERENCES co_giay(id),
    CONSTRAINT fk_giay_tt_chat_lieu FOREIGN KEY (chat_lieu_giay_id) REFERENCES chat_lieu_giay(id),
    CONSTRAINT fk_giay_tt_trong_luong FOREIGN KEY (trong_luong_id) REFERENCES trong_luong(id),
    CONSTRAINT fk_giay_tt_cong_nghe FOREIGN KEY (cong_nghe_dem_id) REFERENCES cong_nghe_dem(id)
);
GO

CREATE TABLE giay_chi_tiet (
    id             INT           NOT NULL CONSTRAINT pk_giay_chi_tiet PRIMARY KEY IDENTITY(1,1),
    giay_id        INT           NOT NULL,
    ma_bien_the    NVARCHAR(150) NOT NULL,
    mau_sac_id     INT           NOT NULL,
    kich_co_id     INT           NOT NULL,
    so_luong       INT           NOT NULL CONSTRAINT df_gct_so_luong DEFAULT 0,
    gia_goc        DECIMAL(18,2) NOT NULL,
    gia_ban        DECIMAL(18,2) NOT NULL,
    sku            NVARCHAR(150) NOT NULL,
    kich_hoat      INT           NOT NULL CONSTRAINT df_gct_kich_hoat DEFAULT 1,
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_gct_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_gct_ma_bien_the UNIQUE (ma_bien_the),
    CONSTRAINT uq_gct_sku UNIQUE (sku),
    CONSTRAINT uq_gct_to_hop UNIQUE (giay_id, mau_sac_id, kich_co_id),
    CONSTRAINT ck_gct_so_luong CHECK (so_luong >= 0),
    CONSTRAINT ck_gct_gia_goc CHECK (gia_goc > 0),
    CONSTRAINT ck_gct_gia_ban CHECK (gia_ban > 0),
    CONSTRAINT ck_gct_kich_hoat CHECK (kich_hoat IN (0, 1)),
    CONSTRAINT fk_gct_giay FOREIGN KEY (giay_id) REFERENCES giay(id) ON DELETE CASCADE,
    CONSTRAINT fk_gct_mau_sac FOREIGN KEY (mau_sac_id) REFERENCES mau_sac(id),
    CONSTRAINT fk_gct_kich_co FOREIGN KEY (kich_co_id) REFERENCES kich_co(id)
);
GO

CREATE TABLE hinh_anh_giay (
    id                INT            NOT NULL CONSTRAINT pk_hinh_anh_giay PRIMARY KEY IDENTITY(1,1),
    giay_chi_tiet_id  INT            NOT NULL,
    loai_hinh         INT            NOT NULL,
    url               NVARCHAR(1000) NOT NULL,
    mo_ta             NVARCHAR(300)  NULL,
    la_hinh_chinh     BIT            NOT NULL CONSTRAINT df_hag_la_hinh_chinh DEFAULT 0,
    trang_thai        INT            NOT NULL CONSTRAINT df_hag_trang_thai DEFAULT 1,
    ngay_tao          DATETIME2      NOT NULL CONSTRAINT df_hag_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat     DATETIME2      NULL,
    CONSTRAINT ck_hag_loai_hinh CHECK (loai_hinh IN (1, 2, 3)),
    CONSTRAINT ck_hag_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT fk_hag_gct FOREIGN KEY (giay_chi_tiet_id) REFERENCES giay_chi_tiet(id) ON DELETE CASCADE
);
GO

CREATE TABLE dot_giam_gia_san_pham (
    id                INT       NOT NULL CONSTRAINT pk_dgg_sp PRIMARY KEY IDENTITY(1,1),
    dot_giam_gia_id   INT       NOT NULL,
    giay_chi_tiet_id  INT       NOT NULL,
    trang_thai        INT       NOT NULL CONSTRAINT df_dgg_sp_trang_thai DEFAULT 1,
    ngay_tao          DATETIME2 NOT NULL CONSTRAINT df_dgg_sp_ngay_tao DEFAULT SYSDATETIME(),
    CONSTRAINT uq_dgg_sp UNIQUE (dot_giam_gia_id, giay_chi_tiet_id),
    CONSTRAINT ck_dgg_sp_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT fk_dgg_sp_dot_giam_gia FOREIGN KEY (dot_giam_gia_id) REFERENCES dot_giam_gia(id),
    CONSTRAINT fk_dgg_sp_gct FOREIGN KEY (giay_chi_tiet_id) REFERENCES giay_chi_tiet(id) ON DELETE CASCADE
);
GO

CREATE TABLE phieu_giam_gia (
    id                 INT           NOT NULL CONSTRAINT pk_phieu_giam_gia PRIMARY KEY IDENTITY(1,1),
    ma                 NVARCHAR(100) NOT NULL,
    ten                NVARCHAR(200) NOT NULL,
    loai               INT           NOT NULL,
    loai_phieu         INT           NOT NULL CONSTRAINT df_pgg_loai_phieu DEFAULT 1,
    gia_tri            DECIMAL(18,2) NOT NULL,
    gia_tri_toi_thieu  DECIMAL(18,2) NULL,
    giam_toi_da        DECIMAL(18,2) NULL,
    ngay_bat_dau       DATETIME2     NULL,
    ngay_ket_thuc      DATETIME2     NULL,
    so_luong           INT           NOT NULL,
    so_luong_da_dung   INT           NOT NULL CONSTRAINT df_pgg_da_dung DEFAULT 0,
    trang_thai         INT           NOT NULL CONSTRAINT df_pgg_trang_thai DEFAULT 1,
    ngay_tao           DATETIME2     NOT NULL CONSTRAINT df_pgg_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat      DATETIME2     NULL,
    CONSTRAINT uq_pgg_ma UNIQUE (ma),
    CONSTRAINT ck_pgg_loai CHECK (loai IN (1, 2, 3)),
    CONSTRAINT ck_pgg_loai_phieu CHECK (loai_phieu IN (1, 2)),
    CONSTRAINT ck_pgg_gia_tri CHECK (gia_tri >= 0),
    CONSTRAINT ck_pgg_phan_tram CHECK (loai <> 1 OR (gia_tri > 0 AND gia_tri <= 100)),
    CONSTRAINT ck_pgg_so_luong CHECK (so_luong >= 0),
    CONSTRAINT ck_pgg_da_dung CHECK (so_luong_da_dung >= 0 AND so_luong_da_dung <= so_luong),
    CONSTRAINT ck_pgg_trang_thai CHECK (trang_thai IN (0, 1, 2, 3, 4)),
    CONSTRAINT ck_pgg_thoi_gian CHECK (
        ngay_ket_thuc IS NULL OR ngay_bat_dau IS NULL OR ngay_ket_thuc >= ngay_bat_dau
    )
);
GO

CREATE TABLE phieu_giam_gia_khach_hang (
    id                 INT              NOT NULL CONSTRAINT pk_pgg_kh PRIMARY KEY IDENTITY(1,1),
    phieu_giam_gia_id  INT              NOT NULL,
    khach_hang_id      UNIQUEIDENTIFIER NOT NULL,
    ngay_su_dung       DATETIME2        NULL,
    trang_thai         INT              NOT NULL CONSTRAINT df_pgg_kh_trang_thai DEFAULT 1,
    ngay_tao           DATETIME2        NOT NULL CONSTRAINT df_pgg_kh_ngay_tao DEFAULT SYSDATETIME(),
    CONSTRAINT uq_pgg_kh UNIQUE (phieu_giam_gia_id, khach_hang_id),
    CONSTRAINT ck_pgg_kh_trang_thai CHECK (trang_thai IN (0, 1, 2, 3, 4)),
    CONSTRAINT fk_pgg_kh_pgg FOREIGN KEY (phieu_giam_gia_id) REFERENCES phieu_giam_gia(id) ON DELETE CASCADE,
    CONSTRAINT fk_pgg_kh_kh FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE CASCADE
);
GO

CREATE TABLE hoa_don (
    id                    INT              NOT NULL CONSTRAINT pk_hoa_don PRIMARY KEY IDENTITY(1,1),
    ma                    NVARCHAR(150)    NOT NULL,
    kenh_ban              INT              NOT NULL CONSTRAINT df_hoa_don_kenh_ban DEFAULT 1,
    khach_hang_id         UNIQUEIDENTIFIER NULL,
    nhan_vien_id          UNIQUEIDENTIFIER NULL,
    phieu_giam_gia_id     INT              NULL,
    ten_nguoi_nhan        NVARCHAR(100)    NOT NULL,
    sdt_nguoi_nhan        VARCHAR(20)      NOT NULL,
    dia_chi_giao_hang     NVARCHAR(300)    NOT NULL,
    ngay_lap              DATETIME2        NOT NULL CONSTRAINT df_hoa_don_ngay_lap DEFAULT SYSDATETIME(),
    ngay_thanh_toan       DATETIME2        NULL,
    trang_thai            INT              NOT NULL,
    tong_tien_hang        DECIMAL(18,2)    NOT NULL,
    tien_giam             DECIMAL(18,2)    NOT NULL CONSTRAINT df_hoa_don_tien_giam DEFAULT 0,
    tong_tien_thanh_toan  DECIMAL(18,2)    NOT NULL,
    ghi_chu               NVARCHAR(1000)   NULL,
    ngay_tao              DATETIME2        NOT NULL CONSTRAINT df_hoa_don_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat         DATETIME2        NULL,
    CONSTRAINT uq_hoa_don_ma UNIQUE (ma),
    CONSTRAINT ck_hoa_don_kenh_ban CHECK (kenh_ban IN (1, 2)),
    CONSTRAINT ck_hoa_don_trang_thai CHECK (trang_thai IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
    CONSTRAINT ck_hoa_don_tong_tien CHECK (
        tong_tien_hang >= 0 AND tien_giam >= 0 AND tong_tien_thanh_toan >= 0
    ),
    CONSTRAINT fk_hoa_don_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id),
    CONSTRAINT fk_hoa_don_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id),
    CONSTRAINT fk_hoa_don_pgg FOREIGN KEY (phieu_giam_gia_id) REFERENCES phieu_giam_gia(id)
);
GO

CREATE TABLE lich_su_hoa_don (
    id             INT              NOT NULL CONSTRAINT pk_lich_su_hoa_don PRIMARY KEY IDENTITY(1,1),
    hoa_don_id     INT              NOT NULL,
    nhan_vien_id   UNIQUEIDENTIFIER NULL,
    trang_thai     NVARCHAR(100)    NOT NULL,
    ghi_chu        NVARCHAR(1000)   NULL,
    ngay_tao       DATETIME2        NOT NULL CONSTRAINT df_ls_hd_ngay_tao DEFAULT SYSDATETIME(),
    CONSTRAINT fk_ls_hd_hoa_don FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE,
    CONSTRAINT fk_ls_hd_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id)
);
GO

CREATE TABLE hoa_don_chi_tiet (
    id                INT           NOT NULL CONSTRAINT pk_hoa_don_chi_tiet PRIMARY KEY IDENTITY(1,1),
    hoa_don_id        INT           NOT NULL,
    giay_chi_tiet_id  INT           NOT NULL,
    so_luong          INT           NOT NULL CONSTRAINT df_hdct_so_luong DEFAULT 1,
    gia_don_vi        DECIMAL(18,2) NOT NULL,
    thanh_tien        DECIMAL(18,2) NOT NULL,
    trang_thai        INT           NOT NULL CONSTRAINT df_hdct_trang_thai DEFAULT 1,
    ngay_tao          DATETIME2     NOT NULL CONSTRAINT df_hdct_ngay_tao DEFAULT SYSDATETIME(),
    CONSTRAINT uq_hdct UNIQUE (hoa_don_id, giay_chi_tiet_id),
    CONSTRAINT ck_hdct_so_luong CHECK (so_luong > 0),
    CONSTRAINT ck_hdct_gia_don_vi CHECK (gia_don_vi > 0),
    CONSTRAINT ck_hdct_thanh_tien CHECK (thanh_tien = so_luong * gia_don_vi),
    CONSTRAINT ck_hdct_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT fk_hdct_hoa_don FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE,
    CONSTRAINT fk_hdct_gct FOREIGN KEY (giay_chi_tiet_id) REFERENCES giay_chi_tiet(id)
);
GO

CREATE TABLE van_chuyen (
    id                 INT           NOT NULL CONSTRAINT pk_van_chuyen PRIMARY KEY IDENTITY(1,1),
    hoa_don_id         INT           NOT NULL,
    don_vi_van_chuyen  NVARCHAR(100) NOT NULL,
    ma_van_don         NVARCHAR(100) NULL,
    phi_van_chuyen     DECIMAL(18,2) NOT NULL CONSTRAINT df_vc_phi DEFAULT 0,
    ngay_gui           DATETIME2     NULL,
    ngay_du_kien       DATETIME2     NULL,
    ngay_giao_that     DATETIME2     NULL,
    trang_thai         INT           NOT NULL CONSTRAINT df_vc_trang_thai DEFAULT 1,
    ghi_chu            NVARCHAR(500) NULL,
    ngay_tao           DATETIME2     NOT NULL CONSTRAINT df_vc_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat      DATETIME2     NULL,
    CONSTRAINT uq_vc_hoa_don UNIQUE (hoa_don_id),
    CONSTRAINT ck_vc_phi CHECK (phi_van_chuyen >= 0),
    CONSTRAINT ck_vc_trang_thai CHECK (trang_thai IN (1, 2, 3, 4, 5)),
    CONSTRAINT fk_vc_hoa_don FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE
);
GO

CREATE TABLE thanh_toan (
    id                INT              NOT NULL CONSTRAINT pk_thanh_toan PRIMARY KEY IDENTITY(1,1),
    hoa_don_id        INT              NOT NULL,
    nhan_vien_id      UNIQUEIDENTIFIER NULL,
    ma_giao_dich      NVARCHAR(200)    NULL,
    hinh_thuc         INT              NOT NULL,
    so_tien           DECIMAL(18,2)    NOT NULL,
    tien_thoi_lai     DECIMAL(18,2)    NULL,
    ngan_hang         NVARCHAR(100)    NULL,
    noi_dung_ck       NVARCHAR(300)    NULL,
    cong_thanh_toan   NVARCHAR(100)    NULL,
    ngay_thanh_toan   DATETIME2        NULL,
    trang_thai        INT              NOT NULL,
    ghi_chu           NVARCHAR(500)    NULL,
    ngay_tao          DATETIME2        NOT NULL CONSTRAINT df_tt_ngay_tao DEFAULT SYSDATETIME(),
    CONSTRAINT ck_tt_hinh_thuc CHECK (hinh_thuc IN (1, 2, 3, 4)),
    CONSTRAINT ck_tt_trang_thai CHECK (trang_thai IN (0, 1, 2, 3, 4, 5)),
    CONSTRAINT ck_tt_so_tien CHECK (so_tien > 0),
    CONSTRAINT ck_tt_thoi_lai CHECK (tien_thoi_lai IS NULL OR tien_thoi_lai >= 0),
    CONSTRAINT fk_tt_hoa_don FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE,
    CONSTRAINT fk_tt_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id)
);
GO

CREATE TABLE phieu_tra_hang (
    id              INT              NOT NULL CONSTRAINT pk_phieu_tra_hang PRIMARY KEY IDENTITY(1,1),
    ma              NVARCHAR(150)    NOT NULL,
    hoa_don_id      INT              NOT NULL,
    khach_hang_id   UNIQUEIDENTIFIER NOT NULL,
    nhan_vien_id    UNIQUEIDENTIFIER NULL,
    ly_do           NVARCHAR(500)    NULL,
    tong_tien_hoan  DECIMAL(18,2)    NOT NULL,
    hinh_thuc_hoan  INT              NOT NULL,
    trang_thai      INT              NOT NULL CONSTRAINT df_pth_trang_thai DEFAULT 1,
    ngay_tao        DATETIME2        NOT NULL CONSTRAINT df_pth_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat   DATETIME2        NULL,
    CONSTRAINT uq_pth_ma UNIQUE (ma),
    CONSTRAINT ck_pth_hinh_thuc CHECK (hinh_thuc_hoan IN (1, 2, 3)),
    CONSTRAINT ck_pth_trang_thai CHECK (trang_thai IN (1, 2, 3)),
    CONSTRAINT ck_pth_tong_tien CHECK (tong_tien_hoan >= 0),
    CONSTRAINT fk_pth_hoa_don FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id),
    CONSTRAINT fk_pth_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id),
    CONSTRAINT fk_pth_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id)
);
GO

CREATE TABLE phieu_tra_hang_chi_tiet (
    id                   INT           NOT NULL CONSTRAINT pk_pthct PRIMARY KEY IDENTITY(1,1),
    phieu_tra_hang_id    INT           NOT NULL,
    hoa_don_chi_tiet_id  INT           NULL,
    giay_chi_tiet_id     INT           NOT NULL,
    so_luong_tra         INT           NOT NULL CONSTRAINT df_pthct_so_luong_tra DEFAULT 1,
    gia_ban              DECIMAL(18,2) NOT NULL,
    thanh_tien           DECIMAL(18,2) NOT NULL,
    trang_thai           INT           NOT NULL CONSTRAINT df_pthct_trang_thai DEFAULT 1,
    ghi_chu              NVARCHAR(500) NULL,
    ngay_tao             DATETIME2     NOT NULL CONSTRAINT df_pthct_ngay_tao DEFAULT SYSDATETIME(),
    CONSTRAINT ck_pthct_so_luong CHECK (so_luong_tra > 0),
    CONSTRAINT ck_pthct_gia_ban CHECK (gia_ban >= 0),
    CONSTRAINT ck_pthct_thanh_tien CHECK (thanh_tien = so_luong_tra * gia_ban),
    CONSTRAINT ck_pthct_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT fk_pthct_phieu FOREIGN KEY (phieu_tra_hang_id) REFERENCES phieu_tra_hang(id) ON DELETE CASCADE,
    CONSTRAINT fk_pthct_hdct FOREIGN KEY (hoa_don_chi_tiet_id) REFERENCES hoa_don_chi_tiet(id),
    CONSTRAINT fk_pthct_gct FOREIGN KEY (giay_chi_tiet_id) REFERENCES giay_chi_tiet(id)
);
GO

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX ix_dia_chi_kh_khach_hang ON dia_chi_khach_hang(khach_hang_id);
CREATE INDEX ix_giay_thuong_hieu ON giay(thuong_hieu_id);
CREATE INDEX ix_giay_loai_giay ON giay(loai_giay_id);
CREATE INDEX ix_giay_tt_giay ON giay_thuoc_tinh(giay_id);
CREATE INDEX ix_giay_tt_de_giay ON giay_thuoc_tinh(de_giay_id);
CREATE INDEX ix_giay_tt_co_giay ON giay_thuoc_tinh(co_giay_id);
CREATE INDEX ix_giay_tt_chat_lieu ON giay_thuoc_tinh(chat_lieu_giay_id);
CREATE INDEX ix_giay_tt_trong_luong ON giay_thuoc_tinh(trong_luong_id);
CREATE INDEX ix_giay_tt_cong_nghe ON giay_thuoc_tinh(cong_nghe_dem_id);
CREATE INDEX ix_gct_giay ON giay_chi_tiet(giay_id);
CREATE INDEX ix_gct_mau_sac ON giay_chi_tiet(mau_sac_id);
CREATE INDEX ix_gct_kich_co ON giay_chi_tiet(kich_co_id);
CREATE INDEX ix_hag_gct ON hinh_anh_giay(giay_chi_tiet_id);
CREATE INDEX ix_dgg_sp_dot ON dot_giam_gia_san_pham(dot_giam_gia_id);
CREATE INDEX ix_dgg_sp_gct ON dot_giam_gia_san_pham(giay_chi_tiet_id);
CREATE INDEX ix_pgg_kh_pgg ON phieu_giam_gia_khach_hang(phieu_giam_gia_id);
CREATE INDEX ix_pgg_kh_kh ON phieu_giam_gia_khach_hang(khach_hang_id);
CREATE INDEX ix_hoa_don_khach_hang ON hoa_don(khach_hang_id);
CREATE INDEX ix_hoa_don_nhan_vien ON hoa_don(nhan_vien_id);
CREATE INDEX ix_hoa_don_kenh_ban ON hoa_don(kenh_ban);
CREATE INDEX ix_hoa_don_trang_thai ON hoa_don(trang_thai);
CREATE INDEX ix_ls_hd_hoa_don ON lich_su_hoa_don(hoa_don_id);
CREATE INDEX ix_hdct_hoa_don ON hoa_don_chi_tiet(hoa_don_id);
CREATE INDEX ix_hdct_gct ON hoa_don_chi_tiet(giay_chi_tiet_id);
CREATE INDEX ix_vc_hoa_don ON van_chuyen(hoa_don_id);
CREATE INDEX ix_tt_hoa_don ON thanh_toan(hoa_don_id);
CREATE INDEX ix_pth_hoa_don ON phieu_tra_hang(hoa_don_id);
CREATE INDEX ix_pthct_phieu ON phieu_tra_hang_chi_tiet(phieu_tra_hang_id);
GO
