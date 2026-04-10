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
-- GHI CHÚ TRANG_THAI CHUNG:
--   Hầu hết bảng dùng: 0 = Không hoạt động | 1 = Hoạt động
--   Các bảng nghiệp vụ có thêm trạng thái riêng (xem từng bảng)
-- ============================================================

-- ============================================================
-- [01] nhan_vien
-- ============================================================
CREATE TABLE nhan_vien (
    id            UNIQUEIDENTIFIER NOT NULL CONSTRAINT pk_nhan_vien PRIMARY KEY DEFAULT NEWID(),
    ma            VARCHAR(20)      NOT NULL,
    ho_ten        NVARCHAR(100)    NOT NULL,
    email         VARCHAR(100)     NOT NULL,
    mat_khau      VARCHAR(255)     NOT NULL,
    sdt           VARCHAR(20)      NULL,
    dia_chi       NVARCHAR(200)    NULL,
    vai_tro       INT              NOT NULL CONSTRAINT df_nhan_vien_vai_tro    DEFAULT 2,
    -- 1 = Admin  |  2 = Bán hàng  |  3 = Kho
    trang_thai    INT              NOT NULL CONSTRAINT df_nhan_vien_trang_thai DEFAULT 1,
    -- 0 = Khóa   |  1 = Hoạt động
    ngay_tao      DATETIME2        NOT NULL CONSTRAINT df_nhan_vien_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2        NULL,
    CONSTRAINT uq_nhan_vien_ma         UNIQUE (ma),
    CONSTRAINT uq_nhan_vien_email      UNIQUE (email),
    CONSTRAINT ck_nhan_vien_trang_thai CHECK  (trang_thai IN (0, 1)),
    CONSTRAINT ck_nhan_vien_vai_tro    CHECK  (vai_tro    IN (1, 2, 3))
);
GO

-- ============================================================
-- [02] khach_hang
-- ============================================================
CREATE TABLE khach_hang (
    id            UNIQUEIDENTIFIER NOT NULL CONSTRAINT pk_khach_hang PRIMARY KEY DEFAULT NEWID(),
    ten_dang_nhap VARCHAR(50)      NOT NULL,
    ho_ten        NVARCHAR(100)    NOT NULL,
    email         VARCHAR(100)     NULL,
    sdt           VARCHAR(20)      NULL,
    ngay_sinh     DATE             NULL,
    mat_khau      VARCHAR(255)     NOT NULL,
    trang_thai    INT              NOT NULL CONSTRAINT df_khach_hang_trang_thai DEFAULT 1,
    -- 0 = Khóa   |  1 = Hoạt động
    ngay_tao      DATETIME2        NOT NULL CONSTRAINT df_khach_hang_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2        NULL,
    CONSTRAINT uq_khach_hang_ten_dang_nhap UNIQUE (ten_dang_nhap),
    CONSTRAINT uq_khach_hang_email         UNIQUE (email),
    CONSTRAINT ck_khach_hang_trang_thai    CHECK  (trang_thai IN (0, 1))
);
GO

