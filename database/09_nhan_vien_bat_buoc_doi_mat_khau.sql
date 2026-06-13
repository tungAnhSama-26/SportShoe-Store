IF COL_LENGTH('dbo.nhan_vien', 'bat_buoc_doi_mat_khau') IS NULL
BEGIN
    ALTER TABLE dbo.nhan_vien
        ADD bat_buoc_doi_mat_khau BIT NOT NULL
            CONSTRAINT df_nhan_vien_bat_buoc_doi_mat_khau DEFAULT 0;
END;

IF COL_LENGTH('dbo.nhan_vien', 'han_doi_mat_khau') IS NULL
BEGIN
    ALTER TABLE dbo.nhan_vien
        ADD han_doi_mat_khau DATETIME2 NULL;
END;

EXEC sp_executesql N'
    UPDATE dbo.nhan_vien
    SET bat_buoc_doi_mat_khau = 0,
        han_doi_mat_khau = NULL
    WHERE han_doi_mat_khau IS NULL
       OR vai_tro = 1
       OR ma = ''NV001'';
';
