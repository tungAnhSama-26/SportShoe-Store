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

CREATE TABLE quyen_han (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_quyen_han PRIMARY KEY,
    ma_quyen_han VARCHAR(50) NOT NULL,
    ten_quyen_han NVARCHAR(100) NOT NULL,
    trang_thai INT NOT NULL CONSTRAINT df_quyen_han_trang_thai DEFAULT 1,
    xoa_mem BIT NOT NULL CONSTRAINT df_quyen_han_xoa_mem DEFAULT 0,
    CONSTRAINT uq_quyen_han_ma UNIQUE (ma_quyen_han),
    CONSTRAINT uq_quyen_han_ten UNIQUE (ten_quyen_han),
    CONSTRAINT ck_quyen_han_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE chuc_nang (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_chuc_nang PRIMARY KEY,
    ma_chuc_nang VARCHAR(50) NOT NULL,
    ten_chuc_nang NVARCHAR(100) NOT NULL,
    mo_ta NVARCHAR(255) NULL,
    trang_thai INT NOT NULL CONSTRAINT df_chuc_nang_trang_thai DEFAULT 1,
    xoa_mem BIT NOT NULL CONSTRAINT df_chuc_nang_xoa_mem DEFAULT 0,
    CONSTRAINT uq_chuc_nang_ma UNIQUE (ma_chuc_nang),
    CONSTRAINT uq_chuc_nang_ten UNIQUE (ten_chuc_nang),
    CONSTRAINT ck_chuc_nang_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE quyen_han_chuc_nang (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_quyen_han_chuc_nang PRIMARY KEY,
    id_quyen_han INT NOT NULL,
    id_chuc_nang INT NOT NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_quyen_han_chuc_nang_xoa_mem DEFAULT 0,
    CONSTRAINT uq_quyen_han_chuc_nang UNIQUE (id_quyen_han, id_chuc_nang),
    CONSTRAINT fk_quyen_han_chuc_nang_quyen_han FOREIGN KEY (id_quyen_han) REFERENCES quyen_han(id),
    CONSTRAINT fk_quyen_han_chuc_nang_chuc_nang FOREIGN KEY (id_chuc_nang) REFERENCES chuc_nang(id)
);
GO

CREATE TABLE nhan_vien (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT pk_nhan_vien PRIMARY KEY DEFAULT NEWID(),
    ma VARCHAR(20) NOT NULL,
    ten_dang_nhap VARCHAR(50) NOT NULL,
    ho_ten NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NULL,
    mat_khau VARCHAR(255) NOT NULL,
    sdt VARCHAR(20) NULL,
    dia_chi NVARCHAR(200) NULL,
    quyen_han_id INT NOT NULL,
    trang_thai INT NOT NULL CONSTRAINT df_nhan_vien_trang_thai DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_nhan_vien_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_nhan_vien_xoa_mem DEFAULT 0,
    CONSTRAINT uq_nhan_vien_ma UNIQUE (ma),
    CONSTRAINT uq_nhan_vien_ten_dang_nhap UNIQUE (ten_dang_nhap),
    CONSTRAINT uq_nhan_vien_email UNIQUE (email),
    CONSTRAINT ck_nhan_vien_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT fk_nhan_vien_quyen_han FOREIGN KEY (quyen_han_id) REFERENCES quyen_han(id)
);
GO

CREATE TABLE khach_hang (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT pk_khach_hang PRIMARY KEY DEFAULT NEWID(),
    ten_dang_nhap VARCHAR(50) NOT NULL,
    ho_ten NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NULL,
    sdt VARCHAR(20) NULL,
    dia_chi NVARCHAR(200) NULL,
    ngay_sinh DATE NULL,
    mat_khau VARCHAR(255) NOT NULL,
    trang_thai INT NOT NULL CONSTRAINT df_khach_hang_trang_thai DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_khach_hang_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_khach_hang_xoa_mem DEFAULT 0,
    CONSTRAINT uq_khach_hang_ten_dang_nhap UNIQUE (ten_dang_nhap),
    CONSTRAINT uq_khach_hang_email UNIQUE (email),
    CONSTRAINT ck_khach_hang_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

CREATE TABLE kich_co (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_kich_co PRIMARY KEY,
    gia_tri NVARCHAR(20) NOT NULL,
    ghi_chu NVARCHAR(200) NULL,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_kich_co_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_kich_co_xoa_mem DEFAULT 0,
    CONSTRAINT uq_kich_co_gia_tri UNIQUE (gia_tri)
);
GO

CREATE TABLE mau_sac (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_mau_sac PRIMARY KEY,
    ma NVARCHAR(50) NOT NULL,
    ten NVARCHAR(100) NOT NULL,
    ma_mau_hex NVARCHAR(7) NULL,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_mau_sac_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_mau_sac_xoa_mem DEFAULT 0,
    CONSTRAINT uq_mau_sac_ma UNIQUE (ma),
    CONSTRAINT uq_mau_sac_ten UNIQUE (ten),
    CONSTRAINT ck_mau_sac_ma_mau_hex CHECK (ma_mau_hex IS NULL OR ma_mau_hex LIKE '#[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]')
);
GO

CREATE TABLE dot_giam_gia (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_dot_giam_gia PRIMARY KEY,
    ma NVARCHAR(100) NOT NULL,
    ten NVARCHAR(200) NOT NULL,
    mo_ta NVARCHAR(500) NULL,
    phan_tram DECIMAL(5,2) NULL,
    so_tien_giam DECIMAL(18,2) NULL,
    ngay_bat_dau DATETIME2 NULL,
    ngay_ket_thuc DATETIME2 NULL,
    kich_hoat BIT NOT NULL CONSTRAINT df_dot_giam_gia_kich_hoat DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_dot_giam_gia_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_dot_giam_gia_xoa_mem DEFAULT 0,
    CONSTRAINT uq_dot_giam_gia_ma UNIQUE (ma),
    CONSTRAINT ck_dot_giam_gia_phan_tram CHECK (phan_tram IS NULL OR (phan_tram >= 0 AND phan_tram <= 100)),
    CONSTRAINT ck_dot_giam_gia_so_tien_giam CHECK (so_tien_giam IS NULL OR so_tien_giam >= 0),
    CONSTRAINT ck_dot_giam_gia_thoi_gian CHECK (ngay_ket_thuc IS NULL OR ngay_bat_dau IS NULL OR ngay_ket_thuc >= ngay_bat_dau)
);
GO

CREATE TABLE giay (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_giay PRIMARY KEY,
    ma NVARCHAR(100) NOT NULL,
    ten NVARCHAR(300) NOT NULL,
    thuong_hieu NVARCHAR(200) NOT NULL,
    loai_giay NVARCHAR(100) NULL,
    gioi_tinh INT NULL,
    chat_lieu NVARCHAR(100) NULL,
    dot_giam_gia_id INT NULL,
    mo_ta NVARCHAR(MAX) NULL,
    trang_thai INT NOT NULL CONSTRAINT df_giay_trang_thai DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_giay_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_giay_xoa_mem DEFAULT 0,
    CONSTRAINT uq_giay_ma UNIQUE (ma),
    CONSTRAINT ck_giay_trang_thai CHECK (trang_thai IN (1, 2)),
    CONSTRAINT ck_giay_gioi_tinh CHECK (gioi_tinh IS NULL OR gioi_tinh IN (1, 2, 3)),
    CONSTRAINT fk_giay_dot_giam_gia FOREIGN KEY (dot_giam_gia_id) REFERENCES dot_giam_gia(id)
);
GO

CREATE TABLE giay_chi_tiet (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_giay_chi_tiet PRIMARY KEY,
    giay_id INT NOT NULL,
    ma_bien_the NVARCHAR(150) NOT NULL,
    mau_sac_id INT NOT NULL,
    kich_co_id INT NOT NULL,
    so_luong INT NOT NULL CONSTRAINT df_giay_chi_tiet_so_luong DEFAULT 0,
    gia_goc DECIMAL(18,2) NOT NULL,
    gia_ban DECIMAL(18,2) NOT NULL,
    sku NVARCHAR(150) NOT NULL,
    kich_hoat BIT NOT NULL CONSTRAINT df_giay_chi_tiet_kich_hoat DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_giay_chi_tiet_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_giay_chi_tiet_xoa_mem DEFAULT 0,
    CONSTRAINT uq_giay_chi_tiet_ma_bien_the UNIQUE (ma_bien_the),
    CONSTRAINT uq_giay_chi_tiet_sku UNIQUE (sku),
    CONSTRAINT uq_giay_chi_tiet_to_hop UNIQUE (giay_id, mau_sac_id, kich_co_id),
    CONSTRAINT ck_giay_chi_tiet_so_luong CHECK (so_luong >= 0),
    CONSTRAINT ck_giay_chi_tiet_gia_goc CHECK (gia_goc >= 0),
    CONSTRAINT ck_giay_chi_tiet_gia_ban CHECK (gia_ban >= 0),
    CONSTRAINT fk_giay_chi_tiet_giay FOREIGN KEY (giay_id) REFERENCES giay(id) ON DELETE CASCADE,
    CONSTRAINT fk_giay_chi_tiet_mau_sac FOREIGN KEY (mau_sac_id) REFERENCES mau_sac(id),
    CONSTRAINT fk_giay_chi_tiet_kich_co FOREIGN KEY (kich_co_id) REFERENCES kich_co(id)
);
GO

CREATE TABLE hinh_anh_giay (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_hinh_anh_giay PRIMARY KEY,
    giay_chi_tiet_id INT NOT NULL,
    loai_hinh INT NOT NULL,
    url NVARCHAR(1000) NOT NULL,
    mo_ta NVARCHAR(300) NULL,
    la_hinh_chinh BIT NOT NULL CONSTRAINT df_hinh_anh_giay_la_hinh_chinh DEFAULT 0,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_hinh_anh_giay_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_hinh_anh_giay_xoa_mem DEFAULT 0,
    CONSTRAINT ck_hinh_anh_giay_loai CHECK (loai_hinh IN (1, 2, 3)),
    CONSTRAINT fk_hinh_anh_giay_giay_chi_tiet FOREIGN KEY (giay_chi_tiet_id) REFERENCES giay_chi_tiet(id) ON DELETE CASCADE
);
GO

CREATE TABLE phieu_giam_gia (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_phieu_giam_gia PRIMARY KEY,
    ma NVARCHAR(100) NOT NULL,
    ten NVARCHAR(200) NOT NULL,
    loai INT NOT NULL,
    gia_tri DECIMAL(18,2) NOT NULL,
    ngay_bat_dau DATETIME2 NULL,
    ngay_ket_thuc DATETIME2 NULL,
    so_luong INT NOT NULL,
    kich_hoat BIT NOT NULL CONSTRAINT df_phieu_giam_gia_kich_hoat DEFAULT 1,
    trang_thai INT NOT NULL CONSTRAINT df_phieu_giam_gia_trang_thai DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_phieu_giam_gia_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_phieu_giam_gia_xoa_mem DEFAULT 0,
    CONSTRAINT uq_phieu_giam_gia_ma UNIQUE (ma),
    CONSTRAINT ck_phieu_giam_gia_loai CHECK (loai IN (1, 2, 3)),
    CONSTRAINT ck_phieu_giam_gia_gia_tri CHECK (gia_tri >= 0),
    CONSTRAINT ck_phieu_giam_gia_so_luong CHECK (so_luong >= 0),
    CONSTRAINT ck_phieu_giam_gia_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT ck_phieu_giam_gia_thoi_gian CHECK (ngay_ket_thuc IS NULL OR ngay_bat_dau IS NULL OR ngay_ket_thuc >= ngay_bat_dau)
);
GO

CREATE TABLE phieu_giam_gia_khach_hang (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_phieu_giam_gia_khach_hang PRIMARY KEY,
    phieu_giam_gia_id INT NOT NULL,
    khach_hang_id UNIQUEIDENTIFIER NOT NULL,
    da_su_dung BIT NOT NULL CONSTRAINT df_phieu_giam_gia_khach_hang_da_su_dung DEFAULT 0,
    ngay_su_dung DATETIME2 NULL,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_phieu_giam_gia_khach_hang_ngay_tao DEFAULT SYSDATETIME(),
    xoa_mem BIT NOT NULL CONSTRAINT df_phieu_giam_gia_khach_hang_xoa_mem DEFAULT 0,
    CONSTRAINT uq_phieu_giam_gia_khach_hang UNIQUE (phieu_giam_gia_id, khach_hang_id),
    CONSTRAINT fk_pgg_kh_phieu_giam_gia FOREIGN KEY (phieu_giam_gia_id) REFERENCES phieu_giam_gia(id) ON DELETE CASCADE,
    CONSTRAINT fk_pgg_kh_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE CASCADE
);
GO

CREATE TABLE gio_hang (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_gio_hang PRIMARY KEY,
    khach_hang_id UNIQUEIDENTIFIER NOT NULL,
    kich_hoat BIT NOT NULL CONSTRAINT df_gio_hang_kich_hoat DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_gio_hang_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_gio_hang_xoa_mem DEFAULT 0,
    CONSTRAINT uq_gio_hang_khach_hang UNIQUE (khach_hang_id),
    CONSTRAINT fk_gio_hang_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE CASCADE
);
GO

CREATE TABLE gio_hang_chi_tiet (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_gio_hang_chi_tiet PRIMARY KEY,
    gio_hang_id INT NOT NULL,
    giay_chi_tiet_id INT NOT NULL,
    so_luong INT NOT NULL CONSTRAINT df_gio_hang_chi_tiet_so_luong DEFAULT 1,
    gia_tai_thoi_diem DECIMAL(18,2) NOT NULL,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_gio_hang_chi_tiet_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_gio_hang_chi_tiet_xoa_mem DEFAULT 0,
    CONSTRAINT uq_gio_hang_chi_tiet UNIQUE (gio_hang_id, giay_chi_tiet_id),
    CONSTRAINT ck_gio_hang_chi_tiet_so_luong CHECK (so_luong > 0),
    CONSTRAINT ck_gio_hang_chi_tiet_gia CHECK (gia_tai_thoi_diem >= 0),
    CONSTRAINT fk_gio_hang_chi_tiet_gio_hang FOREIGN KEY (gio_hang_id) REFERENCES gio_hang(id) ON DELETE CASCADE,
    CONSTRAINT fk_gio_hang_chi_tiet_giay_chi_tiet FOREIGN KEY (giay_chi_tiet_id) REFERENCES giay_chi_tiet(id)
);
GO

CREATE TABLE hoa_don (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_hoa_don PRIMARY KEY,
    ma NVARCHAR(150) NOT NULL,
    khach_hang_id UNIQUEIDENTIFIER NOT NULL,
    nhan_vien_id UNIQUEIDENTIFIER NULL,
    phieu_giam_gia_id INT NULL,
    ngay_lap DATETIME2 NOT NULL CONSTRAINT df_hoa_don_ngay_lap DEFAULT SYSDATETIME(),
    ngay_thanh_toan DATETIME2 NULL,
    trang_thai INT NOT NULL,
    tong_tien DECIMAL(18,2) NOT NULL,
    ghi_chu NVARCHAR(1000) NULL,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_hoa_don_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_hoa_don_xoa_mem DEFAULT 0,
    CONSTRAINT uq_hoa_don_ma UNIQUE (ma),
    CONSTRAINT ck_hoa_don_trang_thai CHECK (trang_thai IN (1, 2, 3, 4)),
    CONSTRAINT ck_hoa_don_tong_tien CHECK (tong_tien >= 0),
    CONSTRAINT fk_hoa_don_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id),
    CONSTRAINT fk_hoa_don_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id),
    CONSTRAINT fk_hoa_don_phieu_giam_gia FOREIGN KEY (phieu_giam_gia_id) REFERENCES phieu_giam_gia(id)
);
GO

CREATE TABLE hoa_don_chi_tiet (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_hoa_don_chi_tiet PRIMARY KEY,
    hoa_don_id INT NOT NULL,
    giay_chi_tiet_id INT NOT NULL,
    so_luong INT NOT NULL CONSTRAINT df_hoa_don_chi_tiet_so_luong DEFAULT 1,
    gia_don_vi DECIMAL(18,2) NOT NULL,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_hoa_don_chi_tiet_ngay_tao DEFAULT SYSDATETIME(),
    xoa_mem BIT NOT NULL CONSTRAINT df_hoa_don_chi_tiet_xoa_mem DEFAULT 0,
    CONSTRAINT uq_hoa_don_chi_tiet UNIQUE (hoa_don_id, giay_chi_tiet_id),
    CONSTRAINT ck_hoa_don_chi_tiet_so_luong CHECK (so_luong > 0),
    CONSTRAINT ck_hoa_don_chi_tiet_gia CHECK (gia_don_vi >= 0),
    CONSTRAINT fk_hoa_don_chi_tiet_hoa_don FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE,
    CONSTRAINT fk_hoa_don_chi_tiet_giay_chi_tiet FOREIGN KEY (giay_chi_tiet_id) REFERENCES giay_chi_tiet(id)
);
GO

CREATE TABLE danh_gia (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_danh_gia PRIMARY KEY,
    khach_hang_id UNIQUEIDENTIFIER NOT NULL,
    giay_id INT NOT NULL,
    hoa_don_id INT NULL,
    so_sao INT NOT NULL,
    noi_dung NVARCHAR(1000) NULL,
    trang_thai INT NOT NULL CONSTRAINT df_danh_gia_trang_thai DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_danh_gia_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_danh_gia_xoa_mem DEFAULT 0,
    CONSTRAINT uq_danh_gia_khach_hang_giay UNIQUE (khach_hang_id, giay_id),
    CONSTRAINT ck_danh_gia_so_sao CHECK (so_sao BETWEEN 1 AND 5),
    CONSTRAINT ck_danh_gia_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT fk_danh_gia_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id),
    CONSTRAINT fk_danh_gia_giay FOREIGN KEY (giay_id) REFERENCES giay(id),
    CONSTRAINT fk_danh_gia_hoa_don FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id)
);
GO

