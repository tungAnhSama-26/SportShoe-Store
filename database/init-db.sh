#!/bin/bash
set -e

SA_PASSWORD="${MSSQL_SA_PASSWORD:-Yoasobi123.}"
DB_HOST="${DB_HOST:-database}"

# Xác định đường dẫn sqlcmd trong image MSSQL
if [ -f /opt/mssql-tools18/bin/sqlcmd ]; then
    SQLCMD="/opt/mssql-tools18/bin/sqlcmd -C"
elif [ -f /opt/mssql-tools/bin/sqlcmd ]; then
    SQLCMD="/opt/mssql-tools/bin/sqlcmd"
else
    SQLCMD="sqlcmd -C"
fi

echo "=================================================="
echo "Đang kiểm tra kết nối tới SQL Server tại $DB_HOST..."
echo "=================================================="

for i in {1..60}; do
    if $SQLCMD -S "$DB_HOST" -U sa -P "$SA_PASSWORD" -Q "SELECT 1" > /dev/null 2>&1; then
        echo "SQL Server đã sẵn sàng kết nối!"
        break
    fi
    echo "Đang chờ SQL Server khởi động... ($i/60)"
    sleep 2
done

# Kiểm tra xem database 'giay' đã tồn tại chưa
DB_EXISTS=$($SQLCMD -S "$DB_HOST" -U sa -P "$SA_PASSWORD" -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(*) FROM sys.databases WHERE name = 'giay'" 2>/dev/null | tr -d '[:space:]' || echo "0")

if [ "$DB_EXISTS" = "0" ] || [ -z "$DB_EXISTS" ]; then
    echo "Database 'giay' chưa tồn tại. Đang tiến hành tạo schema và nạp dữ liệu mẫu..."
    
    echo "1. Chạy 01_schema.sql (Tạo database và bảng)..."
    $SQLCMD -S "$DB_HOST" -U sa -P "$SA_PASSWORD" -i /database/01_schema.sql
    
    echo "2. Chạy 02_data.sql (Nạp dữ liệu mẫu)..."
    $SQLCMD -S "$DB_HOST" -U sa -P "$SA_PASSWORD" -d giay -i /database/02_data.sql
    
    if [ -f /database/03_fix_hoa_don_trang_thai.sql ]; then
        echo "3. Chạy 03_fix_hoa_don_trang_thai.sql..."
        $SQLCMD -S "$DB_HOST" -U sa -P "$SA_PASSWORD" -d giay -i /database/03_fix_hoa_don_trang_thai.sql
    fi

    if [ -f /database/04_migrate_nhan_vien_password_status.sql ]; then
        echo "4. Chạy 04_migrate_nhan_vien_password_status.sql..."
        $SQLCMD -S "$DB_HOST" -U sa -P "$SA_PASSWORD" -d giay -i /database/04_migrate_nhan_vien_password_status.sql
    fi

    if [ -f /database/05_migrate_hoa_don_event_history.sql ]; then
        echo "5. Chạy 05_migrate_hoa_don_event_history.sql..."
        $SQLCMD -S "$DB_HOST" -U sa -P "$SA_PASSWORD" -d giay -i /database/05_migrate_hoa_don_event_history.sql
    fi

    if [ -f /database/06_migrate_lich_lam_viec_ca_lam.sql ]; then
        echo "6. Chạy 06_migrate_lich_lam_viec_ca_lam.sql..."
        $SQLCMD -S "$DB_HOST" -U sa -P "$SA_PASSWORD" -d giay -i /database/06_migrate_lich_lam_viec_ca_lam.sql
    fi

    if [ -f /database/bangiaoca.sql ]; then
        echo "7. Chạy bangiaoca.sql..."
        $SQLCMD -S "$DB_HOST" -U sa -P "$SA_PASSWORD" -d giay -i /database/bangiaoca.sql
    fi

    echo "=================================================="
    echo "Khởi tạo Database 'giay' hoàn tất thành công!"
    echo "=================================================="
else
    echo "Database 'giay' đã tồn tại sẵn, bỏ qua bước khởi tạo dữ liệu."
fi
