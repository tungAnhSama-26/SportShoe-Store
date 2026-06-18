USE giay;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE Name = N'face_descriptor' AND Object_ID = Object_ID(N'nhan_vien'))
BEGIN
    ALTER TABLE nhan_vien ADD face_descriptor NVARCHAR(MAX);
END
GO