CREATE TABLE thanh_toan (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_thanh_toan PRIMARY KEY,
    hoa_don_id INT NOT NULL,
    ma_giao_dich NVARCHAR(200) NULL,
    hinh_thuc INT NOT NULL,
    so_tien DECIMAL(18,2) NOT NULL,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_thanh_toan_ngay_tao DEFAULT SYSDATETIME(),
    trang_thai INT NOT NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_thanh_toan_xoa_mem DEFAULT 0,
    CONSTRAINT ck_thanh_toan_hinh_thuc CHECK (hinh_thuc IN (1, 2, 3)),
    CONSTRAINT ck_thanh_toan_trang_thai CHECK (trang_thai IN (0, 1, 2)),
    CONSTRAINT ck_thanh_toan_so_tien CHECK (so_tien >= 0),
    CONSTRAINT fk_thanh_toan_hoa_don FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE
);
GO

CREATE TABLE phieu_tra_hang (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_phieu_tra_hang PRIMARY KEY,
    ma NVARCHAR(150) NOT NULL,
    hoa_don_id INT NOT NULL,
    khach_hang_id UNIQUEIDENTIFIER NOT NULL,
    nhan_vien_id UNIQUEIDENTIFIER NULL,
    ly_do NVARCHAR(500) NULL,
    tong_tien_hoan DECIMAL(18,2) NOT NULL,
    hinh_thuc_hoan INT NOT NULL,
    trang_thai INT NOT NULL CONSTRAINT df_phieu_tra_hang_trang_thai DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_phieu_tra_hang_ngay_tao DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2 NULL,
    xoa_mem BIT NOT NULL CONSTRAINT df_phieu_tra_hang_xoa_mem DEFAULT 0,
    CONSTRAINT uq_phieu_tra_hang_ma UNIQUE (ma),
    CONSTRAINT ck_phieu_tra_hang_hinh_thuc_hoan CHECK (hinh_thuc_hoan IN (1, 2, 3)),
    CONSTRAINT ck_phieu_tra_hang_trang_thai CHECK (trang_thai IN (1, 2, 3)),
    CONSTRAINT ck_phieu_tra_hang_tong_tien_hoan CHECK (tong_tien_hoan >= 0),
    CONSTRAINT fk_phieu_tra_hang_hoa_don FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id),
    CONSTRAINT fk_phieu_tra_hang_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id),
    CONSTRAINT fk_phieu_tra_hang_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id)
);
GO

