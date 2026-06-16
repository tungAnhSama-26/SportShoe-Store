$connString = "Server=localhost,1433;Database=giay;User Id=sa;Password=TungAnh@123456;Encrypt=True;TrustServerCertificate=True"
$conn = New-Object System.Data.SqlClient.SqlConnection($connString)
$conn.Open()
$cmd = $conn.CreateCommand()
$cmd.CommandText = "
DELETE FROM giay_thuoc_tinh WHERE giay_id IN (SELECT id FROM giay WHERE ten LIKE N'%Zoopegasus%');
DELETE FROM hinh_anh_giay WHERE giay_chi_tiet_id IN (SELECT id FROM giay_chi_tiet WHERE giay_id IN (SELECT id FROM giay WHERE ten LIKE N'%Zoopegasus%'));
DELETE FROM giay_chi_tiet WHERE giay_id IN (SELECT id FROM giay WHERE ten LIKE N'%Zoopegasus%');
DELETE FROM giay WHERE ten LIKE N'%Zoopegasus%';
"
$rows = $cmd.ExecuteNonQuery()
Write-Host "Deleted $rows rows."
$conn.Close()
