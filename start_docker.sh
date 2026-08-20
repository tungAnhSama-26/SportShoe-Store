#!/usr/bin/env bash
# ==============================================================================
# SCRIPT QUẢN LÝ DOCKER CHO DỰ ÁN SPORTSHOE STORE
# ==============================================================================

set -e

ACTION="${1:-start}"

show_help() {
    echo "Sử dụng: ./start_docker.sh [lệnh]"
    echo ""
    echo "Các lệnh hỗ trợ:"
    echo "  start     (hoặc không truyền gì): Khởi động toàn bộ hệ thống (DB, Backend, Frontend)"
    echo "  build     : Build lại toàn bộ images và khởi động"
    echo "  stop      : Dừng toàn bộ hệ thống"
    echo "  restart   : Khởi động lại toàn bộ hệ thống"
    echo "  logs      : Xem log thời gian thực của tất cả các dịch vụ"
    echo "  logs-be   : Xem log của Backend"
    echo "  logs-fe   : Xem log của Frontend"
    echo "  logs-db   : Xem log của Database"
    echo "  reset-db  : Xóa toàn bộ dữ liệu DB hiện tại và khởi tạo lại từ đầu"
    echo "  status    : Kiểm tra trạng thái các container"
    echo "  help      : Hiển thị trợ giúp này"
}

check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo "❌ Lỗi: Không thể kết nối tới Docker Daemon!"
        echo "👉 Bạn hãy mở ứng dụng Docker Desktop trên máy trước rồi chạy lại lệnh này nhé."
        exit 1
    fi
}

case "$ACTION" in
    start)
        check_docker
        echo "🚀 Đang khởi động hệ thống SportShoe Store (tự động build nếu chưa có image)..."
        docker compose up -d --build
        echo ""
        echo "=========================================================="
        echo "🎉 Hệ thống đang chạy!"
        echo "🌐 Web Frontend : http://localhost"
        echo "🔌 API Backend  : http://localhost:8080"
        echo "🗄️  SQL Server   : localhost:1433 (user: sa)"
        echo "📝 Xem logs     : ./start_docker.sh logs"
        echo "🛑 Dừng hệ thống: ./start_docker.sh stop"
        echo "=========================================================="
        ;;
    build)
        check_docker
        echo "🔨 Đang build lại toàn bộ images và khởi động hệ thống..."
        docker compose up -d --build --force-recreate
        echo "✅ Hoàn tất build và khởi động!"
        ;;
    stop)
        echo "🛑 Đang dừng toàn bộ dịch vụ..."
        docker compose down
        echo "✅ Đã dừng toàn bộ dịch vụ."
        ;;
    restart)
        echo "🔄 Đang khởi động lại hệ thống..."
        docker compose restart
        echo "✅ Đã khởi động lại."
        ;;
    logs)
        docker compose logs -f
        ;;
    logs-be)
        docker compose logs -f backend
        ;;
    logs-fe)
        docker compose logs -f frontend
        ;;
    logs-db)
        docker compose logs -f database
        ;;
    status)
        docker compose ps
        ;;
    reset-db)
        read -p "⚠️ Bạn có chắc muốn XÓA TOÀN BỘ dữ liệu DB và nạp lại từ đầu? (y/N): " confirm
        if [[ "$confirm" =~ ^[Yy]$ ]]; then
            echo "🗑️  Đang xóa volume dữ liệu database..."
            docker compose down -v
            echo "🚀 Đang khởi động lại và tự động nạp dữ liệu..."
            docker compose up -d
            echo "✅ Đã reset DB thành công!"
        else
            echo "❌ Đã hủy thao tác."
        fi
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "❌ Lệnh không hợp lệ: $ACTION"
        echo ""
        show_help
        exit 1
        ;;
esac