CREATE TABLE phieu_tra_hang_chi_tiet (
    id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_phieu_tra_hang_chi_tiet PRIMARY KEY,
    phieu_tra_hang_id INT NOT NULL,
    hoa_don_chi_tiet_id INT NULL,
    giay_chi_tiet_id INT NOT NULL,
    so_luong_tra INT NOT NULL CONSTRAINT df_phieu_tra_hang_ct_so_luong_tra DEFAULT 1,
    gia_ban DECIMAL(18,2) NOT NULL,
    thanh_tien DECIMAL(18,2) NOT NULL,
    ghi_chu NVARCHAR(500) NULL,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT df_phieu_tra_hang_ct_ngay_tao DEFAULT SYSDATETIME(),
    xoa_mem BIT NOT NULL CONSTRAINT df_phieu_tra_hang_ct_xoa_mem DEFAULT 0,
    CONSTRAINT ck_phieu_tra_hang_ct_so_luong_tra CHECK (so_luong_tra > 0),
    CONSTRAINT ck_phieu_tra_hang_ct_gia_ban CHECK (gia_ban >= 0),
    CONSTRAINT ck_phieu_tra_hang_ct_thanh_tien CHECK (thanh_tien >= 0),
    CONSTRAINT fk_phieu_tra_hang_ct_phieu_tra_hang FOREIGN KEY (phieu_tra_hang_id) REFERENCES phieu_tra_hang(id) ON DELETE CASCADE,
    CONSTRAINT fk_phieu_tra_hang_ct_hoa_don_ct FOREIGN KEY (hoa_don_chi_tiet_id) REFERENCES hoa_don_chi_tiet(id),
    CONSTRAINT fk_phieu_tra_hang_ct_giay_chi_tiet FOREIGN KEY (giay_chi_tiet_id) REFERENCES giay_chi_tiet(id)
);
GO

