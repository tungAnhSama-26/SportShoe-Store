IF OBJECT_ID(N'dbo.chat_lieu_giay', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.chat_lieu_giay (
        id            INT           NOT NULL CONSTRAINT pk_chat_lieu_giay PRIMARY KEY IDENTITY(1,1),
        ma            NVARCHAR(50)  NOT NULL,
        ten           NVARCHAR(100) NOT NULL,
        mo_ta         NVARCHAR(300) NULL,
        trang_thai    INT           NOT NULL CONSTRAINT df_chat_lieu_giay_trang_thai DEFAULT 1,
        ngay_tao      DATETIME2     NOT NULL CONSTRAINT df_chat_lieu_giay_ngay_tao DEFAULT SYSDATETIME(),
        ngay_cap_nhat DATETIME2     NULL,
        CONSTRAINT uq_chat_lieu_giay_ma UNIQUE (ma),
        CONSTRAINT uq_chat_lieu_giay_ten UNIQUE (ten),
        CONSTRAINT ck_chat_lieu_giay_trang_thai CHECK (trang_thai IN (0, 1))
    );
END
GO

IF COL_LENGTH(N'dbo.giay_thuoc_tinh', N'chat_lieu_giay_id') IS NULL
BEGIN
    ALTER TABLE dbo.giay_thuoc_tinh
    ADD chat_lieu_giay_id INT NULL;
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = N'fk_giay_tt_chat_lieu_giay'
)
BEGIN
    ALTER TABLE dbo.giay_thuoc_tinh
    ADD CONSTRAINT fk_giay_tt_chat_lieu_giay
        FOREIGN KEY (chat_lieu_giay_id) REFERENCES dbo.chat_lieu_giay(id);
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'ix_giay_tt_chat_lieu_giay_id'
      AND object_id = OBJECT_ID(N'dbo.giay_thuoc_tinh')
)
BEGIN
    CREATE INDEX ix_giay_tt_chat_lieu_giay_id ON dbo.giay_thuoc_tinh(chat_lieu_giay_id);
END
GO

MERGE dbo.chat_lieu_giay AS target
USING (
    VALUES
        (N'CLG001', N'Da tổng hợp', N'Chất liệu da tổng hợp bền và dễ vệ sinh', 1),
        (N'CLG002', N'Da lộn + Cao su', N'Phối da lộn với bề mặt cao su', 1),
        (N'CLG003', N'Mesh + Da', N'Lưới thoáng khí kết hợp các mảng da', 1),
        (N'CLG004', N'Mesh', N'Vải lưới thoáng khí nhẹ chân', 1),
        (N'CLG005', N'FlyteFoam', N'Chất liệu foam nhẹ dùng cho giày chạy bộ', 1),
        (N'CLG006', N'Canvas', N'Vải canvas bền cho giày lifestyle', 1),
        (N'CLG007', N'Flyknit + Da tổng hợp', N'Vải dệt kết hợp da tổng hợp', 1)
) AS source(ma, ten, mo_ta, trang_thai)
ON target.ma = source.ma
WHEN MATCHED THEN
    UPDATE SET
        ten = source.ten,
        mo_ta = source.mo_ta,
        trang_thai = source.trang_thai,
        ngay_cap_nhat = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (ma, ten, mo_ta, trang_thai)
    VALUES (source.ma, source.ten, source.mo_ta, source.trang_thai);
GO

UPDATE gtt
SET gtt.chat_lieu_giay_id = clg.id
FROM dbo.giay_thuoc_tinh AS gtt
JOIN dbo.giay AS g
    ON g.id = gtt.giay_id
JOIN dbo.chat_lieu_giay AS clg
    ON clg.ten = g.chat_lieu
WHERE gtt.chat_lieu_giay_id IS NULL
  AND g.chat_lieu IS NOT NULL;
GO
