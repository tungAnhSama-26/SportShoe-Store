package com.example.server.core.admin.nhanVien.event;

/**
 * Dữ liệu tạm để gửi thông tin đăng nhập sau khi transaction tạo nhân viên commit.
 * Mật khẩu chỉ tồn tại trong bộ nhớ và tuyệt đối không được ghi log.
 */
public record NhanVienAccountCreatedEvent(
        String email,
        String hoTen,
        String tenDangNhap,
        String matKhauTamThoi
) {
    @Override
    public String toString() {
        return "NhanVienAccountCreatedEvent{credentials=REDACTED}";
    }
}