-- ============================================================
-- [03] dia_chi_khach_hang
--   trang_thai: 0 = Không dùng nữa | 1 = Đang dùng
-- ============================================================
CREATE TABLE dia_chi_khach_hang (
    id             INT              NOT NULL CONSTRAINT pk_dia_chi_kh PRIMARY KEY IDENTITY(1,1),
    khach_hang_id  UNIQUEIDENTIFIER NOT NULL,
    ho_ten         NVARCHAR(100)    NOT NULL,
    sdt            VARCHAR(20)      NOT NULL,
    tinh_thanh     NVARCHAR(100)    NOT NULL,
    quan_huyen     NVARCHAR(100)    NOT NULL,
    phuong_xa      NVARCHAR(100)    NOT NULL,
    dia_chi_cu_the NVARCHAR(300)    NOT NULL,
    la_mac_dinh    BIT              NOT NULL CONSTRAINT df_dc_kh_la_mac_dinh DEFAULT 0,
    trang_thai     INT              NOT NULL CONSTRAINT df_dc_kh_trang_thai  DEFAULT 1,
    -- 0 = Không dùng nữa  |  1 = Đang dùng
    ngay_tao       DATETIME2        NOT NULL CONSTRAINT df_dc_kh_ngay_tao    DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2        NULL,
    CONSTRAINT ck_dc_kh_trang_thai CHECK  (trang_thai IN (0, 1)),
    CONSTRAINT fk_dia_chi_kh       FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- [04] kich_co
--   trang_thai: 0 = Ngừng dùng | 1 = Đang dùng
-- ============================================================
CREATE TABLE kich_co (
    id            INT           NOT NULL CONSTRAINT pk_kich_co PRIMARY KEY IDENTITY(1,1),
    gia_tri       NVARCHAR(20)  NOT NULL,
    ghi_chu       NVARCHAR(200) NULL,
    trang_thai    INT           NOT NULL CONSTRAINT df_kich_co_trang_thai DEFAULT 1,
    -- 0 = Ngừng dùng  |  1 = Đang dùng
    ngay_tao      DATETIME2     NOT NULL CONSTRAINT df_kich_co_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2     NULL,
    CONSTRAINT uq_kich_co_gia_tri    UNIQUE (gia_tri),
    CONSTRAINT ck_kich_co_trang_thai CHECK  (trang_thai IN (0, 1))
);
GO

-- ============================================================
-- [05] mau_sac
--   trang_thai: 0 = Ngừng dùng | 1 = Đang dùng
-- ============================================================
CREATE TABLE mau_sac (
    id            INT           NOT NULL CONSTRAINT pk_mau_sac PRIMARY KEY IDENTITY(1,1),
    ma            NVARCHAR(50)  NOT NULL,
    ten           NVARCHAR(100) NOT NULL,
    ma_mau_hex    NVARCHAR(7)   NULL,
    trang_thai    INT           NOT NULL CONSTRAINT df_mau_sac_trang_thai DEFAULT 1,
    -- 0 = Ngừng dùng  |  1 = Đang dùng
    ngay_tao      DATETIME2     NOT NULL CONSTRAINT df_mau_sac_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2     NULL,
    CONSTRAINT uq_mau_sac_ma       UNIQUE (ma),
    CONSTRAINT uq_mau_sac_ten      UNIQUE (ten),
    CONSTRAINT ck_mau_sac_trang_thai CHECK (trang_thai IN (0, 1)),
    CONSTRAINT ck_mau_sac_ma_hex   CHECK  (
        ma_mau_hex IS NULL OR
        ma_mau_hex LIKE '#[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]'
    )
);
GO

-- ============================================================
-- [06] thuong_hieu
--   trang_thai: 0 = Ngừng kinh doanh | 1 = Đang kinh doanh
-- ============================================================
CREATE TABLE thuong_hieu (
    id            INT           NOT NULL CONSTRAINT pk_thuong_hieu PRIMARY KEY IDENTITY(1,1),
    ma            NVARCHAR(50)  NOT NULL,
    ten           NVARCHAR(200) NOT NULL,
    xuat_xu       NVARCHAR(100) NULL,
    mo_ta         NVARCHAR(500) NULL,
    logo_url      NVARCHAR(500) NULL,
    website       NVARCHAR(300) NULL,
    trang_thai    INT           NOT NULL CONSTRAINT df_thuong_hieu_trang_thai DEFAULT 1,
    -- 0 = Ngừng kinh doanh  |  1 = Đang kinh doanh
    ngay_tao      DATETIME2     NOT NULL CONSTRAINT df_thuong_hieu_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2     NULL,
    CONSTRAINT uq_thuong_hieu_ma       UNIQUE (ma),
    CONSTRAINT uq_thuong_hieu_ten      UNIQUE (ten),
    CONSTRAINT ck_thuong_hieu_trang_thai CHECK (trang_thai IN (0, 1))
);
GO

-- ============================================================
-- [07] loai_giay
--   trang_thai: 0 = Ẩn | 1 = Hiển thị
--   Chỉ 1 cấp vì shop chuyên giày thể thao (không cần cha-con)
-- ============================================================
CREATE TABLE loai_giay (
    id              INT           NOT NULL CONSTRAINT pk_loai_giay PRIMARY KEY IDENTITY(1,1),
    ma              NVARCHAR(50)  NOT NULL,
    ten             NVARCHAR(200) NOT NULL,
    mo_ta           NVARCHAR(500) NULL,
    
    trang_thai      INT           NOT NULL CONSTRAINT df_loai_giay_trang_thai DEFAULT 1,
    -- 0 = Ẩn  |  1 = Hiển thị
    ngay_tao        DATETIME2     NOT NULL CONSTRAINT df_loai_giay_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat   DATETIME2     NULL,
    CONSTRAINT uq_loai_giay_ma         UNIQUE (ma),
    CONSTRAINT uq_loai_giay_ten        UNIQUE (ten),
    CONSTRAINT ck_loai_giay_trang_thai CHECK  (trang_thai IN (0, 1))
);
GO

-- ============================================================
-- [08] de_giay
--   trang_thai: 0 = Ngừng dùng | 1 = Đang dùng
-- ============================================================
CREATE TABLE de_giay (
    id            INT           NOT NULL CONSTRAINT pk_de_giay PRIMARY KEY IDENTITY(1,1),
    ma            NVARCHAR(50)  NOT NULL,
    ten           NVARCHAR(100) NOT NULL,
    mo_ta         NVARCHAR(300) NULL,
    trang_thai    INT           NOT NULL CONSTRAINT df_de_giay_trang_thai DEFAULT 1,
    -- 0 = Ngừng dùng  |  1 = Đang dùng
    ngay_tao      DATETIME2     NOT NULL CONSTRAINT df_de_giay_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2     NULL,
    CONSTRAINT uq_de_giay_ma         UNIQUE (ma),
    CONSTRAINT uq_de_giay_ten        UNIQUE (ten),
    CONSTRAINT ck_de_giay_trang_thai CHECK  (trang_thai IN (0, 1))
);
GO

-- ============================================================
-- [09] co_giay
--   trang_thai: 0 = Ngừng dùng | 1 = Đang dùng
-- ============================================================
CREATE TABLE co_giay (
    id            INT           NOT NULL CONSTRAINT pk_co_giay PRIMARY KEY IDENTITY(1,1),
    ma            NVARCHAR(50)  NOT NULL,
    ten           NVARCHAR(100) NOT NULL,
    mo_ta         NVARCHAR(300) NULL,
    trang_thai    INT           NOT NULL CONSTRAINT df_co_giay_trang_thai DEFAULT 1,
    -- 0 = Ngừng dùng  |  1 = Đang dùng
    ngay_tao      DATETIME2     NOT NULL CONSTRAINT df_co_giay_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2     NULL,
    CONSTRAINT uq_co_giay_ma         UNIQUE (ma),
    CONSTRAINT uq_co_giay_ten        UNIQUE (ten),
    CONSTRAINT ck_co_giay_trang_thai CHECK  (trang_thai IN (0, 1))
);
GO

-- ============================================================
-- [10] trong_luong
--   trang_thai: 0 = Ngừng dùng | 1 = Đang dùng
-- ============================================================
CREATE TABLE trong_luong (
    id            INT           NOT NULL CONSTRAINT pk_trong_luong PRIMARY KEY IDENTITY(1,1),
    ma            NVARCHAR(50)  NOT NULL,
    gia_tri       INT           NOT NULL,   -- đơn vị: gram
    mo_ta         NVARCHAR(300) NULL,
    trang_thai    INT           NOT NULL CONSTRAINT df_trong_luong_trang_thai DEFAULT 1,
    -- 0 = Ngừng dùng  |  1 = Đang dùng
    ngay_tao      DATETIME2     NOT NULL CONSTRAINT df_trong_luong_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2     NULL,
    CONSTRAINT uq_trong_luong_ma         UNIQUE (ma),
    CONSTRAINT uq_trong_luong_gia_tri    UNIQUE (gia_tri),
    CONSTRAINT ck_trong_luong_gia_tri    CHECK  (gia_tri > 0),
    CONSTRAINT ck_trong_luong_trang_thai CHECK  (trang_thai IN (0, 1))
);
GO

-- ============================================================
-- [11] cong_nghe_dem
--   trang_thai: 0 = Ngừng dùng | 1 = Đang dùng
-- ============================================================
CREATE TABLE cong_nghe_dem (
    id            INT           NOT NULL CONSTRAINT pk_cong_nghe_dem PRIMARY KEY IDENTITY(1,1),
    ma            NVARCHAR(50)  NOT NULL,
    ten           NVARCHAR(200) NOT NULL,
    mo_ta         NVARCHAR(500) NULL,
    trang_thai    INT           NOT NULL CONSTRAINT df_cong_nghe_dem_trang_thai DEFAULT 1,
    -- 0 = Ngừng dùng  |  1 = Đang dùng
    ngay_tao      DATETIME2     NOT NULL CONSTRAINT df_cong_nghe_dem_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2     NULL,
    CONSTRAINT uq_cong_nghe_dem_ma         UNIQUE (ma),
    CONSTRAINT uq_cong_nghe_dem_ten        UNIQUE (ten),
    CONSTRAINT ck_cong_nghe_dem_trang_thai CHECK  (trang_thai IN (0, 1))
);
GO

-- ============================================================
-- [12] dot_giam_gia
--   kich_hoat: 0 = Tạm dừng | 1 = Đang chạy
-- ============================================================
CREATE TABLE dot_giam_gia (
    id            INT           NOT NULL CONSTRAINT pk_dot_giam_gia PRIMARY KEY IDENTITY(1,1),
    ma            NVARCHAR(100) NOT NULL,
    ten           NVARCHAR(200) NOT NULL,
    mo_ta         NVARCHAR(500) NULL,
    loai_giam     INT           NOT NULL,
    -- 1 = Phần trăm  |  2 = Số tiền cố định
    gia_tri_giam  DECIMAL(18,2) NOT NULL,
    ngay_bat_dau  DATETIME2     NULL,
    ngay_ket_thuc DATETIME2     NULL,
    kich_hoat     INT           NOT NULL CONSTRAINT df_dgg_kich_hoat DEFAULT 1,
    -- 0 = Tạm dừng  |  1 = Đang chạy
    ngay_tao      DATETIME2     NOT NULL CONSTRAINT df_dgg_ngay_tao  DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2     NULL,
    CONSTRAINT uq_dot_giam_gia_ma        UNIQUE (ma),
    CONSTRAINT ck_dot_giam_gia_loai      CHECK  (loai_giam IN (1, 2)),
    CONSTRAINT ck_dot_giam_gia_gia_tri   CHECK  (gia_tri_giam > 0),
    CONSTRAINT ck_dot_giam_gia_phan_tram CHECK  (
        loai_giam <> 1 OR (gia_tri_giam > 0 AND gia_tri_giam <= 100)
    ),
    CONSTRAINT ck_dot_giam_gia_kich_hoat CHECK  (kich_hoat IN (0, 1)),
    CONSTRAINT ck_dot_giam_gia_thoi_gian CHECK  (
        ngay_ket_thuc IS NULL OR ngay_bat_dau IS NULL OR ngay_ket_thuc >= ngay_bat_dau
    )
);
GO

-- ============================================================
-- [13] giay
--   trang_thai: 1 = Đang bán | 2 = Ngừng bán
-- ============================================================
CREATE TABLE giay (
    id             INT           NOT NULL CONSTRAINT pk_giay PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(100) NOT NULL,
    ten            NVARCHAR(300) NOT NULL,
    thuong_hieu_id INT           NOT NULL,
    loai_giay_id   INT           NOT NULL,
    gioi_tinh      INT           NULL,
    -- 1 = Nam  |  2 = Nữ  |  3 = Unisex
    chat_lieu      NVARCHAR(100) NULL,
    mo_ta          NVARCHAR(MAX) NULL,
    trang_thai     INT           NOT NULL CONSTRAINT df_giay_trang_thai DEFAULT 1,
    -- 1 = Đang bán  |  2 = Ngừng bán
    ngay_tao       DATETIME2     NOT NULL CONSTRAINT df_giay_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2     NULL,
    CONSTRAINT uq_giay_ma           UNIQUE (ma),
    CONSTRAINT ck_giay_trang_thai   CHECK  (trang_thai IN (1, 2)),
    CONSTRAINT ck_giay_gioi_tinh    CHECK  (gioi_tinh IS NULL OR gioi_tinh IN (1, 2, 3)),
    CONSTRAINT fk_giay_thuong_hieu  FOREIGN KEY (thuong_hieu_id) REFERENCES thuong_hieu(id),
    CONSTRAINT fk_giay_loai_giay    FOREIGN KEY (loai_giay_id)   REFERENCES loai_giay(id)
);
GO

-- ============================================================
-- [14] giay_thuoc_tinh  (1-1 với giay)
--   trang_thai: 0 = Chưa cập nhật | 1 = Đầy đủ
--   4 thuộc tính đặc trưng giày thể thao (FK sang bảng danh mục)
-- ============================================================
CREATE TABLE giay_thuoc_tinh (
    id               INT       NOT NULL CONSTRAINT pk_giay_thuoc_tinh PRIMARY KEY IDENTITY(1,1),
    giay_id          INT       NOT NULL,
    de_giay_id       INT       NULL,
    co_giay_id       INT       NULL,
    trong_luong_id   INT       NULL,
    cong_nghe_dem_id INT       NULL,
    trang_thai       INT       NOT NULL CONSTRAINT df_giay_tt_trang_thai DEFAULT 1,
    -- 0 = Chưa cập nhật đủ  |  1 = Đầy đủ thông tin
    ngay_tao         DATETIME2 NOT NULL CONSTRAINT df_giay_tt_ngay_tao  DEFAULT SYSDATETIME(),
    ngay_cap_nhat    DATETIME2 NULL,
    CONSTRAINT uq_giay_thuoc_tinh_giay_id   UNIQUE (giay_id),
    CONSTRAINT ck_giay_tt_trang_thai        CHECK  (trang_thai IN (0, 1)),
    CONSTRAINT fk_giay_thuoc_tinh_giay      FOREIGN KEY (giay_id)          REFERENCES giay(id)          ON DELETE CASCADE,
    CONSTRAINT fk_giay_tt_de_giay           FOREIGN KEY (de_giay_id)       REFERENCES de_giay(id),
    CONSTRAINT fk_giay_tt_co_giay           FOREIGN KEY (co_giay_id)       REFERENCES co_giay(id),
    CONSTRAINT fk_giay_tt_trong_luong       FOREIGN KEY (trong_luong_id)   REFERENCES trong_luong(id),
    CONSTRAINT fk_giay_tt_cong_nghe_dem     FOREIGN KEY (cong_nghe_dem_id) REFERENCES cong_nghe_dem(id)
);
GO

-- ============================================================
-- [15] dot_giam_gia_san_pham  (nhiều-nhiều)
--   trang_thai: 0 = Đã hủy | 1 = Đang áp dụng
-- ============================================================
CREATE TABLE dot_giam_gia_san_pham (
    id              INT       NOT NULL CONSTRAINT pk_dgg_sp PRIMARY KEY IDENTITY(1,1),
    dot_giam_gia_id INT       NOT NULL,
    giay_id         INT       NOT NULL,
    trang_thai      INT       NOT NULL CONSTRAINT df_dgg_sp_trang_thai DEFAULT 1,
    -- 0 = Đã hủy  |  1 = Đang áp dụng
    ngay_tao        DATETIME2 NOT NULL CONSTRAINT df_dgg_sp_ngay_tao   DEFAULT SYSDATETIME(),
    CONSTRAINT uq_dgg_sp               UNIQUE (dot_giam_gia_id, giay_id),
    CONSTRAINT ck_dgg_sp_trang_thai    CHECK  (trang_thai IN (0, 1)),
    CONSTRAINT fk_dgg_sp_dot_giam_gia  FOREIGN KEY (dot_giam_gia_id) REFERENCES dot_giam_gia(id),
    CONSTRAINT fk_dgg_sp_giay          FOREIGN KEY (giay_id)         REFERENCES giay(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- [16] giay_chi_tiet
--   kich_hoat: 0 = Ngừng bán | 1 = Đang bán
-- ============================================================
CREATE TABLE giay_chi_tiet (
    id            INT           NOT NULL CONSTRAINT pk_giay_chi_tiet PRIMARY KEY IDENTITY(1,1),
    giay_id       INT           NOT NULL,
    ma_bien_the   NVARCHAR(150) NOT NULL,
    mau_sac_id    INT           NOT NULL,
    kich_co_id    INT           NOT NULL,
    so_luong      INT           NOT NULL CONSTRAINT df_gct_so_luong  DEFAULT 0,
    gia_goc       DECIMAL(18,2) NOT NULL,
    gia_ban       DECIMAL(18,2) NOT NULL,
    sku           NVARCHAR(150) NOT NULL,
    kich_hoat     INT           NOT NULL CONSTRAINT df_gct_kich_hoat DEFAULT 1,
    -- 0 = Ngừng bán  |  1 = Đang bán
    ngay_tao      DATETIME2     NOT NULL CONSTRAINT df_gct_ngay_tao  DEFAULT SYSDATETIME(),
    ngay_cap_nhat DATETIME2     NULL,
    CONSTRAINT uq_gct_ma_bien_the  UNIQUE (ma_bien_the),
    CONSTRAINT uq_gct_sku          UNIQUE (sku),
    CONSTRAINT uq_gct_to_hop       UNIQUE (giay_id, mau_sac_id, kich_co_id),
    CONSTRAINT ck_gct_so_luong     CHECK  (so_luong >= 0),
    CONSTRAINT ck_gct_gia_goc      CHECK  (gia_goc > 0),
    CONSTRAINT ck_gct_gia_ban      CHECK  (gia_ban > 0),
    CONSTRAINT ck_gct_kich_hoat    CHECK  (kich_hoat IN (0, 1)),
    CONSTRAINT fk_gct_giay         FOREIGN KEY (giay_id)    REFERENCES giay(id)    ON DELETE CASCADE,
    CONSTRAINT fk_gct_mau_sac      FOREIGN KEY (mau_sac_id) REFERENCES mau_sac(id),
    CONSTRAINT fk_gct_kich_co      FOREIGN KEY (kich_co_id) REFERENCES kich_co(id)
);
GO

-- ============================================================
-- [17] hinh_anh_giay
--   trang_thai: 0 = Ẩn | 1 = Hiển thị
-- ============================================================
CREATE TABLE hinh_anh_giay (
    id               INT            NOT NULL CONSTRAINT pk_hinh_anh_giay PRIMARY KEY IDENTITY(1,1),
    giay_chi_tiet_id INT            NOT NULL,
    loai_hinh        INT            NOT NULL,
    -- 1 = Ảnh chính  |  2 = Ảnh phụ  |  3 = Video
    url              NVARCHAR(1000) NOT NULL,
    mo_ta            NVARCHAR(300)  NULL,
    la_hinh_chinh    BIT            NOT NULL CONSTRAINT df_hag_la_hinh_chinh DEFAULT 0,
    trang_thai       INT            NOT NULL CONSTRAINT df_hag_trang_thai    DEFAULT 1,
    -- 0 = Ẩn  |  1 = Hiển thị
    ngay_tao         DATETIME2      NOT NULL CONSTRAINT df_hag_ngay_tao      DEFAULT SYSDATETIME(),
    ngay_cap_nhat    DATETIME2      NULL,
    CONSTRAINT ck_hag_loai_hinh  CHECK  (loai_hinh IN (1, 2, 3)),
    CONSTRAINT ck_hag_trang_thai CHECK  (trang_thai IN (0, 1)),
    CONSTRAINT fk_hag_gct        FOREIGN KEY (giay_chi_tiet_id) REFERENCES giay_chi_tiet(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- [18] phieu_giam_gia
--   trang_thai: 0 = Ngừng | 1 = Hoạt động
-- ============================================================
CREATE TABLE phieu_giam_gia (
    id                INT           NOT NULL CONSTRAINT pk_phieu_giam_gia PRIMARY KEY IDENTITY(1,1),
    ma                NVARCHAR(100) NOT NULL,
    ten               NVARCHAR(200) NOT NULL,
    loai              INT           NOT NULL,
    -- 1 = Phần trăm  |  2 = Số tiền cố định  |  3 = Miễn phí vận chuyển
    gia_tri           DECIMAL(18,2) NOT NULL,
    gia_tri_toi_thieu DECIMAL(18,2) NULL,
    giam_toi_da       DECIMAL(18,2) NULL,
    ngay_bat_dau      DATETIME2     NULL,
    ngay_ket_thuc     DATETIME2     NULL,
    so_luong          INT           NOT NULL,
    so_luong_da_dung  INT           NOT NULL CONSTRAINT df_pgg_da_dung   DEFAULT 0,
    trang_thai        INT           NOT NULL CONSTRAINT df_pgg_trang_thai DEFAULT 1,
    -- 0 = Ngừng  |  1 = Hoạt động
    ngay_tao          DATETIME2     NOT NULL CONSTRAINT df_pgg_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat     DATETIME2     NULL,
    CONSTRAINT uq_pgg_ma           UNIQUE (ma),
    CONSTRAINT ck_pgg_loai         CHECK  (loai IN (1, 2, 3)),
    CONSTRAINT ck_pgg_gia_tri      CHECK  (gia_tri >= 0),
    CONSTRAINT ck_pgg_phan_tram    CHECK  (loai <> 1 OR (gia_tri > 0 AND gia_tri <= 100)),
    CONSTRAINT ck_pgg_so_luong     CHECK  (so_luong >= 0),
    CONSTRAINT ck_pgg_da_dung      CHECK  (so_luong_da_dung >= 0),
    CONSTRAINT ck_pgg_trang_thai   CHECK  (trang_thai IN (0, 1)),
    CONSTRAINT ck_pgg_thoi_gian    CHECK  (
        ngay_ket_thuc IS NULL OR ngay_bat_dau IS NULL OR ngay_ket_thuc >= ngay_bat_dau
    )
);
GO

-- ============================================================
-- [19] phieu_giam_gia_khach_hang
--   trang_thai: 0 = Đã dùng | 1 = Chưa dùng
-- ============================================================
CREATE TABLE phieu_giam_gia_khach_hang (
    id                INT              NOT NULL CONSTRAINT pk_pgg_kh PRIMARY KEY IDENTITY(1,1),
    phieu_giam_gia_id INT              NOT NULL,
    khach_hang_id     UNIQUEIDENTIFIER NOT NULL,
    ngay_su_dung      DATETIME2        NULL,
    trang_thai        INT              NOT NULL CONSTRAINT df_pgg_kh_trang_thai DEFAULT 1,
    -- 0 = Đã dùng  |  1 = Chưa dùng
    ngay_tao          DATETIME2        NOT NULL CONSTRAINT df_pgg_kh_ngay_tao   DEFAULT SYSDATETIME(),
    CONSTRAINT uq_pgg_kh             UNIQUE (phieu_giam_gia_id, khach_hang_id),
    CONSTRAINT ck_pgg_kh_trang_thai  CHECK  (trang_thai IN (0, 1)),
    CONSTRAINT fk_pgg_kh_pgg         FOREIGN KEY (phieu_giam_gia_id) REFERENCES phieu_giam_gia(id) ON DELETE CASCADE,
    CONSTRAINT fk_pgg_kh_kh          FOREIGN KEY (khach_hang_id)    REFERENCES khach_hang(id)     ON DELETE CASCADE
);
GO

-- ============================================================
-- [20] hoa_don
--   kenh_ban : 1 = Tại quầy   | 2 = Online
--   trang_thai: 1=Chờ xác nhận | 2=Đã thanh toán | 3=Đang giao
--               4=Hoàn thành   | 5=Đã hủy
-- ============================================================
CREATE TABLE hoa_don (
    id                   INT              NOT NULL CONSTRAINT pk_hoa_don PRIMARY KEY IDENTITY(1,1),
    ma                   NVARCHAR(150)    NOT NULL,
    kenh_ban             INT              NOT NULL CONSTRAINT df_hoa_don_kenh_ban  DEFAULT 1,
    -- 1 = Tại quầy  |  2 = Online
    khach_hang_id        UNIQUEIDENTIFIER NULL,
    nhan_vien_id         UNIQUEIDENTIFIER NULL,
    phieu_giam_gia_id    INT              NULL,
    ten_nguoi_nhan       NVARCHAR(100)    NOT NULL,
    sdt_nguoi_nhan       VARCHAR(20)      NOT NULL,
    dia_chi_giao_hang    NVARCHAR(300)    NOT NULL,
    ngay_lap             DATETIME2        NOT NULL CONSTRAINT df_hoa_don_ngay_lap  DEFAULT SYSDATETIME(),
    ngay_thanh_toan      DATETIME2        NULL,
    trang_thai           INT              NOT NULL,
    tong_tien_hang       DECIMAL(18,2)    NOT NULL,
    tien_giam            DECIMAL(18,2)    NOT NULL CONSTRAINT df_hoa_don_tien_giam DEFAULT 0,
    tong_tien_thanh_toan DECIMAL(18,2)    NOT NULL,
    ghi_chu              NVARCHAR(1000)   NULL,
    ngay_tao             DATETIME2        NOT NULL CONSTRAINT df_hoa_don_ngay_tao  DEFAULT SYSDATETIME(),
    ngay_cap_nhat        DATETIME2        NULL,
    CONSTRAINT uq_hoa_don_ma          UNIQUE (ma),
    CONSTRAINT ck_hoa_don_kenh_ban    CHECK  (kenh_ban IN (1, 2)),
    CONSTRAINT ck_hoa_don_trang_thai  CHECK  (trang_thai IN (1, 2, 3, 4, 5)),
    CONSTRAINT ck_hoa_don_tong_tien   CHECK  (
        tong_tien_hang >= 0 AND tien_giam >= 0 AND tong_tien_thanh_toan >= 0
    ),
    CONSTRAINT fk_hoa_don_kh   FOREIGN KEY (khach_hang_id)     REFERENCES khach_hang(id),
    CONSTRAINT fk_hoa_don_nv   FOREIGN KEY (nhan_vien_id)      REFERENCES nhan_vien(id),
    CONSTRAINT fk_hoa_don_pgg  FOREIGN KEY (phieu_giam_gia_id) REFERENCES phieu_giam_gia(id)
);
GO

-- ============================================================
-- [21] hoa_don_chi_tiet
--   trang_thai: 0 = Đã hủy dòng | 1 = Hợp lệ
-- ============================================================
CREATE TABLE hoa_don_chi_tiet (
    id               INT           NOT NULL CONSTRAINT pk_hoa_don_chi_tiet PRIMARY KEY IDENTITY(1,1),
    hoa_don_id       INT           NOT NULL,
    giay_chi_tiet_id INT           NOT NULL,
    so_luong         INT           NOT NULL CONSTRAINT df_hdct_so_luong  DEFAULT 1,
    gia_don_vi       DECIMAL(18,2) NOT NULL,
    thanh_tien       DECIMAL(18,2) NOT NULL,
    trang_thai       INT           NOT NULL CONSTRAINT df_hdct_trang_thai DEFAULT 1,
    -- 0 = Đã hủy dòng  |  1 = Hợp lệ
    ngay_tao         DATETIME2     NOT NULL CONSTRAINT df_hdct_ngay_tao  DEFAULT SYSDATETIME(),
    CONSTRAINT uq_hdct              UNIQUE (hoa_don_id, giay_chi_tiet_id),
    CONSTRAINT ck_hdct_so_luong     CHECK  (so_luong > 0),
    CONSTRAINT ck_hdct_gia_don_vi   CHECK  (gia_don_vi > 0),
    CONSTRAINT ck_hdct_thanh_tien   CHECK  (thanh_tien = so_luong * gia_don_vi),
    CONSTRAINT ck_hdct_trang_thai   CHECK  (trang_thai IN (0, 1)),
    CONSTRAINT fk_hdct_hoa_don      FOREIGN KEY (hoa_don_id)       REFERENCES hoa_don(id)       ON DELETE CASCADE,
    CONSTRAINT fk_hdct_gct          FOREIGN KEY (giay_chi_tiet_id) REFERENCES giay_chi_tiet(id)
);
GO

-- ============================================================
-- [22] van_chuyen
--   trang_thai: 1=Chờ lấy hàng | 2=Đang vận chuyển | 3=Đã giao
--               4=Giao thất bại | 5=Đã hoàn hàng
-- ============================================================
CREATE TABLE van_chuyen (
    id                INT           NOT NULL CONSTRAINT pk_van_chuyen PRIMARY KEY IDENTITY(1,1),
    hoa_don_id        INT           NOT NULL,
    don_vi_van_chuyen NVARCHAR(100) NOT NULL,
    -- GHN / GHTK / ViettelPost / J&T...
    ma_van_don        NVARCHAR(100) NULL,
    phi_van_chuyen    DECIMAL(18,2) NOT NULL CONSTRAINT df_vc_phi        DEFAULT 0,
    ngay_gui          DATETIME2     NULL,
    ngay_du_kien      DATETIME2     NULL,
    ngay_giao_that    DATETIME2     NULL,
    trang_thai        INT           NOT NULL CONSTRAINT df_vc_trang_thai DEFAULT 1,
    ghi_chu           NVARCHAR(500) NULL,
    ngay_tao          DATETIME2     NOT NULL CONSTRAINT df_vc_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat     DATETIME2     NULL,
    CONSTRAINT uq_vc_hoa_don_id  UNIQUE (hoa_don_id),
    CONSTRAINT ck_vc_phi         CHECK  (phi_van_chuyen >= 0),
    CONSTRAINT ck_vc_trang_thai  CHECK  (trang_thai IN (1, 2, 3, 4, 5)),
    CONSTRAINT fk_vc_hoa_don     FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- [23] thanh_toan
--   trang_thai: 0 = Thất bại | 1 = Thành công | 2 = Đang chờ
-- ============================================================
CREATE TABLE thanh_toan (
    id              INT              NOT NULL CONSTRAINT pk_thanh_toan PRIMARY KEY IDENTITY(1,1),
    hoa_don_id      INT              NOT NULL,
    nhan_vien_id    UNIQUEIDENTIFIER NULL,
    ma_giao_dich    NVARCHAR(200)    NULL,
    hinh_thuc       INT              NOT NULL,
    -- 1 = Tiền mặt  |  2 = Chuyển khoản  |  3 = Ví điện tử
    so_tien         DECIMAL(18,2)    NOT NULL,
    tien_thoi_lai   DECIMAL(18,2)    NULL,
    ngan_hang       NVARCHAR(100)    NULL,
    noi_dung_ck     NVARCHAR(300)    NULL,
    cong_thanh_toan NVARCHAR(100)    NULL,
    ngay_thanh_toan DATETIME2        NULL,
    trang_thai      INT              NOT NULL,
    ghi_chu         NVARCHAR(500)    NULL,
    ngay_tao        DATETIME2        NOT NULL CONSTRAINT df_tt_ngay_tao DEFAULT SYSDATETIME(),
    CONSTRAINT ck_tt_hinh_thuc  CHECK  (hinh_thuc IN (1, 2, 3)),
    CONSTRAINT ck_tt_trang_thai CHECK  (trang_thai IN (0, 1, 2)),
    CONSTRAINT ck_tt_so_tien    CHECK  (so_tien > 0),
    CONSTRAINT ck_tt_thoi_lai   CHECK  (tien_thoi_lai IS NULL OR tien_thoi_lai >= 0),
    CONSTRAINT fk_tt_hoa_don    FOREIGN KEY (hoa_don_id)   REFERENCES hoa_don(id) ON DELETE CASCADE,
    CONSTRAINT fk_tt_nhan_vien  FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id)
);
GO

-- ============================================================
-- [24] phieu_tra_hang
--   trang_thai: 1=Chờ xử lý | 2=Đã hoàn tiền | 3=Từ chối
-- ============================================================
CREATE TABLE phieu_tra_hang (
    id             INT              NOT NULL CONSTRAINT pk_phieu_tra_hang PRIMARY KEY IDENTITY(1,1),
    ma             NVARCHAR(150)    NOT NULL,
    hoa_don_id     INT              NOT NULL,
    khach_hang_id  UNIQUEIDENTIFIER NOT NULL,
    nhan_vien_id   UNIQUEIDENTIFIER NULL,
    ly_do          NVARCHAR(500)    NULL,
    tong_tien_hoan DECIMAL(18,2)    NOT NULL,
    hinh_thuc_hoan INT              NOT NULL,
    -- 1 = Tiền mặt  |  2 = Chuyển khoản  |  3 = Ví điện tử
    trang_thai     INT              NOT NULL CONSTRAINT df_pth_trang_thai DEFAULT 1,
    ngay_tao       DATETIME2        NOT NULL CONSTRAINT df_pth_ngay_tao   DEFAULT SYSDATETIME(),
    ngay_cap_nhat  DATETIME2        NULL,
    CONSTRAINT uq_pth_ma         UNIQUE (ma),
    CONSTRAINT ck_pth_hinh_thuc  CHECK  (hinh_thuc_hoan IN (1, 2, 3)),
    CONSTRAINT ck_pth_trang_thai CHECK  (trang_thai IN (1, 2, 3)),
    CONSTRAINT ck_pth_tong_tien  CHECK  (tong_tien_hoan >= 0),
    CONSTRAINT fk_pth_hoa_don    FOREIGN KEY (hoa_don_id)    REFERENCES hoa_don(id),
    CONSTRAINT fk_pth_kh         FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id),
    CONSTRAINT fk_pth_nv         FOREIGN KEY (nhan_vien_id)  REFERENCES nhan_vien(id)
);
GO

-- ============================================================
-- [25] phieu_tra_hang_chi_tiet
--   trang_thai: 0 = Đã hủy dòng | 1 = Hợp lệ
-- ============================================================
CREATE TABLE phieu_tra_hang_chi_tiet (
    id                  INT           NOT NULL CONSTRAINT pk_pthct PRIMARY KEY IDENTITY(1,1),
    phieu_tra_hang_id   INT           NOT NULL,
    hoa_don_chi_tiet_id INT           NULL,
    giay_chi_tiet_id    INT           NOT NULL,
    so_luong_tra        INT           NOT NULL CONSTRAINT df_pthct_sl_tra   DEFAULT 1,
    gia_ban             DECIMAL(18,2) NOT NULL,
    thanh_tien          DECIMAL(18,2) NOT NULL,
    trang_thai          INT           NOT NULL CONSTRAINT df_pthct_trang_thai DEFAULT 1,
    -- 0 = Đã hủy dòng  |  1 = Hợp lệ
    ghi_chu             NVARCHAR(500) NULL,
    ngay_tao            DATETIME2     NOT NULL CONSTRAINT df_pthct_ngay_tao DEFAULT SYSDATETIME(),
    CONSTRAINT ck_pthct_so_luong_tra CHECK (so_luong_tra > 0),
    CONSTRAINT ck_pthct_gia_ban      CHECK (gia_ban >= 0),
    CONSTRAINT ck_pthct_thanh_tien   CHECK (thanh_tien = so_luong_tra * gia_ban),
    CONSTRAINT ck_pthct_trang_thai   CHECK (trang_thai IN (0, 1)),
    CONSTRAINT fk_pthct_phieu        FOREIGN KEY (phieu_tra_hang_id)   REFERENCES phieu_tra_hang(id)    ON DELETE CASCADE,
    CONSTRAINT fk_pthct_hdct         FOREIGN KEY (hoa_don_chi_tiet_id) REFERENCES hoa_don_chi_tiet(id),
    CONSTRAINT fk_pthct_gct          FOREIGN KEY (giay_chi_tiet_id)    REFERENCES giay_chi_tiet(id)
);
GO


-- ============================================================
-- INDEX
-- ============================================================
CREATE INDEX ix_dia_chi_kh_kh_id          ON dia_chi_khach_hang(khach_hang_id);
CREATE INDEX ix_giay_thuong_hieu_id       ON giay(thuong_hieu_id);
CREATE INDEX ix_giay_loai_giay_id         ON giay(loai_giay_id);
CREATE INDEX ix_giay_thuoc_tinh_giay_id   ON giay_thuoc_tinh(giay_id);
CREATE INDEX ix_giay_tt_de_giay_id        ON giay_thuoc_tinh(de_giay_id);
CREATE INDEX ix_giay_tt_co_giay_id        ON giay_thuoc_tinh(co_giay_id);
CREATE INDEX ix_giay_tt_trong_luong_id    ON giay_thuoc_tinh(trong_luong_id);
CREATE INDEX ix_giay_tt_cong_nghe_dem_id  ON giay_thuoc_tinh(cong_nghe_dem_id);
CREATE INDEX ix_dgg_sp_dgg_id             ON dot_giam_gia_san_pham(dot_giam_gia_id);
CREATE INDEX ix_dgg_sp_giay_id            ON dot_giam_gia_san_pham(giay_id);
CREATE INDEX ix_gct_giay_id               ON giay_chi_tiet(giay_id);
CREATE INDEX ix_gct_mau_sac_id            ON giay_chi_tiet(mau_sac_id);
CREATE INDEX ix_gct_kich_co_id            ON giay_chi_tiet(kich_co_id);
CREATE INDEX ix_hoa_don_kh_id             ON hoa_don(khach_hang_id);
CREATE INDEX ix_hoa_don_nv_id             ON hoa_don(nhan_vien_id);
CREATE INDEX ix_hoa_don_kenh_ban          ON hoa_don(kenh_ban);
CREATE INDEX ix_hdct_hoa_don_id           ON hoa_don_chi_tiet(hoa_don_id);
CREATE INDEX ix_vc_hoa_don_id             ON van_chuyen(hoa_don_id);
CREATE INDEX ix_tt_hoa_don_id             ON thanh_toan(hoa_don_id);
CREATE INDEX ix_pth_hoa_don_id            ON phieu_tra_hang(hoa_don_id);
GO

-- =============================================
-- [01] nhan_vien
-- =============================================
INSERT INTO nhan_vien (ma, ho_ten, email, mat_khau, sdt, dia_chi, vai_tro, trang_thai)
VALUES 
('NV001', N'Nguyễn Văn An', 'an.nguyen@giay.com', 'hashedpass123', '0912345678', N'Hà Nội', 1, 1),   -- Admin
('NV002', N'Trần Thị Bình', 'binh.tran@giay.com', 'hashedpass123', '0987654321', N'Hồ Chí Minh', 2, 1), -- Nhân viên
('NV003', N'Lê Hoàng Cường', 'cuong.le@giay.com', 'hashedpass123', '0978123456', N'Đà Nẵng', 2, 1),    -- Nhân viên
('NV004', N'Phạm Thị Dung', 'dung.pham@giay.com', 'hashedpass123', '0901234567', N'Hà Nội', 2, 1),     -- Nhân viên
('NV005', N'Hoàng Minh Đức', 'duc.hoang@giay.com', 'hashedpass123', '0934567890', N'Hồ Chí Minh', 1, 1),-- Admin
('NV006', N'Vũ Thị Hương', 'huong.vu@giay.com', 'hashedpass123', '0918765432', N'Cần Thơ', 2, 1),      -- Nhân viên
('NV007', N'Đặng Văn Khải', 'khai.dang@giay.com', 'hashedpass123', '0945678901', N'Hà Nội', 2, 1);     -- Nhân viên
GO

-- =============================================
-- [02] khach_hang
-- =============================================
INSERT INTO khach_hang (ten_dang_nhap, ho_ten, email, sdt, ngay_sinh, mat_khau, trang_thai)
VALUES 
('khach1', N'Nguyễn Thị Lan', 'lan.nguyen@gmail.com', '0911111111', '1995-03-15', 'pass123', 1),
('khach2', N'Trần Văn Hải', 'hai.tran@yahoo.com', '0988888888', '1998-07-20', 'pass123', 1),
('khach3', N'Lê Thị Mai', 'mai.le@hotmail.com', '0977777777', '2000-11-05', 'pass123', 1),
('khach4', N'Phạm Minh Quân', 'quan.pham@gmail.com', '0909999999', '1997-01-30', 'pass123', 1),
('khach5', N'Hoàng Thị Ngọc', 'ngoc.hoang@gmail.com', '0933333333', '1996-09-12', 'pass123', 1),
('khach6', N'Vũ Văn Long', 'long.vu@yahoo.com', '0912222222', '1999-04-18', 'pass123', 1),
('khach7', N'Đặng Thị Hạnh', 'hanh.dang@gmail.com', '0944444444', '2001-12-25', 'pass123', 1);
GO

-- =============================================
-- [03] dia_chi_khach_hang
-- =============================================
INSERT INTO dia_chi_khach_hang (khach_hang_id, ho_ten, sdt, tinh_thanh, quan_huyen, phuong_xa, dia_chi_cu_the, la_mac_dinh, trang_thai)
SELECT id, ho_ten, sdt, N'Hà Nội', N'Hoàn Kiếm', N'Phường Hàng Đào', N'12 Hàng Đào', 1, 1 FROM khach_hang WHERE ten_dang_nhap = 'khach1'
UNION ALL
SELECT id, ho_ten, sdt, N'Hồ Chí Minh', N'Quận 1', N'Phường Bến Nghé', N'45 Nguyễn Huệ', 1, 1 FROM khach_hang WHERE ten_dang_nhap = 'khach2'
UNION ALL
SELECT id, ho_ten, sdt, N'Đà Nẵng', N'Hải Châu', N'Phường Hải Châu 1', N'78 Trần Phú', 1, 1 FROM khach_hang WHERE ten_dang_nhap = 'khach3'
UNION ALL
SELECT id, ho_ten, sdt, N'Hà Nội', N'Cầu Giấy', N'Phường Yên Hòa', N'123 Xuân Thủy', 1, 1 FROM khach_hang WHERE ten_dang_nhap = 'khach4'
UNION ALL
SELECT id, ho_ten, sdt, N'Hồ Chí Minh', N'Quận 7', N'Phường Tân Phong', N'67 Nguyễn Lương Bằng', 1, 1 FROM khach_hang WHERE ten_dang_nhap = 'khach5'
UNION ALL
SELECT id, ho_ten, sdt, N'Cần Thơ', N'Ninh Kiều', N'Phường An Bình', N'89 Võ Văn Tần', 1, 1 FROM khach_hang WHERE ten_dang_nhap = 'khach6'
UNION ALL
SELECT id, ho_ten, sdt, N'Hà Nội', N'Ba Đình', N'Phường Trúc Bạch', N'34 Trúc Bạch', 1, 1 FROM khach_hang WHERE ten_dang_nhap = 'khach7';
GO

-- =============================================
-- [04] kich_co
-- =============================================
INSERT INTO kich_co (gia_tri, ghi_chu, trang_thai)
VALUES 
('38', N'Size nữ nhỏ', 1),
('39', N'Size nữ', 1),
('40', N'Size unisex phổ biến', 1),
('41', N'Size nam', 1),
('42', N'Size nam', 1),
('43', N'Size nam lớn', 1),
('44', N'Size nam lớn', 1);
GO

-- =============================================
-- [05] mau_sac
-- =============================================
INSERT INTO mau_sac (ma, ten, ma_mau_hex, trang_thai)
VALUES 
('MS001', N'Trắng', '#FFFFFF', 1),
('MS002', N'Đen', '#000000', 1),
('MS003', N'Xanh Navy', '#001F3F', 1),
('MS004', N'Đỏ', '#FF0000', 1),
('MS005', N'Xám', '#808080', 1),
('MS006', N'Xanh Lá', '#00FF00', 1),
('MS007', N'Hồng', '#FFC0CB', 1);
GO

-- =============================================
-- [06] thuong_hieu 
-- =============================================
INSERT INTO thuong_hieu (ma, ten, xuat_xu, mo_ta, logo_url, website, trang_thai)
VALUES 
('TH001', N'Nike', N'Mỹ', N'Thương hiệu giày thể thao hàng đầu thế giới', 'https://example.com/nike-logo.png', 'https://nike.com', 1),
('TH002', N'Adidas', N'Đức', N'Performance và Originals', 'https://example.com/adidas-logo.png', 'https://adidas.com', 1),
('TH003', N'New Balance', N'Mỹ', N'Chuyên giày chạy bộ và lifestyle', 'https://example.com/nb-logo.png', 'https://newbalance.com', 1),
('TH004', N'Puma', N'Đức', N'Thể thao và thời trang', 'https://example.com/puma-logo.png', 'https://puma.com', 1),
('TH005', N'Asics', N'Nhật Bản', N'Giày chạy bộ chuyên nghiệp', 'https://example.com/asics-logo.png', 'https://asics.com', 1),
('TH006', N'Vans', N'Mỹ', N'Skate và casual', 'https://example.com/vans-logo.png', 'https://vans.com', 1),
('TH007', N'Converse', N'Mỹ', N'All Star kinh điển', 'https://example.com/converse-logo.png', 'https://converse.com', 1);
GO

-- =============================================
-- [07] loai_giay 
-- =============================================
INSERT INTO loai_giay (ma, ten, mo_ta, trang_thai)
VALUES 
('LG001', N'Running', N'Giày chạy bộ', 1),
('LG002', N'Basketball', N'Giày bóng rổ', 1),
('LG003', N'Lifestyle', N'Giày thời trang hàng ngày', 1),
('LG004', N'Skateboarding', N'Giày trượt ván', 1),
('LG005', N'Training', N'Giày tập gym', 1),
('LG006', N'Casual', N'Giày đi chơi', 1),
('LG007', N'Walking', N'Giày đi bộ', 1);
GO

-- =============================================
-- [08] de_giay 
-- =============================================
INSERT INTO de_giay (ma, ten, mo_ta, trang_thai)
VALUES 
('DG001', N'Phylon', N'Đế nhẹ, đàn hồi tốt', 1),
('DG002', N'Rubber', N'Đế cao su bền bỉ', 1),
('DG003', N'EVA', N'Đế EVA nhẹ và êm', 1),
('DG004', N'Boost', N'Đế Boost siêu đàn hồi', 1),
('DG005', N'Zoom Air', N'Đế có túi khí', 1),
('DG006', N'Gel', N'Đế Gel giảm chấn', 1),
('DG007', N'FuelCell', N'Đế PEBA cao cấp', 1);
GO

-- =============================================
-- [09] co_giay 
-- =============================================
INSERT INTO co_giay (ma, ten, mo_ta, trang_thai)
VALUES 
('CG001', N'Low', N'Cổ thấp', 1),
('CG002', N'Mid', N'Cổ trung', 1),
('CG003', N'High', N'Cổ cao', 1),
('CG004', N'Slip-on', N'Không dây', 1),
('CG005', N'Ankle', N'Cổ ngắn', 1),
('CG006', N'Boot', N'Phong cách boot', 1),
('CG007', N'Low-top', N'Cổ thấp thể thao', 1);
GO

-- =============================================
-- [10] trong_luong 
-- =============================================
INSERT INTO trong_luong (ma, gia_tri, mo_ta, trang_thai)
VALUES 
('TL001', 280, N'Nhẹ cho chạy bộ', 1),
('TL002', 320, N'Trung bình', 1),
('TL003', 350, N'Bền bỉ', 1),
('TL004', 400, N'Heavy duty', 1),
('TL005', 250, N'Rất nhẹ', 1),
('TL006', 300, N'Phổ biến', 1),
('TL007', 270, N'Nhẹ cho nữ', 1);
GO

-- =============================================
-- [11] cong_nghe_dem 
-- =============================================
INSERT INTO cong_nghe_dem (ma, ten, mo_ta, trang_thai)
VALUES 
('CND001', N'Zoom Air', N'Túi khí Nike tăng độ bật', 1),
('CND002', N'Boost', N'Đệm năng lượng Adidas', 1),
('CND003', N'Fresh Foam', N'Đệm êm New Balance', 1),
('CND004', N'Gel Cushioning', N'Giảm chấn Asics', 1),
('CND005', N'React', N'Đệm phản hồi Nike', 1),
('CND006', N'FuelCell', N'Đệm PEBA New Balance', 1),
('CND007', N' EVA Foam', N'Đệm cơ bản nhẹ', 1);
GO

-- =============================================
-- [12] dot_giam_gia 
-- =============================================
INSERT INTO dot_giam_gia (ma, ten, mo_ta, loai_giam, gia_tri_giam, ngay_bat_dau, ngay_ket_thuc, kich_hoat)
VALUES 
('DGG001', N'Giảm 20% Toàn Shop', N'Khuyến mãi lớn', 1, 20, '2026-04-01', '2026-04-30', 1),
('DGG002', N'Giảm 500k Nike', N'Áp dụng cho Nike', 2, 500000, '2026-04-05', '2026-05-05', 1),
('DGG003', N'Flash Sale 15%', N'Bán nhanh', 1, 15, '2026-04-08', '2026-04-10', 1),
('DGG004', N'Giảm 300k Adidas', N'Chỉ Adidas', 2, 300000, '2026-03-01', '2026-06-01', 1),
('DGG005', N'Miễn phí ship', N'Đơn trên 1 triệu', 2, 50000, NULL, NULL, 1),  -- giả sử số tiền
('DGG006', N'Giảm 10% Thành Viên', N'Khách VIP', 1, 10, '2026-01-01', '2026-12-31', 1),
('DGG007', N'Sale Cuối Tháng', N'Giảm 25%', 1, 25, '2026-04-25', '2026-04-30', 1);
GO

-- =============================================
-- [13] giay 
-- =============================================
INSERT INTO giay (ma, ten, thuong_hieu_id, loai_giay_id, gioi_tinh, chat_lieu, mo_ta, trang_thai)
VALUES 
('G001', N'Nike Air Force 1', 1, 3, 3, N'Da tổng hợp', N'Giày thể thao kinh điển', 1),
('G002', N'Adidas Samba OG', 2, 3, 3, N'Da lộn + Cao su', N'Phong cách retro', 1),
('G003', N'New Balance 530', 3, 1, 1, N'Mesh + Da', N'Giày chạy bộ hàng ngày', 1),
('G004', N'Puma RS-X', 4, 5, 2, N'Mesh', N'Giày training', 1),
('G005', N'Asics Gel-Kayano 30', 5, 1, 1, N'FlyteFoam', N'Giày chạy bộ hỗ trợ', 1),
('G006', N'Vans Old Skool', 6, 4, 3, N'Canvas', N'Giày skate kinh điển', 1),
('G007', N'Converse Chuck 70', 7, 3, 3, N'Canvas', N'All Star cao cấp', 1);
GO

-- =============================================
-- [14] giay_thuoc_tinh 
-- =============================================
INSERT INTO giay_thuoc_tinh (giay_id, de_giay_id, co_giay_id, trong_luong_id, cong_nghe_dem_id, trang_thai)
VALUES 
(1, 5, 1, 2, 1, 1),   -- Nike AF1
(2, 2, 1, 3, 2, 1),   -- Adidas Samba
(3, 3, 1, 1, 3, 1),   -- NB 530
(4, 1, 2, 4, 5, 1),   -- Puma
(5, 6, 1, 2, 4, 1),   -- Asics
(6, 2, 1, 5, 7, 1),   -- Vans
(7, 2, 3, 3, 7, 1);   -- Converse
GO

-- =============================================
-- [15] dot_giam_gia_san_pham 
-- =============================================
INSERT INTO dot_giam_gia_san_pham (dot_giam_gia_id, giay_id, trang_thai)
VALUES 
(1,1,1),(1,2,1),(2,1,1),(3,3,1),(4,2,1),(5,4,1),(6,5,1);
GO

-- =============================================
-- [16] giay_chi_tiet 
-- =============================================
INSERT INTO giay_chi_tiet (giay_id, ma_bien_the, mau_sac_id, kich_co_id, so_luong, gia_goc, gia_ban, sku, kich_hoat)
VALUES 
(1, 'AF1-WH-40', 1, 4, 50, 2500000, 2290000, 'AF1WH40', 1),
(2, 'SAM-BL-41', 2, 5, 30, 2300000, 1990000, 'SAMBL41', 1),
(3, 'NB530-GR-42', 5, 6, 40, 2800000, 2590000, 'NB530GR42', 1),
(4, 'RSX-PI-39', 7, 3, 25, 1800000, 1590000, 'RSXPI39', 1),
(5, 'GEL-BL-43', 2, 7, 35, 4200000, 3890000, 'GELBL43', 1),
(6, 'VANS-BK-40', 2, 4, 60, 1500000, 1390000, 'VANSBK40', 1),
(7, 'CH70-WH-41', 1, 5, 45, 1700000, 1490000, 'CH70WH41', 1);
GO

-- =============================================
-- [17] hinh_anh_giay
-- =============================================
INSERT INTO hinh_anh_giay (giay_chi_tiet_id, loai_hinh, url, mo_ta, la_hinh_chinh, trang_thai)
VALUES 
(1, 1, 'https://example.com/af1-white-main.jpg', N'Ảnh chính AF1 trắng', 1, 1),
(2, 1, 'https://example.com/samba-black-main.jpg', N'Ảnh chính Samba đen', 1, 1),
(3, 1, 'https://example.com/nb530-grey-main.jpg', N'Ảnh chính NB530 xám', 1, 1),
(4, 1, 'https://example.com/rsx-pink-main.jpg', N'Ảnh chính RS-X hồng', 1, 1),
(5, 1, 'https://example.com/gel-kayano-black-main.jpg', N'Ảnh chính Gel Kayano đen', 1, 1),
(6, 1, 'https://example.com/vans-black-main.jpg', N'Ảnh chính Vans đen', 1, 1),
(7, 1, 'https://example.com/chuck70-white-main.jpg', N'Ảnh chính Chuck 70 trắng', 1, 1);
GO

-- =============================================
-- [18] phieu_giam_gia
-- =============================================
INSERT INTO phieu_giam_gia (ma, ten, loai, gia_tri, gia_tri_toi_thieu, giam_toi_da, ngay_bat_dau, ngay_ket_thuc, so_luong, trang_thai)
VALUES 
('PGG001', N'Giảm 10% đơn đầu', 1, 10, 500000, 300000, '2026-04-01', '2026-06-01', 100, 1),
('PGG002', N'Giảm 200k', 2, 200000, 1000000, NULL, '2026-04-01', '2026-04-30', 50, 1),
('PGG003', N'Miễn phí ship', 3, 0, 800000, NULL, '2026-01-01', '2026-12-31', 200, 1),
('PGG004', N'Giảm 15%', 1, 15, 1500000, 500000, '2026-03-15', '2026-05-15', 80, 1),
('PGG005', N'Giảm 150k', 2, 150000, 500000, NULL, NULL, NULL, 30, 1),
('PGG006', N'Giảm 20% cho thành viên', 1, 20, 2000000, 600000, '2026-04-01', '2026-04-15', 40, 1),
('PGG007', N'Giảm 300k Black Friday', 2, 300000, 2500000, NULL, '2026-11-20', '2026-11-30', 25, 1);
GO

-- =============================================
-- [19] phieu_giam_gia_khach_hang
-- =============================================
INSERT INTO phieu_giam_gia_khach_hang (phieu_giam_gia_id, khach_hang_id, trang_thai)
SELECT 1, id, 1 FROM khach_hang WHERE ten_dang_nhap IN ('khach1','khach2','khach3')
UNION ALL
SELECT 2, id, 1 FROM khach_hang WHERE ten_dang_nhap IN ('khach4','khach5')
UNION ALL
SELECT 3, id, 1 FROM khach_hang WHERE ten_dang_nhap IN ('khach6','khach7');
GO

-- =============================================
-- [20..25] Các bảng nghiệp vụ 
-- =============================================
-- Hoa don 
INSERT INTO hoa_don (ma, kenh_ban, khach_hang_id, nhan_vien_id, phieu_giam_gia_id, ten_nguoi_nhan, sdt_nguoi_nhan, dia_chi_giao_hang, trang_thai, tong_tien_hang, tien_giam, tong_tien_thanh_toan)
VALUES 
('HD001', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach1'), (SELECT id FROM nhan_vien WHERE ma='NV002'), 1, N'Nguyễn Thị Lan', '0911111111', N'12 Hàng Đào, Hà Nội', 4, 4500000, 450000, 4050000),
('HD002', 1, NULL, (SELECT id FROM nhan_vien WHERE ma='NV001'), NULL, N'Trần Văn Hải', '0988888888', N'45 Nguyễn Huệ, HCM', 2, 3200000, 0, 3200000),
('HD003', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach3'), (SELECT id FROM nhan_vien WHERE ma='NV003'), 2, N'Lê Thị Mai', '0977777777', N'78 Trần Phú, Đà Nẵng', 3, 2800000, 200000, 2600000),
('HD004', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach4'), NULL, NULL, N'Phạm Minh Quân', '0909999999', N'123 Xuân Thủy, Hà Nội', 4, 5500000, 0, 5500000),
('HD005', 1, NULL, (SELECT id FROM nhan_vien WHERE ma='NV002'), 3, N'Hoàng Thị Ngọc', '0933333333', N'67 Nguyễn Lương Bằng, HCM', 2, 1800000, 0, 1800000),
('HD006', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach6'), (SELECT id FROM nhan_vien WHERE ma='NV005'), NULL, N'Vũ Văn Long', '0912222222', N'89 Võ Văn Tần, Cần Thơ', 1, 3900000, 0, 3900000),
('HD007', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach7'), NULL, 4, N'Đặng Thị Hạnh', '0944444444', N'34 Trúc Bạch, Hà Nội', 4, 6200000, 930000, 5270000);
GO
-- =============================================
-- [21] hoa_don_chi_tiet
-- =============================================
INSERT INTO hoa_don_chi_tiet (hoa_don_id, giay_chi_tiet_id, so_luong, gia_don_vi, thanh_tien, trang_thai)
VALUES 
(1, 1, 1, 2290000, 2290000, 1),   -- HD001 mua AF1 trắng size 40
(1, 6, 1, 1390000, 1390000, 1),   -- + Vans đen
(2, 2, 2, 1990000, 3980000, 1),   -- HD002 mua 2 đôi Samba
(3, 3, 1, 2590000, 2590000, 1),   -- HD003 mua NB530
(4, 5, 1, 3890000, 3890000, 1),   -- HD004 mua Asics Gel
(5, 4, 1, 1590000, 1590000, 1),   -- HD005 mua Puma RS-X
(6, 7, 2, 1490000, 2980000, 1),   -- HD006 mua 2 đôi Converse
(7, 1, 1, 2290000, 2290000, 1);   -- HD007 mua AF1
GO

-- =============================================
-- [22] van_chuyen
-- =============================================
INSERT INTO van_chuyen (hoa_don_id, don_vi_van_chuyen, ma_van_don, phi_van_chuyen, ngay_gui, ngay_du_kien, ngay_giao_that, trang_thai, ghi_chu)
VALUES 
(1, N'Giao Hàng Nhanh (GHN)', 'GHN123456789', 35000, '2026-04-02', '2026-04-05', '2026-04-04', 3, N'Giao thành công'),
(2, N'GHTK', 'GHTK987654', 0, '2026-04-03', '2026-04-04', NULL, 1, N'Đang chuẩn bị lấy hàng'),
(3, N'Viettel Post', 'VT123987', 30000, '2026-04-01', '2026-04-06', '2026-04-05', 3, N'Đã giao'),
(4, N'J&T Express', 'JT456789', 25000, '2026-04-04', '2026-04-07', NULL, 2, N'Đang vận chuyển'),
(5, N'Giao Hàng Nhanh (GHN)', 'GHN111222', 0, '2026-04-05', '2026-04-06', '2026-04-06', 3, N'Giao tại quầy'),
(6, N'GHN', 'GHN333444', 40000, '2026-04-06', '2026-04-09', NULL, 2, N'Đang giao'),
(7, N'Viettel Post', 'VT555666', 30000, '2026-04-02', '2026-04-05', '2026-04-04', 3, N'Hoàn thành');
GO

-- =============================================
-- [23] thanh_toan
-- =============================================
INSERT INTO thanh_toan (hoa_don_id, nhan_vien_id, ma_giao_dich, hinh_thuc, so_tien, tien_thoi_lai, ngan_hang, noi_dung_ck, cong_thanh_toan, ngay_thanh_toan, trang_thai, ghi_chu)
VALUES 
(1, (SELECT id FROM nhan_vien WHERE ma='NV002'), 'MOMO-20260402-001', 2, 4050000, 0, NULL, NULL, N'Momo', '2026-04-02 14:30:00', 1, N'Thanh toán online'),
(2, (SELECT id FROM nhan_vien WHERE ma='NV001'), NULL, 1, 3200000, 0, NULL, NULL, NULL, '2026-04-03 10:15:00', 1, N'Tiền mặt tại quầy'),
(3, (SELECT id FROM nhan_vien WHERE ma='NV003'), 'VNPAY-20260401-003', 2, 2600000, 0, NULL, NULL, N'VNPay', '2026-04-01 16:45:00', 1, NULL),
(4, NULL, 'BANK-20260404-004', 2, 5500000, 0, N'Vietcombank', N'HD004 - Nguyen Minh Quan', NULL, '2026-04-04 09:20:00', 1, N'Chuyển khoản'),
(5, (SELECT id FROM nhan_vien WHERE ma='NV002'), NULL, 1, 1800000, 200000, NULL, NULL, NULL, '2026-04-05 11:00:00', 1, N'Tiền mặt, trả lại 200k'),
(6, NULL, 'ZALOPAY-20260406-006', 3, 3900000, 0, NULL, NULL, N'ZaloPay', '2026-04-06 20:10:00', 1, NULL),
(7, (SELECT id FROM nhan_vien WHERE ma='NV005'), 'BANK-20260402-007', 2, 5270000, 0, N'Techcombank', N'Thanh toán HD007', NULL, '2026-04-02 15:55:00', 1, NULL);
GO

-- =============================================
-- [24] phieu_tra_hang   (ĐÃ SỬA)
-- =============================================
INSERT INTO phieu_tra_hang (ma, hoa_don_id, khach_hang_id, nhan_vien_id, ly_do, tong_tien_hoan, hinh_thuc_hoan, trang_thai)
VALUES 
('PTH001', 1, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach1'), (SELECT id FROM nhan_vien WHERE ma='NV004'), N'Sản phẩm bị lỗi size', 2290000, 2, 2),
('PTH002', 3, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach3'), (SELECT id FROM nhan_vien WHERE ma='NV003'), N'Đổi màu khác', 2590000, 1, 1),
('PTH003', 4, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach4'), NULL, N'Không vừa chân', 3890000, 2, 3),
('PTH004', 5, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach5'), (SELECT id FROM nhan_vien WHERE ma='NV002'), N'Hàng lỗi nhỏ', 1590000, 3, 2),
('PTH005', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach2'), (SELECT id FROM nhan_vien WHERE ma='NV001'), N'Khách đổi ý', 1990000, 1, 1),   -- SỬA: Dùng khach2 thay vì NULL
('PTH006', 7, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach7'), (SELECT id FROM nhan_vien WHERE ma='NV005'), N'Sai mẫu', 2290000, 2, 2),
('PTH007', 6, (SELECT id FROM khach_hang WHERE ten_dang_nhap='khach6'), NULL, N'Đổi size lớn hơn', 1490000, 1, 1);
GO

-- =============================================
-- [25] phieu_tra_hang_chi_tiet   (ĐÃ SỬA)
-- =============================================
INSERT INTO phieu_tra_hang_chi_tiet (phieu_tra_hang_id, hoa_don_chi_tiet_id, giay_chi_tiet_id, so_luong_tra, gia_ban, thanh_tien, trang_thai, ghi_chu)
VALUES 
(1, 1, 1, 1, 2290000, 2290000, 1, N'Trả toàn bộ'),
(2, 4, 3, 1, 2590000, 2590000, 1, NULL),
(3, 5, 5, 1, 3890000, 3890000, 1, N'Từ chối trả'),
(4, 6, 4, 1, 1590000, 1590000, 1, N'Hoàn tiền thành công'),
(5, 3, 2, 1, 1990000, 1990000, 1, N'Đổi ý'),
(6, 8, 1, 1, 2290000, 2290000, 1, NULL),
(7, 7, 7, 1, 1490000, 1490000, 1, N'Đổi size');
GO
USE giay;
GO

-- [01] nhan_vien
SELECT 
    id, ma, ho_ten, email, mat_khau, sdt, dia_chi, 
    vai_tro, trang_thai, ngay_tao, ngay_cap_nhat
FROM nhan_vien;
GO

-- [02] khach_hang
SELECT 
    id, ten_dang_nhap, ho_ten, email, sdt, ngay_sinh, 
    mat_khau, trang_thai, ngay_tao, ngay_cap_nhat
FROM khach_hang;
GO

-- [03] dia_chi_khach_hang
SELECT 
    id, khach_hang_id, ho_ten, sdt, tinh_thanh, quan_huyen, 
    phuong_xa, dia_chi_cu_the, la_mac_dinh, trang_thai, 
    ngay_tao, ngay_cap_nhat
FROM dia_chi_khach_hang;
GO

-- [04] kich_co
SELECT 
    id, gia_tri, ghi_chu, trang_thai, ngay_tao, ngay_cap_nhat
FROM kich_co;
GO

-- [05] mau_sac
SELECT 
    id, ma, ten, ma_mau_hex, trang_thai, ngay_tao, ngay_cap_nhat
FROM mau_sac;
GO

-- [06] thuong_hieu
SELECT 
    id, ma, ten, xuat_xu, mo_ta, logo_url, website, 
    trang_thai, ngay_tao, ngay_cap_nhat
FROM thuong_hieu;
GO

-- [07] loai_giay
SELECT 
    id, ma, ten, mo_ta, trang_thai, ngay_tao, ngay_cap_nhat
FROM loai_giay;
GO

-- [08] de_giay
SELECT 
    id, ma, ten, mo_ta, trang_thai, ngay_tao, ngay_cap_nhat
FROM de_giay;
GO

-- [09] co_giay
SELECT 
    id, ma, ten, mo_ta, trang_thai, ngay_tao, ngay_cap_nhat
FROM co_giay;
GO

-- [10] trong_luong
SELECT 
    id, ma, gia_tri, mo_ta, trang_thai, ngay_tao, ngay_cap_nhat
FROM trong_luong;
GO

-- [11] cong_nghe_dem
SELECT 
    id, ma, ten, mo_ta, trang_thai, ngay_tao, ngay_cap_nhat
FROM cong_nghe_dem;
GO

-- [12] dot_giam_gia
SELECT 
    id, ma, ten, mo_ta, loai_giam, gia_tri_giam, 
    ngay_bat_dau, ngay_ket_thuc, kich_hoat, ngay_tao, ngay_cap_nhat
FROM dot_giam_gia;
GO

-- [13] giay
SELECT 
    id, ma, ten, thuong_hieu_id, loai_giay_id, gioi_tinh, 
    chat_lieu, mo_ta, trang_thai, ngay_tao, ngay_cap_nhat
FROM giay;
GO

-- [14] giay_thuoc_tinh
SELECT 
    id, giay_id, de_giay_id, co_giay_id, trong_luong_id, 
    cong_nghe_dem_id, trang_thai, ngay_tao, ngay_cap_nhat
FROM giay_thuoc_tinh;
GO

-- [15] dot_giam_gia_san_pham
SELECT 
    id, dot_giam_gia_id, giay_id, trang_thai, ngay_tao
FROM dot_giam_gia_san_pham;
GO

-- [16] giay_chi_tiet
SELECT 
    id, giay_id, ma_bien_the, mau_sac_id, kich_co_id, 
    so_luong, gia_goc, gia_ban, sku, kich_hoat, 
    ngay_tao, ngay_cap_nhat
FROM giay_chi_tiet;
GO

-- [17] hinh_anh_giay
SELECT 
    id, giay_chi_tiet_id, loai_hinh, url, mo_ta, 
    la_hinh_chinh, trang_thai, ngay_tao, ngay_cap_nhat
FROM hinh_anh_giay;
GO

-- [18] phieu_giam_gia
SELECT 
    id, ma, ten, loai, gia_tri, gia_tri_toi_thieu, giam_toi_da, 
    ngay_bat_dau, ngay_ket_thuc, so_luong, so_luong_da_dung, 
    trang_thai, ngay_tao, ngay_cap_nhat
FROM phieu_giam_gia;
GO

-- [19] phieu_giam_gia_khach_hang
SELECT 
    id, phieu_giam_gia_id, khach_hang_id, ngay_su_dung, 
    trang_thai, ngay_tao
FROM phieu_giam_gia_khach_hang;
GO

-- [20] hoa_don
SELECT 
    id, ma, kenh_ban, khach_hang_id, nhan_vien_id, phieu_giam_gia_id,
    ten_nguoi_nhan, sdt_nguoi_nhan, dia_chi_giao_hang, ngay_lap, 
    ngay_thanh_toan, trang_thai, tong_tien_hang, tien_giam, 
    tong_tien_thanh_toan, ghi_chu, ngay_tao, ngay_cap_nhat
FROM hoa_don;
GO

-- [21] hoa_don_chi_tiet
SELECT 
    id, hoa_don_id, giay_chi_tiet_id, so_luong, gia_don_vi, 
    thanh_tien, trang_thai, ngay_tao
FROM hoa_don_chi_tiet;
GO

-- [22] van_chuyen
SELECT 
    id, hoa_don_id, don_vi_van_chuyen, ma_van_don, phi_van_chuyen,
    ngay_gui, ngay_du_kien, ngay_giao_that, trang_thai, ghi_chu, 
    ngay_tao, ngay_cap_nhat
FROM van_chuyen;
GO

-- [23] thanh_toan
SELECT 
    id, hoa_don_id, nhan_vien_id, ma_giao_dich, hinh_thuc, so_tien, 
    tien_thoi_lai, ngan_hang, noi_dung_ck, cong_thanh_toan, 
    ngay_thanh_toan, trang_thai, ghi_chu, ngay_tao
FROM thanh_toan;
GO

-- [24] phieu_tra_hang
SELECT 
    id, ma, hoa_don_id, khach_hang_id, nhan_vien_id, ly_do, 
    tong_tien_hoan, hinh_thuc_hoan, trang_thai, ngay_tao, ngay_cap_nhat
FROM phieu_tra_hang;
GO

-- [25] phieu_tra_hang_chi_tiet
SELECT 
    id, phieu_tra_hang_id, hoa_don_chi_tiet_id, giay_chi_tiet_id, 
    so_luong_tra, gia_ban, thanh_tien, trang_thai, ghi_chu, ngay_tao
FROM phieu_tra_hang_chi_tiet;
GO

-- ==================== THÊM DANH MỤC MỚI ====================

-- Thêm Kích cỡ mới
INSERT INTO kich_co (gia_tri, ghi_chu, trang_thai)
VALUES 
('36', N'Size nữ rất nhỏ', 1),
('37', N'Size nữ nhỏ', 1),
('45', N'Size nam lớn', 1);

-- Thêm Màu sắc mới
INSERT INTO mau_sac (ma, ten, ma_mau_hex, trang_thai)
VALUES 
('MS008', N'Xanh Dương', '#1E90FF', 1),
('MS009', N'Cam', '#FF7F00', 1),
('MS010', N'Tím', '#8A2BE2', 1),
('MS011', N'Vàng', '#FFD700', 1),
('MS012', N'Nâu', '#8B4513', 1);

-- Thêm Đế giày mới
INSERT INTO de_giay (ma, ten, mo_ta, trang_thai)
VALUES 
('DG008', N'Air Max', N'Đế khí lớn Nike Air Max', 1),
('DG009', N'React Foam', N'Đệm React siêu nhẹ và êm', 1);

-- Thêm Cổ giày mới
INSERT INTO co_giay (ma, ten, mo_ta, trang_thai)
VALUES 
('CG008', N'Mid-top', N'Cổ trung cao', 1),
('CG009', N'Flyknit', N'Cổ dệt Flyknit ôm chân', 1);

-- Thêm Trọng lượng mới
INSERT INTO trong_luong (ma, gia_tri, mo_ta, trang_thai)
VALUES 
('TL008', 260, N'Rất nhẹ Air Max', 1),
('TL009', 290, N'Nhẹ trung bình', 1);


-- Thêm Loại giày mới (nếu muốn)
INSERT INTO loai_giay (ma, ten, mo_ta, trang_thai)
VALUES 
('LG008', N'Running Cushion', N'Giày chạy bộ có đệm tối đa', 1);

-- ==================== THÊM SẢN PHẨM MỚI ====================

INSERT INTO giay (ma, ten, thuong_hieu_id, loai_giay_id, gioi_tinh, chat_lieu, mo_ta, trang_thai)
VALUES 
('G008', N'Nike Air Max 270', 1, 1, 3, N'Flyknit + Da tổng hợp', 
 N'Giày chạy bộ và lifestyle với đệm Air Max lớn, mang lại cảm giác êm ái và phong cách nổi bật.', 1);

 -- ==================== THÊM BIẾN THỂ (giay_chi_tiet) ====================

INSERT INTO giay_chi_tiet 
(giay_id, ma_bien_the, mau_sac_id, kich_co_id, so_luong, gia_goc, gia_ban, sku, kich_hoat)
VALUES

-- Màu Trắng (MS001)
((SELECT id FROM giay WHERE ma = 'G008'), 'AM270-WH-40', 
 (SELECT id FROM mau_sac WHERE ma = 'MS001'), 
 (SELECT id FROM kich_co WHERE gia_tri = '40'), 45, 3200000, 2890000, 'AM270WH40', 1),

((SELECT id FROM giay WHERE ma = 'G008'), 'AM270-WH-41', 
 (SELECT id FROM mau_sac WHERE ma = 'MS001'), 
 (SELECT id FROM kich_co WHERE gia_tri = '41'), 50, 3200000, 2890000, 'AM270WH41', 1),

-- Màu Đen (MS002)
((SELECT id FROM giay WHERE ma = 'G008'), 'AM270-BK-40', 
 (SELECT id FROM mau_sac WHERE ma = 'MS002'), 
 (SELECT id FROM kich_co WHERE gia_tri = '40'), 30, 3200000, 2890000, 'AM270BK40', 1),

((SELECT id FROM giay WHERE ma = 'G008'), 'AM270-BK-42', 
 (SELECT id FROM mau_sac WHERE ma = 'MS002'), 
 (SELECT id FROM kich_co WHERE gia_tri = '42'), 35, 3200000, 2890000, 'AM270BK42', 1),

-- Màu Xanh Dương (MS008)
((SELECT id FROM giay WHERE ma = 'G008'), 'AM270-BL-39', 
 (SELECT id FROM mau_sac WHERE ma = 'MS008'), 
 (SELECT id FROM kich_co WHERE gia_tri = '39'), 25, 3200000, 2890000, 'AM270BL39', 1),

((SELECT id FROM giay WHERE ma = 'G008'), 'AM270-BL-41', 
 (SELECT id FROM mau_sac WHERE ma = 'MS008'), 
 (SELECT id FROM kich_co WHERE gia_tri = '41'), 40, 3200000, 2890000, 'AM270BL41', 1),

-- Màu Cam (MS009)
((SELECT id FROM giay WHERE ma = 'G008'), 'AM270-OR-40', 
 (SELECT id FROM mau_sac WHERE ma = 'MS009'), 
 (SELECT id FROM kich_co WHERE gia_tri = '40'), 20, 3200000, 2890000, 'AM270OR40', 1),

-- Màu Hồng (MS007) - size nữ
((SELECT id FROM giay WHERE ma = 'G008'), 'AM270-PK-37', 
 (SELECT id FROM mau_sac WHERE ma = 'MS007'), 
 (SELECT id FROM kich_co WHERE gia_tri = '37'), 15, 3200000, 2890000, 'AM270PK37', 1);

 -- Thêm hình ảnh (chỉ ví dụ cho vài biến thể)
INSERT INTO hinh_anh_giay (giay_chi_tiet_id, loai_hinh, url, mo_ta, la_hinh_chinh, trang_thai)
VALUES
-- Biến thể AM270-WH-40
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = 'AM270-WH-40'), 1, 
 'https://example.com/airmax270-white-main.jpg', N'Ảnh chính Nike Air Max 270 Trắng', 1, 1),

((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = 'AM270-WH-40'), 2, 
 'https://example.com/airmax270-white-side.jpg', N'Ảnh ngang', 0, 1),

-- Biến thể AM270-BK-40
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = 'AM270-BK-40'), 1, 
 'https://example.com/airmax270-black-main.jpg', N'Ảnh chính Nike Air Max 270 Đen', 1, 1),

-- Biến thể AM270-BL-41
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = 'AM270-BL-41'), 1, 
 'https://example.com/airmax270-blue-main.jpg', N'Ảnh chính Nike Air Max 270 Xanh Dương', 1, 1);