CREATE INDEX ix_nhan_vien_quyen_han_id ON nhan_vien(quyen_han_id);
CREATE INDEX ix_quyen_han_chuc_nang_quyen_han ON quyen_han_chuc_nang(id_quyen_han);
CREATE INDEX ix_quyen_han_chuc_nang_chuc_nang ON quyen_han_chuc_nang(id_chuc_nang);
CREATE INDEX ix_giay_dot_giam_gia_id ON giay(dot_giam_gia_id);
CREATE INDEX ix_giay_chi_tiet_giay_id ON giay_chi_tiet(giay_id);
CREATE INDEX ix_giay_chi_tiet_mau_sac_id ON giay_chi_tiet(mau_sac_id);
CREATE INDEX ix_giay_chi_tiet_kich_co_id ON giay_chi_tiet(kich_co_id);
CREATE INDEX ix_gio_hang_chi_tiet_gio_hang_id ON gio_hang_chi_tiet(gio_hang_id);
CREATE INDEX ix_hoa_don_khach_hang_id ON hoa_don(khach_hang_id);
CREATE INDEX ix_hoa_don_nhan_vien_id ON hoa_don(nhan_vien_id);
CREATE INDEX ix_hoa_don_phieu_giam_gia_id ON hoa_don(phieu_giam_gia_id);
CREATE INDEX ix_hoa_don_chi_tiet_hoa_don_id ON hoa_don_chi_tiet(hoa_don_id);
CREATE INDEX ix_danh_gia_giay_id ON danh_gia(giay_id);
CREATE INDEX ix_danh_gia_khach_hang_id ON danh_gia(khach_hang_id);
CREATE INDEX ix_thanh_toan_hoa_don_id ON thanh_toan(hoa_don_id);
CREATE INDEX ix_phieu_tra_hang_hoa_don_id ON phieu_tra_hang(hoa_don_id);
GO

