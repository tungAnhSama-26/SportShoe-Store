-- ============================================================================
-- FIX: Dong bo lai bo dem IDENTITY cua TAT CA cac bang ve dung MAX(id).
--
-- Trieu chung: tao moi ban ghi (phieu tra hang, hoa don, san pham, voucher...)
-- bi loi "Violation of PRIMARY KEY constraint ... Cannot insert duplicate key".
--
-- Nguyen nhan: du lieu seed/chen tay co id cu the nhung bo dem IDENTITY
-- khong duoc cap nhat theo -> DB sinh id da ton tai.
--
-- Cach dung: chay script nay 1 lan tren database "giay" (chay lai bao nhieu
-- lan cung an toan - chi reseed bang nao dang lech).
-- ============================================================================
SET NOCOUNT ON;

DECLARE @t SYSNAME, @c SYSNAME, @m BIGINT, @sql NVARCHAR(MAX), @full NVARCHAR(160);

DECLARE cur CURSOR FOR
    SELECT t.name, c.name
    FROM sys.identity_columns c
    JOIN sys.tables t ON c.object_id = t.object_id;

OPEN cur;
FETCH NEXT FROM cur INTO @t, @c;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @sql = N'SELECT @m = ISNULL(MAX(' + QUOTENAME(@c) + N'),0) FROM dbo.' + QUOTENAME(@t);
    EXEC sp_executesql @sql, N'@m BIGINT OUTPUT', @m = @m OUTPUT;

    SET @full = N'dbo.' + @t;
    IF @m > 0 AND CAST(IDENT_CURRENT(@full) AS BIGINT) < @m
    BEGIN
        PRINT N'RESEED ' + @t + N' -> ' + CAST(@m AS NVARCHAR(20));
        DBCC CHECKIDENT(@full, RESEED, @m);
    END

    FETCH NEXT FROM cur INTO @t, @c;
END
CLOSE cur;
DEALLOCATE cur;

PRINT N'Hoan tat dong bo IDENTITY.';
