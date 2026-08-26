-- Migration: Sua rang buoc bang thanh_toan de cho phep so tien >= 0 va hinh thuc 5 (ket hop)
USE giay;
GO

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'ck_tt_so_tien')
BEGIN
    ALTER TABLE dbo.thanh_toan DROP CONSTRAINT ck_tt_so_tien;
END
GO

ALTER TABLE dbo.thanh_toan ADD CONSTRAINT ck_tt_so_tien CHECK (so_tien >= 0);
GO

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'ck_tt_hinh_thuc')
BEGIN
    ALTER TABLE dbo.thanh_toan DROP CONSTRAINT ck_tt_hinh_thuc;
END
GO

ALTER TABLE dbo.thanh_toan ADD CONSTRAINT ck_tt_hinh_thuc CHECK (hinh_thuc IN (1, 2, 3, 4, 5));
GO