INSERT INTO quyen_han (ma_quyen_han, ten_quyen_han, trang_thai, xoa_mem)
VALUES
('QH_ADMIN', N'Admin', 1, 0),
('QH_BAN_HANG', N'Nhan vien ban hang', 1, 0),
('QH_KHO', N'Nhan vien kho', 1, 0);
GO

INSERT INTO chuc_nang (ma_chuc_nang, ten_chuc_nang, mo_ta, trang_thai, xoa_mem)
VALUES
('CN_QL_GIAY', N'Quan ly giay', N'Them sua xoa giay va bien the', 1, 0),
('CN_QL_HOA_DON', N'Quan ly hoa don', N'Tao va xu ly hoa don', 1, 0),
('CN_QL_KHUYEN_MAI', N'Quan ly khuyen mai', N'Quan ly dot giam gia va phieu giam gia', 1, 0),
('CN_QL_NHAN_VIEN', N'Quan ly nhan vien', N'Quan ly nhan vien va quyen han', 1, 0),
('CN_QL_KHO', N'Quan ly kho', N'Quan ly ton kho san pham', 1, 0);
GO

INSERT INTO quyen_han_chuc_nang (id_quyen_han, id_chuc_nang, xoa_mem)
VALUES
(1, 1, 0), (1, 2, 0), (1, 3, 0), (1, 4, 0), (1, 5, 0),
(2, 1, 0), (2, 2, 0), (2, 3, 0),
(3, 1, 0), (3, 5, 0);
GO

