package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.time.Instant;

/**
 * Mã QR chuyển khoản của một hóa đơn chờ tại quầy.
 *
 * @param hoaDonId     hóa đơn chờ đang được thanh toán
 * @param maHoaDon     mã hóa đơn, cũng là phần webhook SePay dùng để đối chiếu
 * @param noiDungCk    nội dung chuyển khoản khách phải giữ nguyên khi quét mã
 * @param qrUrl        ảnh VietQR do SePay sinh từ cấu hình tài khoản của cửa hàng
 * @param soTien       số tiền chờ nhận, đã làm tròn về đồng
 * @param daThanhToan  true khi hóa đơn đã được chuyển sang trạng thái thanh toán xong
 * @param trangThai    trạng thái hiện tại của hóa đơn (11 = còn chờ, 6 = đã hủy)
 * @param hetHanLuc    thời điểm mã QR hết hiệu lực; null khi chỉ hỏi trạng thái
 */
public record QrChuyenKhoanResponse(
        Integer hoaDonId,
        String maHoaDon,
        String noiDungCk,
        String qrUrl,
        long soTien,
        boolean daThanhToan,
        Integer trangThai,
        Instant hetHanLuc
) {
}
