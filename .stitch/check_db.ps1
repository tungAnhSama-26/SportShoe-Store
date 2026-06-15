$connString = "Server=localhost;Database=giay;User Id=sa;Password=Thuy@123456;Encrypt=True;TrustServerCertificate=True"
$connection = New-Object System.Data.SqlClient.SqlConnection($connString)
try {
    $connection.Open()
    Write-Output "Successfully connected to SQL Server."
    
    # Check if giao_ca table exists
    $command = $connection.CreateCommand()
    $command.CommandText = "SELECT OBJECT_ID('giao_ca', 'U') AS TableID"
    $tableId = $command.ExecuteScalar()
    
    if ($tableId -ne $null -and $tableId -ne [DBNull]::Value) {
        Write-Output "Table 'giao_ca' already exists. No migration needed."
    } else {
        Write-Output "Table 'giao_ca' does not exist. Running migration script..."
        
        # Read the SQL script
        $sqlScriptPath = "d:\SportShoe-Store\database\08_cham_cong_giao_ca.sql"
        if (Test-Path $sqlScriptPath) {
            $sqlContent = Get-Content -Raw $sqlScriptPath
            
            # Remove USE statements and GO separators
            $sqlContent = $sqlContent -replace '(?im)^\s*USE\s+\w+;\s*$', ''
            $sqlContent = $sqlContent -replace '(?im)^\s*GO\s*$', ';'
            
            $cmd = $connection.CreateCommand()
            $cmd.CommandText = $sqlContent
            $cmd.ExecuteNonQuery()
            Write-Output "Migration script executed successfully."
        } else {
            Write-Error "Migration file 08_cham_cong_giao_ca.sql not found at $sqlScriptPath"
        }
    }
} catch {
    Write-Error "Database error: $_"
} finally {
    if ($connection.State -eq [System.Data.ConnectionState]::Open) {
        $connection.Close()
    }
}