INSERT INTO nhan_vien (ma, ten_dang_nhap, ho_ten, email, mat_khau, sdt, dia_chi, quyen_han_id, xoa_mem)
VALUES
('NV01', 'admin', N'Nguyen Van A', 'a@gmail.com', 'hashed_admin_123', '0900000001', N'Ha Noi', 1, 0),
('NV02', 'nvbanhang1', N'Tran Thi B', 'b@gmail.com', 'hashed_nv_123', '0900000002', N'TP HCM', 2, 0),
('NV03', 'nvbanhang2', N'Le Van C', 'c@gmail.com', 'hashed_nv_123', '0900000003', N'Da Nang', 2, 0),
('NV04', 'nvkho1', N'Pham Thi D', 'd@gmail.com', 'hashed_nv_123', '0900000004', N'Ha Noi', 3, 0),
('NV05', 'nvkho2', N'Hoang Van E', 'e@gmail.com', 'hashed_nv_123', '0900000005', N'TP HCM', 3, 0);
GO

INSERT INTO khach_hang (ten_dang_nhap, ho_ten, email, sdt, dia_chi, ngay_sinh, mat_khau, xoa_mem)
VALUES
('khach1', N'Khach Hang 1', 'kh1@gmail.com', '0911111111', N'Ha Noi', '1999-01-01', 'hashed_kh_123', 0),
('khach2', N'Khach Hang 2', 'kh2@gmail.com', '0922222222', N'TP HCM', '1998-02-02', 'hashed_kh_123', 0),
('khach3', N'Khach Hang 3', 'kh3@gmail.com', '0933333333', N'Da Nang', '1997-03-03', 'hashed_kh_123', 0),
('khach4', N'Khach Hang 4', 'kh4@gmail.com', '0944444444', N'Ha Noi', '1996-04-04', 'hashed_kh_123', 0),
('khach5', N'Khach Hang 5', 'kh5@gmail.com', '0955555555', N'TP HCM', '1995-05-05', 'hashed_kh_123', 0);
GO

INSERT INTO kich_co (gia_tri, ghi_chu, xoa_mem)
VALUES
(N'38', N'Size 38', 0),
(N'39', N'Size 39', 0),
(N'40', N'Size 40', 0),
(N'41', N'Size 41', 0),
(N'42', N'Size 42', 0);
GO

INSERT INTO mau_sac (ma, ten, ma_mau_hex, xoa_mem)
VALUES
(N'MS01', N'Do', '#FF0000', 0),
(N'MS02', N'Xanh', '#00FF00', 0),
(N'MS03', N'Vang', '#FFFF00', 0),
(N'MS04', N'Den', '#000000', 0),
(N'MS05', N'Trang', '#FFFFFF', 0);
GO

INSERT INTO dot_giam_gia (ma, ten, mo_ta, phan_tram, so_tien_giam, ngay_bat_dau, ngay_ket_thuc, xoa_mem)
VALUES
(N'DGG01', N'Sale khai truong', N'Giam theo phan tram cho mau giay', 10, NULL, '2026-03-01', '2026-03-31', 0),
(N'DGG02', N'Sale cuoi tuan', N'Giam theo so tien cho mau giay', NULL, 50000, '2026-03-20', '2026-03-30', 0);
GO

INSERT INTO giay (ma, ten, thuong_hieu, loai_giay, gioi_tinh, chat_lieu, dot_giam_gia_id, mo_ta, xoa_mem)
VALUES
(N'G01', N'Giay Sneaker Nike Air', N'Nike', N'Running', 1, N'Vai luoi', 1, N'Mau chay bo nhe va em', 0),
(N'G02', N'Giay Adidas Superstar', N'Adidas', N'Lifestyle', 3, N'Da tong hop', 1, N'Mau sneaker mac hang ngay', 0),
(N'G03', N'Giay Puma Classic', N'Puma', N'Training', 1, N'Vai Canvas', NULL, N'Mau tap luyen co ban', 0),
(N'G04', N'Giay Vans Old Skool', N'Vans', N'Skate', 3, N'Vai Canvas', 2, N'Mau truot van co dien', 0),
(N'G05', N'Giay Converse Chuck 70', N'Converse', N'Casual', 3, N'Vai Canvas', NULL, N'Mau di pho bien', 0);
GO

INSERT INTO giay_chi_tiet (giay_id, ma_bien_the, mau_sac_id, kich_co_id, so_luong, gia_goc, gia_ban, sku, xoa_mem)
VALUES
(1, N'G01-DO-38', 1, 1, 10, 500000, 650000, N'SKU01', 0),
(2, N'G02-XANH-39', 2, 2, 15, 600000, 750000, N'SKU02', 0),
(3, N'G03-VANG-40', 3, 3, 20, 550000, 700000, N'SKU03', 0),
(4, N'G04-DEN-41', 4, 4, 8, 400000, 550000, N'SKU04', 0),
(5, N'G05-TRANG-42', 5, 5, 12, 450000, 600000, N'SKU05', 0);
GO

INSERT INTO hinh_anh_giay (giay_chi_tiet_id, loai_hinh, url, mo_ta, la_hinh_chinh, xoa_mem)
VALUES
(1, 1, N'https://example.com/sku01-main.jpg', N'Hinh chinh SKU01', 1, 0),
(2, 1, N'https://example.com/sku02-main.jpg', N'Hinh chinh SKU02', 1, 0),
(3, 1, N'https://example.com/sku03-main.jpg', N'Hinh chinh SKU03', 1, 0),
(4, 1, N'https://example.com/sku04-main.jpg', N'Hinh chinh SKU04', 1, 0),
(5, 1, N'https://example.com/sku05-main.jpg', N'Hinh chinh SKU05', 1, 0);
GO

INSERT INTO phieu_giam_gia (ma, ten, loai, gia_tri, ngay_bat_dau, ngay_ket_thuc, so_luong, trang_thai, xoa_mem)
VALUES
(N'V01', N'Giam 10 phan tram', 1, 10, '2026-03-01', '2026-12-31', 100, 1, 0),
(N'V02', N'Giam 20 phan tram', 1, 20, '2026-03-01', '2026-12-31', 100, 1, 0),
(N'V03', N'Giam 30000', 2, 30000, '2026-03-01', '2026-12-31', 100, 1, 0),
(N'V04', N'Giam 50000', 2, 50000, '2026-03-01', '2026-12-31', 100, 1, 0),
(N'V05', N'Mien phi van chuyen', 3, 0, '2026-03-01', '2026-12-31', 100, 1, 0);
GO

INSERT INTO phieu_giam_gia_khach_hang (phieu_giam_gia_id, khach_hang_id, da_su_dung, ngay_su_dung, xoa_mem)
SELECT p.id, k.id,
       CASE WHEN p.ma IN (N'V02', N'V05') THEN 1 ELSE 0 END,
       CASE WHEN p.ma IN (N'V02', N'V05') THEN DATEADD(DAY, 1, k.ngay_tao) ELSE NULL END,
       0
FROM phieu_giam_gia p
JOIN khach_hang k
    ON RIGHT(p.ma, 1) = RIGHT(k.ten_dang_nhap, 1);
GO

INSERT INTO gio_hang (khach_hang_id, xoa_mem)
SELECT id, 0 FROM khach_hang;
GO

INSERT INTO gio_hang_chi_tiet (gio_hang_id, giay_chi_tiet_id, so_luong, gia_tai_thoi_diem, xoa_mem)
VALUES
(1, 1, 2, 650000, 0),
(1, 2, 1, 750000, 0),
(2, 3, 1, 700000, 0),
(3, 4, 3, 550000, 0),
(4, 5, 1, 600000, 0);
GO

INSERT INTO hoa_don (ma, khach_hang_id, nhan_vien_id, phieu_giam_gia_id, ngay_thanh_toan, trang_thai, tong_tien, ghi_chu, xoa_mem)
SELECT
    CONCAT(N'HD', RIGHT('00' + CAST(ROW_NUMBER() OVER (ORDER BY k.ten_dang_nhap) AS VARCHAR(2)), 2)),
    k.id,
    (SELECT TOP 1 id FROM nhan_vien ORDER BY ma),
    p.id,
    DATEADD(HOUR, 2, k.ngay_tao),
    2,
    CASE p.ma
        WHEN N'V01' THEN 1300000
        WHEN N'V02' THEN 1200000
        WHEN N'V03' THEN 1470000
        WHEN N'V04' THEN 1450000
        ELSE 1500000
    END,
    N'Hoa don mau',
    0
FROM khach_hang k
LEFT JOIN phieu_giam_gia p
    ON RIGHT(p.ma, 1) = RIGHT(k.ten_dang_nhap, 1);
GO

INSERT INTO hoa_don_chi_tiet (hoa_don_id, giay_chi_tiet_id, so_luong, gia_don_vi, xoa_mem)
VALUES
(1, 1, 2, 650000, 0),
(2, 3, 1, 700000, 0),
(3, 4, 1, 550000, 0),
(4, 2, 1, 750000, 0),
(5, 5, 1, 600000, 0);
GO

INSERT INTO danh_gia (khach_hang_id, giay_id, hoa_don_id, so_sao, noi_dung, xoa_mem)
SELECT k.id, 1, 1, 5, N'Giay dep, di em chan', 0
FROM khach_hang k
WHERE k.ten_dang_nhap = 'khach1';

INSERT INTO danh_gia (khach_hang_id, giay_id, hoa_don_id, so_sao, noi_dung, xoa_mem)
SELECT k.id, 3, 2, 4, N'Form dep, gia hop ly', 0
FROM khach_hang k
WHERE k.ten_dang_nhap = 'khach2';

INSERT INTO danh_gia (khach_hang_id, giay_id, hoa_don_id, so_sao, noi_dung, xoa_mem)
SELECT k.id, 4, 3, 5, N'Mau dep, di hang ngay rat on', 0
FROM khach_hang k
WHERE k.ten_dang_nhap = 'khach3';
GO

INSERT INTO thanh_toan (hoa_don_id, ma_giao_dich, hinh_thuc, so_tien, trang_thai, xoa_mem)
VALUES
(1, N'TT-HD01', 1, 1300000, 1, 0),
(2, N'TT-HD02', 2, 1200000, 1, 0),
(3, N'TT-HD03', 3, 1470000, 1, 0),
(4, N'TT-HD04', 1, 1450000, 1, 0),
(5, N'TT-HD05', 2, 1500000, 1, 0);
GO

INSERT INTO phieu_tra_hang (ma, hoa_don_id, khach_hang_id, nhan_vien_id, ly_do, tong_tien_hoan, hinh_thuc_hoan, trang_thai, xoa_mem)
SELECT
    CONCAT(N'PTH', RIGHT('00' + CAST(ROW_NUMBER() OVER (ORDER BY h.ma) AS VARCHAR(2)), 2)),
    h.id,
    h.khach_hang_id,
    h.nhan_vien_id,
    N'Khong vua size',
    CASE h.id
        WHEN 1 THEN 650000
        WHEN 2 THEN 700000
        WHEN 3 THEN 550000
        WHEN 4 THEN 750000
        ELSE 600000
    END,
    1,
    2,
    0
FROM hoa_don h;
GO

INSERT INTO phieu_tra_hang_chi_tiet (phieu_tra_hang_id, hoa_don_chi_tiet_id, giay_chi_tiet_id, so_luong_tra, gia_ban, thanh_tien, ghi_chu, xoa_mem)
VALUES
(1, 1, 1, 1, 650000, 650000, N'Tra 1 doi', 0),
(2, 2, 3, 1, 700000, 700000, N'Tra 1 doi', 0),
(3, 3, 4, 1, 550000, 550000, N'Tra 1 doi', 0),
(4, 4, 2, 1, 750000, 750000, N'Tra 1 doi', 0),
(5, 5, 5, 1, 600000, 600000, N'Tra 1 doi', 0);
GO
