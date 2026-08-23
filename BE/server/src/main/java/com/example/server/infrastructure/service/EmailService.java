package com.example.server.infrastructure.service;

import com.example.server.infrastructure.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.core.io.FileSystemResource;

@Service
public class EmailService {

    public record EmailDispatchResult(boolean sent, String warningMessage) {
        public static EmailDispatchResult success() {
            return new EmailDispatchResult(true, null);
        }

        public static EmailDispatchResult failure(String warningMessage) {
            return new EmailDispatchResult(false, warningMessage);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private static final String SYSTEM_LOGIN_URL = "http://localhost:5173/login";
    private static final String CUSTOMER_LOGIN_URL = "http://localhost:3000/login";
    private static final String CUSTOMER_STORE_URL = "http://localhost:3000";
    /** Base URL của backend để tải ảnh sản phẩm (/uploads/...) trong email. */
    private static final String BACKEND_BASE_URL = "http://localhost:8080";
    /** Logo shop nhúng trực tiếp vào email (CID) - luôn hiển thị, không bị mail chặn. */
    private static final String LOGO_RESOURCE = "static/logo-shop.png";
    private static final String LOGO_CID = "shopLogo";
    private static final DateTimeFormatter VOUCHER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter ORDER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username:}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendRegistrationEmail(String to, String fullName, String username, String password) {
        EmailDispatchResult dispatchResult = trySendRegistrationEmail(to, fullName, username, password);
        if (!dispatchResult.sent()) {
            throw new BusinessException(dispatchResult.warningMessage());
        }
    }

    public EmailDispatchResult trySendRegistrationEmail(String to, String fullName, String username, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(to);
            helper.setSubject("SportShoe Internal - Tài khoản hệ thống của bạn");
            helper.setText(buildRegistrationEmailHtml(fullName, username, password), true);
            mailSender.send(message);
            return EmailDispatchResult.success();
        } catch (MessagingException | MailException exception) {
            return emailFailure(
                    "Không thể gửi email tài khoản nhân viên lúc này. Vui lòng thử lại.",
                    to,
                    exception
            );
        }
    }

    public void sendCustomerRegistrationEmail(String to, String fullName, String username, String password) {
        EmailDispatchResult dispatchResult = trySendCustomerRegistrationEmail(to, fullName, username, password);
        if (!dispatchResult.sent()) {
            throw new BusinessException(dispatchResult.warningMessage());
        }
    }

    /**
     * Gửi email chào mừng khách hàng ở luồng nền (không chặn request tạo khách hàng).
     * Lỗi gửi email chỉ được ghi log, không làm hỏng thao tác tạo khách hàng.
     */
    @Async
    public void sendCustomerRegistrationEmailAsync(String to, String fullName, String username, String password) {
        trySendCustomerRegistrationEmail(to, fullName, username, password);
    }

    /**
     * Gửi email thông báo mật khẩu mới cho khách hàng ở luồng nền (không chặn request đổi mật khẩu).
     * Lỗi gửi email chỉ được ghi log, không làm hỏng thao tác đổi mật khẩu.
     */
    @Async
    public void sendCustomerPasswordChangedEmailAsync(String to, String fullName, String username, String newPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(to);
            helper.setSubject("SportShoe - Mật khẩu tài khoản của bạn đã được thay đổi");
            helper.setText(buildCustomerPasswordChangedEmailHtml(fullName, username, newPassword), true);
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            log.error("Không thể gửi email đổi mật khẩu khách hàng tới {}", to, exception);
        }
    }

    /**
     * Gửi email tặng phiếu giảm giá cá nhân cho khách hàng ở luồng nền.
     * Lỗi gửi email chỉ được ghi log, không làm hỏng thao tác tặng phiếu.
     *
     * @param loai 1 = giảm theo phần trăm, 2 = giảm theo số tiền
     */
    @Async
    public void sendVoucherGiftEmailAsync(
            String to,
            String fullName,
            String maPhieu,
            String tenPhieu,
            Integer loai,
            BigDecimal giaTri,
            BigDecimal giamToiDa,
            BigDecimal giaTriToiThieu,
            Instant ngayBatDau,
            Instant ngayKetThuc
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(to);
            helper.setSubject("\uD83C\uDF81 Quà tặng từ SportShoe - Phiếu giảm giá dành riêng cho bạn");
            helper.setText(
                    buildVoucherGiftEmailHtml(fullName, maPhieu, tenPhieu, loai, giaTri, giamToiDa, giaTriToiThieu, ngayBatDau, ngayKetThuc),
                    true
            );
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            log.error("Không thể gửi email tặng phiếu giảm giá tới {}", to, exception);
        }
    }

    /**
     * Gửi email thông báo phiếu giảm giá đã được cập nhật cho khách hàng ở luồng nền.
     */
    @Async
    public void sendVoucherUpdatedEmailAsync(
            String to,
            String fullName,
            String maPhieu,
            String tenPhieu,
            Integer loai,
            BigDecimal giaTri,
            BigDecimal giamToiDa,
            BigDecimal giaTriToiThieu,
            Instant ngayBatDau,
            Instant ngayKetThuc
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(to);
            helper.setSubject("\uD83D\uDD14 SportShoe - Thông báo cập nhật phiếu giảm giá của bạn");
            helper.setText(
                    buildVoucherUpdatedEmailHtml(fullName, maPhieu, tenPhieu, loai, giaTri, giamToiDa, giaTriToiThieu, ngayBatDau, ngayKetThuc),
                    true
            );
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            log.error("Không thể gửi email cập nhật phiếu giảm giá tới {}", to, exception);
        }
    }

    /**
     * Gửi email thông báo phiếu giảm giá đã hết hạn mà khách hàng chưa sử dụng.
     */
    @Async
    public void sendVoucherExpiredEmailAsync(
            String to,
            String fullName,
            String maPhieu,
            String tenPhieu,
            Instant ngayKetThuc
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(to);
            helper.setSubject("\u23F0 SportShoe - Phiếu giảm giá của bạn đã hết hạn");
            helper.setText(
                    buildVoucherExpiredEmailHtml(fullName, maPhieu, tenPhieu, ngayKetThuc),
                    true
            );
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            log.error("Không thể gửi email hết hạn phiếu giảm giá tới {}", to, exception);
        }
    }

    /**
     * Gửi email thông báo phiếu giảm giá ngừng hoạt động.
     */
    @Async
    public void sendVoucherDeactivatedEmailAsync(
            String to,
            String fullName,
            String maPhieu,
            String tenPhieu
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(to);
            helper.setSubject("⚠️ SportShoe - Phiếu giảm giá của bạn đã ngừng hoạt động");
            helper.setText(
                    buildVoucherDeactivatedEmailHtml(fullName, maPhieu, tenPhieu),
                    true
            );
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            log.error("Không thể gửi email ngừng hoạt động phiếu giảm giá tới {}", to, exception);
        }
    }

    public EmailDispatchResult trySendCustomerRegistrationEmail(String to, String fullName, String username, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(to);
            helper.setSubject("Chào mừng đến với SportShoe - Tài khoản của bạn");
            helper.setText(buildCustomerRegistrationEmailHtml(fullName, username, password), true);
            mailSender.send(message);
            return EmailDispatchResult.success();
        } catch (MessagingException | MailException exception) {
            return emailFailure(
                    "Không thể gửi email tài khoản khách hàng lúc này. Vui lòng thử lại.",
                    to,
                    exception
            );
        }
    }

    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            applyFrom(message);
            message.setTo(to);
            message.setSubject("Mã xác nhận quên mật khẩu - SportShoe");
            message.setText(String.format(
                    "Chào bạn,%n%n" +
                            "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản SportShoe.%n" +
                            "Mã xác nhận của bạn là: %s%n%n" +
                            "Mã này sẽ hết hạn sau ít phút. Vui lòng không chia sẻ mã này với bất kỳ ai.%n%n" +
                            "Trân trọng,%n" +
                            "Đội ngũ SportShoe",
                    otp
            ));
            mailSender.send(message);
        } catch (MailException exception) {
            EmailDispatchResult dispatchResult = emailFailure(
                    "Không thể gửi mã xác nhận qua email lúc này. Vui lòng thử lại.",
                    to,
                    exception
            );
            throw new BusinessException(dispatchResult.warningMessage());
        }
    }

    // ===== Email xác nhận đơn hàng (gửi khi khách đặt hàng thành công) =====

    public record DongDonHangEmail(
            String tenSanPham,
            String bienThe,
            String hinhAnh,
            int soLuong,
            BigDecimal donGia,
            BigDecimal thanhTien
    ) {}

    public record DonHangEmail(
            String to,
            String hoTen,
            String email,
            String maHoaDon,
            Instant ngayLap,
            String tenNguoiNhan,
            String sdtNguoiNhan,
            String diaChiGiaoHang,
            String hinhThucThanhToan,
            BigDecimal phiVanChuyen,
            BigDecimal tienGiam,
            BigDecimal tongTienHang,
            BigDecimal tongThanhToan,
            List<DongDonHangEmail> items
    ) {}

    private record InlineProductImage(String cid, File file, String contentType) {}

    private File resolveLocalImageFile(String path) {
        if (path == null || path.isBlank() || path.matches("(?i)^(https?:|data:).*")) {
            return null;
        }
        String cleanPath = path.trim();
        if (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }
        File f1 = new File(cleanPath);
        if (f1.exists() && f1.isFile()) {
            return f1;
        }
        if (!cleanPath.startsWith("uploads/")) {
            File f2 = new File("uploads/" + cleanPath);
            if (f2.exists() && f2.isFile()) {
                return f2;
            }
        }
        return null;
    }

    private String getImageContentType(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }

    /** Gửi email xác nhận đơn hàng ở luồng nền; lỗi chỉ ghi log, không chặn việc đặt hàng. */
    @Async
    public void sendOrderStatusUpdatedEmailAsync(DonHangEmail data, String tenTrangThaiMoi) {
        if (data == null || data.to() == null || data.to().isBlank()) {
            return;
        }
        try {
            log.info("Bắt đầu gửi email cập nhật trạng thái đơn #{} tới {}", data.maHoaDon(), data.to());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(data.to());
            helper.setSubject("SportShoe Store - Cập nhật trạng thái đơn hàng #" + safeText(data.maHoaDon()));
            List<InlineProductImage> inlineImages = new ArrayList<>();
            helper.setText(buildOrderStatusUpdatedEmailHtml(data, tenTrangThaiMoi, inlineImages), true);
            ClassPathResource logo = new ClassPathResource(LOGO_RESOURCE);
            if (logo.exists()) {
                helper.addInline(LOGO_CID, logo);
            }
            for (InlineProductImage img : inlineImages) {
                helper.addInline(img.cid(), new FileSystemResource(img.file()), img.contentType());
            }
            mailSender.send(message);
            log.info("Đã gửi thành công email cập nhật trạng thái đơn #{} tới {}", data.maHoaDon(), data.to());
        } catch (MessagingException | MailException exception) {
            log.error("Không thể gửi email cập nhật trạng thái đơn hàng tới {}", data.to(), exception);
        }
    }

    private String buildOrderStatusUpdatedEmailHtml(DonHangEmail d, String tenTrangThaiMoi, List<InlineProductImage> inlineImages) {
        boolean chuyenKhoan = "VNPAY".equalsIgnoreCase(d.hinhThucThanhToan())
                || "CHUYEN_KHOAN".equalsIgnoreCase(d.hinhThucThanhToan());
        String thanhToanText = chuyenKhoan
                ? "Chuyển khoản ngân hàng (đã thanh toán)"
                : "Thanh toán khi nhận hàng (COD)";
        BigDecimal phi = d.phiVanChuyen() == null ? BigDecimal.ZERO : d.phiVanChuyen();
        String vanChuyenText = phi.signum() <= 0
                ? "Miễn phí (1-3 ngày)"
                : ("GHN - " + formatCurrency(phi) + " (1-3 ngày)");
        String ngayText = d.ngayLap() != null ? ORDER_DATE_FORMAT.format(d.ngayLap()) : "";

        StringBuilder itemsHtml = new StringBuilder();
        if (d.items() != null) {
            int itemIdx = 0;
            for (DongDonHangEmail it : d.items()) {
                String anhCell = "<div style=\"width:56px;height:56px;border-radius:8px;background:#f3f4f6;\"></div>";
                String rawAnh = it.hinhAnh();
                if (rawAnh != null && !rawAnh.isBlank()) {
                    String trimmed = rawAnh.trim();
                    File localFile = resolveLocalImageFile(trimmed);
                    if (localFile != null && localFile.exists() && localFile.isFile()) {
                        String cid = "itemImgStatus_" + (itemIdx++);
                        inlineImages.add(new InlineProductImage(cid, localFile, getImageContentType(localFile)));
                        anhCell = "<img src=\"cid:" + cid + "\" alt=\"\" width=\"56\" style=\"width:56px;height:56px;object-fit:cover;border-radius:8px;background:#f3f4f6;\" />";
                    } else if (trimmed.matches("(?i)^(https?:|data:).*")) {
                        anhCell = "<img src=\"" + escapeHtml(trimmed) + "\" alt=\"\" width=\"56\" style=\"width:56px;height:56px;object-fit:cover;border-radius:8px;background:#f3f4f6;\" />";
                    } else {
                        String resolvedUrl = resolveAnhSanPham(trimmed);
                        if (!resolvedUrl.isBlank()) {
                            anhCell = "<img src=\"" + escapeHtml(resolvedUrl) + "\" alt=\"\" width=\"56\" style=\"width:56px;height:56px;object-fit:cover;border-radius:8px;background:#f3f4f6;\" />";
                        }
                    }
                }
                itemsHtml.append("""
                        <tr>
                          <td style="width:64px;padding:14px 12px 14px 0;border-bottom:1px solid #eee;vertical-align:top;">__ANH__</td>
                          <td style="padding:14px 0;border-bottom:1px solid #eee;font-size:14px;color:#374151;vertical-align:top;">
                            <div style="font-weight:700;color:#111827;">__TEN__</div>
                            <div style="color:#6b7280;font-size:13px;margin-top:2px;">__BIEN_THE__</div>
                            <div style="color:#6b7280;font-size:13px;margin-top:6px;">__DON_GIA__ &nbsp;&times; __SL__</div>
                          </td>
                          <td align="right" style="padding:14px 0;border-bottom:1px solid #eee;font-size:14px;font-weight:700;color:#111827;white-space:nowrap;vertical-align:top;">__THANH_TIEN__</td>
                        </tr>
                        """
                        .replace("__ANH__", anhCell)
                        .replace("__TEN__", escapeHtml(it.tenSanPham()))
                        .replace("__BIEN_THE__", escapeHtml(it.bienThe()))
                        .replace("__DON_GIA__", formatCurrency(it.donGia()))
                        .replace("__SL__", Integer.toString(it.soLuong()))
                        .replace("__THANH_TIEN__", formatCurrency(it.thanhTien())));
            }
        }

        StringBuilder summaryHtml = new StringBuilder();
        summaryHtml.append(orderSummaryRow("Tạm tính", formatCurrency(d.tongTienHang()), false));
        if (d.tienGiam() != null && d.tienGiam().signum() > 0) {
            summaryHtml.append(orderSummaryRow("Giảm giá", "-" + formatCurrency(d.tienGiam()), false));
        }
        summaryHtml.append(orderSummaryRow("Phí vận chuyển",
                phi.signum() <= 0 ? "Miễn phí" : formatCurrency(phi), false));
        summaryHtml.append(orderSummaryRow("Thành tiền", formatCurrency(d.tongThanhToan()), true));

        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Cập nhật trạng thái đơn hàng</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f4f7;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
                  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background:#f4f4f7;padding:32px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="max-width:640px;background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 12px 30px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="padding:30px 40px 6px 40px;text-align:center;">
                              <img src="cid:shopLogo" alt="SportShoe Store" width="140" style="max-width:160px;height:auto;display:inline-block;margin-bottom:8px;" />
                              <div style="font-size:22px;font-weight:800;color:#cf1018;letter-spacing:0.02em;">SportShoe <span style="color:#111827;">Store</span></div>
                            </td>
                          </tr>
                          <tr><td style="padding:16px 40px 0 40px;font-size:16px;color:#111827;">Xin chào <b>__HO_TEN__</b></td></tr>
                          <tr><td style="padding:10px 40px 0 40px;font-size:15px;line-height:1.6;color:#374151;">Đơn hàng của quý khách vừa được cập nhật sang trạng thái: <b style="color:#cf1018;">__TRANG_THAI_MOI__</b>.</td></tr>
                          <tr><td style="padding:22px 40px 0 40px;"><div style="height:1px;background:#ececec;"></div></td></tr>
                          <tr>
                            <td style="padding:22px 40px 0 40px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0"><tr>
                                <td valign="top" style="width:50%;padding-right:14px;">
                                  <div style="font-size:15px;font-weight:800;color:#111827;">Thông tin mua hàng</div>
                                  <div style="margin-top:10px;font-size:14px;color:#374151;font-weight:700;">__HO_TEN__</div>
                                  <div style="margin-top:4px;font-size:13px;color:#6b7280;">__EMAIL__</div>
                                </td>
                                <td valign="top" style="width:50%;padding-left:14px;">
                                  <div style="font-size:15px;font-weight:800;color:#111827;">Địa chỉ nhận hàng</div>
                                  <div style="margin-top:10px;font-size:14px;color:#374151;font-weight:700;">__TEN_NHAN__</div>
                                  <div style="margin-top:4px;font-size:13px;color:#6b7280;line-height:1.5;">__DIA_CHI__</div>
                                  <div style="margin-top:4px;font-size:13px;color:#6b7280;">__SDT_NHAN__</div>
                                </td>
                              </tr></table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:22px 40px 0 40px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0"><tr>
                                <td valign="top" style="width:50%;padding-right:14px;">
                                  <div style="font-size:15px;font-weight:800;color:#111827;">Phương thức thanh toán</div>
                                  <div style="margin-top:10px;font-size:14px;color:#374151;">__THANH_TOAN__</div>
                                </td>
                                <td valign="top" style="width:50%;padding-left:14px;">
                                  <div style="font-size:15px;font-weight:800;color:#111827;">Phương thức vận chuyển</div>
                                  <div style="margin-top:10px;font-size:14px;color:#374151;">__VAN_CHUYEN__</div>
                                </td>
                              </tr></table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 40px 0 40px;">
                              <div style="font-size:15px;font-weight:800;color:#111827;">Thông tin đơn hàng</div>
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="margin-top:8px;"><tr>
                                <td style="font-size:13px;color:#6b7280;">Mã đơn hàng: <b style="color:#cf1018;">#__MA__</b></td>
                                <td align="right" style="font-size:13px;color:#6b7280;">Ngày đặt hàng: <b style="color:#111827;">__NGAY__</b></td>
                              </tr></table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:6px 40px 0 40px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">__ITEMS__</table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:10px 40px 0 40px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">__SUMMARY__</table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 40px 30px 40px;text-align:center;font-size:12px;color:#9ca3af;line-height:1.6;">
                              Email được gửi tự động từ SportShoe Store. Vui lòng không trả lời email này.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """;

        return html
                .replace("__HO_TEN__", escapeHtml(d.hoTen()))
                .replace("__TRANG_THAI_MOI__", escapeHtml(tenTrangThaiMoi))
                .replace("__EMAIL__", escapeHtml(d.email()))
                .replace("__TEN_NHAN__", escapeHtml(d.tenNguoiNhan()))
                .replace("__DIA_CHI__", escapeHtml(d.diaChiGiaoHang()))
                .replace("__SDT_NHAN__", escapeHtml(d.sdtNguoiNhan()))
                .replace("__THANH_TOAN__", escapeHtml(thanhToanText))
                .replace("__VAN_CHUYEN__", escapeHtml(vanChuyenText))
                .replace("__MA__", escapeHtml(d.maHoaDon()))
                .replace("__NGAY__", escapeHtml(ngayText))
                .replace("__ITEMS__", itemsHtml.toString())
                .replace("__SUMMARY__", summaryHtml.toString());
    }

    /** Gửi email xác nhận đơn hàng ở luồng nền; lỗi chỉ ghi log, không chặn việc đặt hàng. */
    @Async
    public void sendOrderConfirmationEmailAsync(DonHangEmail data) {
        if (data == null || data.to() == null || data.to().isBlank()) {
            return;
        }
        try {
            log.info("Bắt đầu gửi email xác nhận đơn hàng #{} tới {}", data.maHoaDon(), data.to());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(data.to());
            helper.setSubject("SportShoe Store - Xác nhận đơn hàng #" + safeText(data.maHoaDon()));
            List<InlineProductImage> inlineImages = new ArrayList<>();
            helper.setText(buildOrderConfirmationEmailHtml(data, inlineImages), true);
            ClassPathResource logo = new ClassPathResource(LOGO_RESOURCE);
            if (logo.exists()) {
                helper.addInline(LOGO_CID, logo);
            }
            for (InlineProductImage img : inlineImages) {
                helper.addInline(img.cid(), new FileSystemResource(img.file()), img.contentType());
            }
            mailSender.send(message);
            log.info("Đã gửi thành công email xác nhận đơn hàng #{} tới {}", data.maHoaDon(), data.to());
        } catch (MessagingException | MailException exception) {
            log.error("Không thể gửi email xác nhận đơn hàng tới {}", data.to(), exception);
        }
    }

    private String buildOrderConfirmationEmailHtml(DonHangEmail d, List<InlineProductImage> inlineImages) {
        boolean chuyenKhoan = "VNPAY".equalsIgnoreCase(d.hinhThucThanhToan())
                || "CHUYEN_KHOAN".equalsIgnoreCase(d.hinhThucThanhToan());
        String thanhToanText = chuyenKhoan
                ? "Chuyển khoản ngân hàng (đã thanh toán)"
                : "Thanh toán khi nhận hàng (COD)";
        String loiThanhToan = chuyenKhoan
                ? "Quý khách đã thanh toán thành công qua chuyển khoản. Đơn hàng đang được xử lý."
                : "Quý khách vui lòng thanh toán khi nhận hàng. Đơn hàng đang được xử lý.";
        BigDecimal phi = d.phiVanChuyen() == null ? BigDecimal.ZERO : d.phiVanChuyen();
        String vanChuyenText = phi.signum() <= 0
                ? "Miễn phí (1-3 ngày)"
                : ("GHN - " + formatCurrency(phi) + " (1-3 ngày)");
        String ngayText = d.ngayLap() != null ? ORDER_DATE_FORMAT.format(d.ngayLap()) : "";

        StringBuilder itemsHtml = new StringBuilder();
        if (d.items() != null) {
            int itemIdx = 0;
            for (DongDonHangEmail it : d.items()) {
                String anhCell = "<div style=\"width:56px;height:56px;border-radius:8px;background:#f3f4f6;\"></div>";
                String rawAnh = it.hinhAnh();
                if (rawAnh != null && !rawAnh.isBlank()) {
                    String trimmed = rawAnh.trim();
                    File localFile = resolveLocalImageFile(trimmed);
                    if (localFile != null && localFile.exists() && localFile.isFile()) {
                        String cid = "itemImgConfirm_" + (itemIdx++);
                        inlineImages.add(new InlineProductImage(cid, localFile, getImageContentType(localFile)));
                        anhCell = "<img src=\"cid:" + cid + "\" alt=\"\" width=\"56\" style=\"width:56px;height:56px;object-fit:cover;border-radius:8px;background:#f3f4f6;\" />";
                    } else if (trimmed.matches("(?i)^(https?:|data:).*")) {
                        anhCell = "<img src=\"" + escapeHtml(trimmed) + "\" alt=\"\" width=\"56\" style=\"width:56px;height:56px;object-fit:cover;border-radius:8px;background:#f3f4f6;\" />";
                    } else {
                        String resolvedUrl = resolveAnhSanPham(trimmed);
                        if (!resolvedUrl.isBlank()) {
                            anhCell = "<img src=\"" + escapeHtml(resolvedUrl) + "\" alt=\"\" width=\"56\" style=\"width:56px;height:56px;object-fit:cover;border-radius:8px;background:#f3f4f6;\" />";
                        }
                    }
                }
                itemsHtml.append("""
                        <tr>
                          <td style="width:64px;padding:14px 12px 14px 0;border-bottom:1px solid #eee;vertical-align:top;">__ANH__</td>
                          <td style="padding:14px 0;border-bottom:1px solid #eee;font-size:14px;color:#374151;vertical-align:top;">
                            <div style="font-weight:700;color:#111827;">__TEN__</div>
                            <div style="color:#6b7280;font-size:13px;margin-top:2px;">__BIEN_THE__</div>
                            <div style="color:#6b7280;font-size:13px;margin-top:6px;">__DON_GIA__ &nbsp;&times; __SL__</div>
                          </td>
                          <td align="right" style="padding:14px 0;border-bottom:1px solid #eee;font-size:14px;font-weight:700;color:#111827;white-space:nowrap;vertical-align:top;">__THANH_TIEN__</td>
                        </tr>
                        """
                        .replace("__ANH__", anhCell)
                        .replace("__TEN__", escapeHtml(it.tenSanPham()))
                        .replace("__BIEN_THE__", escapeHtml(it.bienThe()))
                        .replace("__DON_GIA__", formatCurrency(it.donGia()))
                        .replace("__SL__", Integer.toString(it.soLuong()))
                        .replace("__THANH_TIEN__", formatCurrency(it.thanhTien())));
            }
        }

        StringBuilder summaryHtml = new StringBuilder();
        summaryHtml.append(orderSummaryRow("Tạm tính", formatCurrency(d.tongTienHang()), false));
        if (d.tienGiam() != null && d.tienGiam().signum() > 0) {
            summaryHtml.append(orderSummaryRow("Giảm giá", "-" + formatCurrency(d.tienGiam()), false));
        }
        summaryHtml.append(orderSummaryRow("Phí vận chuyển",
                phi.signum() <= 0 ? "Miễn phí" : formatCurrency(phi), false));
        summaryHtml.append(orderSummaryRow("Thành tiền", formatCurrency(d.tongThanhToan()), true));

        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Xác nhận đơn hàng</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f4f7;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
                  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background:#f4f4f7;padding:32px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="max-width:640px;background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 12px 30px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="padding:30px 40px 6px 40px;text-align:center;">
                              <img src="cid:shopLogo" alt="SportShoe Store" width="140" style="max-width:160px;height:auto;display:inline-block;margin-bottom:8px;" />
                              <div style="font-size:22px;font-weight:800;color:#cf1018;letter-spacing:0.02em;">SportShoe <span style="color:#111827;">Store</span></div>
                            </td>
                          </tr>
                          <tr><td style="padding:16px 40px 0 40px;font-size:16px;color:#111827;">Xin chào <b>__HO_TEN__</b></td></tr>
                          <tr><td style="padding:10px 40px 0 40px;font-size:15px;line-height:1.6;color:#374151;">Cảm ơn quý khách đã đặt hàng tại <b>SportShoe Store</b>!</td></tr>
                          <tr><td style="padding:8px 40px 0 40px;font-size:14px;line-height:1.6;color:#6b7280;">Đơn hàng của quý khách đã được tạo thành công. __LOI_THANH_TOAN__</td></tr>
                          <tr><td style="padding:22px 40px 0 40px;"><div style="height:1px;background:#ececec;"></div></td></tr>
                          <tr>
                            <td style="padding:22px 40px 0 40px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0"><tr>
                                <td valign="top" style="width:50%;padding-right:14px;">
                                  <div style="font-size:15px;font-weight:800;color:#111827;">Thông tin mua hàng</div>
                                  <div style="margin-top:10px;font-size:14px;color:#374151;font-weight:700;">__HO_TEN__</div>
                                  <div style="margin-top:4px;font-size:13px;color:#6b7280;">__EMAIL__</div>
                                </td>
                                <td valign="top" style="width:50%;padding-left:14px;">
                                  <div style="font-size:15px;font-weight:800;color:#111827;">Địa chỉ nhận hàng</div>
                                  <div style="margin-top:10px;font-size:14px;color:#374151;font-weight:700;">__TEN_NHAN__</div>
                                  <div style="margin-top:4px;font-size:13px;color:#6b7280;line-height:1.5;">__DIA_CHI__</div>
                                  <div style="margin-top:4px;font-size:13px;color:#6b7280;">__SDT_NHAN__</div>
                                </td>
                              </tr></table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:22px 40px 0 40px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0"><tr>
                                <td valign="top" style="width:50%;padding-right:14px;">
                                  <div style="font-size:15px;font-weight:800;color:#111827;">Phương thức thanh toán</div>
                                  <div style="margin-top:10px;font-size:14px;color:#374151;">__THANH_TOAN__</div>
                                </td>
                                <td valign="top" style="width:50%;padding-left:14px;">
                                  <div style="font-size:15px;font-weight:800;color:#111827;">Phương thức vận chuyển</div>
                                  <div style="margin-top:10px;font-size:14px;color:#374151;">__VAN_CHUYEN__</div>
                                </td>
                              </tr></table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 40px 0 40px;">
                              <div style="font-size:15px;font-weight:800;color:#111827;">Thông tin đơn hàng</div>
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="margin-top:8px;"><tr>
                                <td style="font-size:13px;color:#6b7280;">Mã đơn hàng: <b style="color:#cf1018;">#__MA__</b></td>
                                <td align="right" style="font-size:13px;color:#6b7280;">Ngày đặt hàng: <b style="color:#111827;">__NGAY__</b></td>
                              </tr></table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:6px 40px 0 40px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">__ITEMS__</table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:10px 40px 0 40px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">__SUMMARY__</table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 40px 30px 40px;text-align:center;font-size:12px;color:#9ca3af;line-height:1.6;">
                              Email được gửi tự động từ SportShoe Store. Vui lòng không trả lời email này.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """;

        return html
                .replace("__HO_TEN__", escapeHtml(d.hoTen()))
                .replace("__LOI_THANH_TOAN__", escapeHtml(loiThanhToan))
                .replace("__EMAIL__", escapeHtml(d.email()))
                .replace("__TEN_NHAN__", escapeHtml(d.tenNguoiNhan()))
                .replace("__DIA_CHI__", escapeHtml(d.diaChiGiaoHang()))
                .replace("__SDT_NHAN__", escapeHtml(d.sdtNguoiNhan()))
                .replace("__THANH_TOAN__", escapeHtml(thanhToanText))
                .replace("__VAN_CHUYEN__", escapeHtml(vanChuyenText))
                .replace("__MA__", escapeHtml(d.maHoaDon()))
                .replace("__NGAY__", escapeHtml(ngayText))
                .replace("__ITEMS__", itemsHtml.toString())
                .replace("__SUMMARY__", summaryHtml.toString());
    }

    private String orderSummaryRow(String label, String value, boolean emphasize) {
        String labelStyle = emphasize
                ? "font-size:16px;font-weight:800;color:#111827;border-top:2px solid #ececec;padding-top:12px;"
                : "font-size:14px;color:#6b7280;";
        String valueStyle = emphasize
                ? "font-size:18px;font-weight:800;color:#cf1018;border-top:2px solid #ececec;padding-top:12px;"
                : "font-size:14px;font-weight:700;color:#111827;";
        return """
                <tr>
                  <td style="padding:6px 0;__LS__">__L__</td>
                  <td align="right" style="padding:6px 0;__VS__">__V__</td>
                </tr>
                """
                .replace("__LS__", labelStyle)
                .replace("__VS__", valueStyle)
                .replace("__L__", escapeHtml(label))
                .replace("__V__", escapeHtml(value));
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    /** Ghép URL đầy đủ cho ảnh sản phẩm (ảnh upload nằm ở backend /uploads/...). */
    private String resolveAnhSanPham(String anh) {
        if (anh == null || anh.isBlank()) {
            return "";
        }
        String a = anh.trim();
        if (a.matches("(?i)^(https?:|data:).*")) {
            return a;
        }
        return a.startsWith("/") ? BACKEND_BASE_URL + a : BACKEND_BASE_URL + "/" + a;
    }

    private void applyFrom(MimeMessageHelper helper) throws MessagingException {
        if (fromAddress != null && !fromAddress.isBlank()) {
            helper.setFrom(fromAddress.trim());
        }
    }

    private void applyFrom(SimpleMailMessage message) {
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress.trim());
        }
    }

    private String buildRegistrationEmailHtml(String fullName, String username, String password) {
        return buildAccountEmailHtml(
                "SportShoe Internal",
                "Internal",
                "Hệ thống quản lý nội bộ<br>chuyên nghiệp và hiệu quả",
                buildFeatureRow(
                        buildFeatureCell("&#128737;", "Bảo mật", "An toàn thông tin", true),
                        buildFeatureCell("&#9889;", "Hiệu quả", "Tối ưu quy trình", true),
                        buildFeatureCell("&#128101;", "Kết nối", "Làm việc cùng nhau", false)
                ),
                "Tài khoản truy cập hệ thống của bạn đã được tạo.<br>Vui lòng sử dụng thông tin dưới đây để đăng nhập.",
                "Username",
                "Mật khẩu tạm thời",
                "Đăng nhập hệ thống",
                SYSTEM_LOGIN_URL,
                "Bạn cần đổi mật khẩu ngay sau lần đăng nhập đầu tiên để đảm bảo bảo mật.",
                "IT Department<br>SportShoe",
                "it-support@sportshoe.vn",
                fullName,
                username,
                password
        );
    }

    private String buildCustomerRegistrationEmailHtml(String fullName, String username, String password) {
        return buildAccountEmailHtml(
                "SportShoe Store",
                "Store",
                "Trải nghiệm mua sắm<br>giày thể thao đỉnh cao",
                buildFeatureRow(
                        buildFeatureCell("&#128717;", "Chất lượng", "Sản phẩm chính hãng", true),
                        buildFeatureCell("&#128666;", "Giao hàng", "Nhanh chóng tiện lợi", true),
                        buildFeatureCell("&#127873;", "Ưu đãi", "Nhiều khuyến mãi hấp dẫn", false)
                ),
                "Tài khoản của bạn đã được đăng ký thành công trên SportShoe.<br>Vui lòng sử dụng thông tin dưới đây để đăng nhập.",
                "Tên đăng nhập",
                "Mật khẩu",
                "Đăng nhập mua sắm",
                CUSTOMER_LOGIN_URL,
                "Bạn nên đổi mật khẩu ngay sau lần đăng nhập đầu tiên để đảm bảo bảo mật.",
                "Đội ngũ SportShoe",
                "support@sportshoe.vn",
                fullName,
                username,
                password
        );
    }

    private String buildCustomerPasswordChangedEmailHtml(String fullName, String username, String newPassword) {
        return buildAccountEmailHtml(
                "SportShoe Store",
                "Store",
                "Trải nghiệm mua sắm<br>giày thể thao đỉnh cao",
                buildFeatureRow(
                        buildFeatureCell("&#128274;", "Bảo mật", "Tài khoản an toàn", true),
                        buildFeatureCell("&#128666;", "Giao hàng", "Nhanh chóng tiện lợi", true),
                        buildFeatureCell("&#127873;", "Ưu đãi", "Nhiều khuyến mãi hấp dẫn", false)
                ),
                "Mật khẩu tài khoản của bạn vừa được thay đổi.<br>Vui lòng sử dụng mật khẩu mới dưới đây để đăng nhập.",
                "Tên đăng nhập",
                "Mật khẩu mới",
                "Đăng nhập ngay",
                CUSTOMER_LOGIN_URL,
                "Vì lý do bảo mật, bạn nên đổi lại mật khẩu sau khi đăng nhập. Nếu bạn không yêu cầu thay đổi này, vui lòng liên hệ với chúng tôi ngay.",
                "Đội ngũ SportShoe",
                "support@sportshoe.vn",
                fullName,
                username,
                newPassword
        );
    }

    private String buildAccountEmailHtml(
            String documentTitle,
            String wordmarkSubLine,
            String wordmarkDescriptionHtml,
            String featureSectionHtml,
            String accountDescriptionHtml,
            String usernameLabel,
            String passwordLabel,
            String buttonLabel,
            String loginUrl,
            String warningText,
            String signatureHtml,
            String supportEmail,
            String fullName,
            String username,
            String password
    ) {
        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>__TITLE__</title>
                </head>
                <body style="margin:0;padding:0;background:#c91018;font-family:Arial,Helvetica,sans-serif;color:#111827;">
                  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background:linear-gradient(135deg,#870e14 0%,#cf1018 38%,#ef1e24 100%);">
                    <tr>
                      <td align="center" style="padding:24px;">
                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="max-width:1420px;">
                          <tr>
                            <td style="padding:0;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                                <tr>
                                  <td valign="top" style="width:52%;padding:48px 36px 24px 24px;">
                                    __BRAND_PANEL__
                                    <div style="padding-top:110px;">
                                      __FEATURES__
                                    </div>
                                  </td>
                                  <td valign="top" style="width:48%;padding:30px 0 0 0;">
                                    <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background:#ffffff;border-radius:28px;overflow:hidden;box-shadow:0 30px 60px rgba(93,11,14,0.18);">
                                      <tr>
                                        <td style="padding:42px 54px 0 54px;">
                                          <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                              <td style="width:72px;height:72px;border-radius:999px;background:#fff3f3;text-align:center;vertical-align:middle;">
                                                __HERO_ICON__
                                              </td>
                                              <td style="padding-left:24px;">
                                                <div style="font-size:28px;line-height:1.3;font-weight:700;color:#404040;">Xin chào,</div>
                                                <div style="font-size:34px;line-height:1.35;font-weight:800;color:#e02525;">__FULL_NAME__ <span style="font-size:30px;vertical-align:middle;">&#128075;</span></div>
                                              </td>
                                            </tr>
                                          </table>
                                          <div style="padding-top:34px;font-size:18px;line-height:1.65;color:#6b7280;">
                                            __ACCOUNT_DESCRIPTION__
                                          </div>
                                          <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="margin-top:28px;border:1px solid #ffd6d6;border-radius:16px;background:#fff7f7;">
                                            <tr>
                                              <td style="padding:18px 22px 14px 22px;">
                                                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                                                  __USERNAME_ROW__
                                                  <tr>
                                                    <td colspan="3" style="padding:16px 0 12px 0;">
                                                      <div style="height:1px;background:#ffdede;"></div>
                                                    </td>
                                                  </tr>
                                                  __PASSWORD_ROW__
                                                </table>
                                              </td>
                                            </tr>
                                          </table>
                                          <div style="padding:36px 0 32px 0;text-align:center;">
                                            __ACTION_BUTTON__
                                          </div>
                                          __NOTICE_BOX__
                                          <div style="margin-top:28px;height:1px;background:#ececec;"></div>
                                          <div style="padding:26px 0 28px 0;font-size:16px;line-height:1.65;color:#5f6368;">
                                            <div>Trân trọng,</div>
                                            <div style="padding-top:8px;font-size:19px;font-weight:800;color:#1f2937;">
                                              __SIGNATURE__
                                            </div>
                                          </div>
                                        </td>
                                      </tr>
                                      <tr>
                                        <td style="border-top:1px solid #f2e5e5;padding:18px 26px 20px 26px;background:#fafafa;">
                                          __FOOTER_META__
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """;

        return html
                .replace("__TITLE__", escapeHtml(documentTitle))
                .replace("__BRAND_PANEL__", buildBrandPanel(wordmarkSubLine, wordmarkDescriptionHtml))
                .replace("__FEATURES__", featureSectionHtml)
                .replace("__HERO_ICON__", buildCenteredIcon("&#128100;", "#ffffff", "#e02525", 34, 72))
                .replace("__FULL_NAME__", escapeHtml(fullName))
                .replace("__ACCOUNT_DESCRIPTION__", accountDescriptionHtml)
                .replace("__USERNAME_ROW__", buildCredentialRow("&#128100;", usernameLabel, username))
                .replace("__PASSWORD_ROW__", buildCredentialRow("&#128274;", passwordLabel, password))
                .replace("__ACTION_BUTTON__", buildActionButton(buttonLabel, loginUrl))
                .replace("__NOTICE_BOX__", buildNoticeBox(warningText))
                .replace("__SIGNATURE__", signatureHtml)
                .replace("__FOOTER_META__", buildFooterMeta(supportEmail));
    }

    private String buildBrandPanel(String subLine, String descriptionHtml) {
        return """
                <div style="display:inline-block;padding:10px 18px;border:1px solid rgba(255,255,255,0.22);border-radius:999px;background:rgba(255,255,255,0.08);font-size:14px;font-weight:700;letter-spacing:0.08em;text-transform:uppercase;color:#fff4f4;">
                  SportShoe
                </div>
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="padding-top:26px;">
                  <tr>
                    <td style="padding-right:28px;border-right:1px solid rgba(255,255,255,0.35);">
                      <div style="font-size:70px;line-height:0.95;font-weight:800;color:#ffffff;">SportShoe</div>
                      <div style="font-size:68px;line-height:1.02;font-weight:300;color:#ffe3e3;">__SUB_LINE__</div>
                    </td>
                    <td style="padding-left:28px;vertical-align:middle;">
                      <div style="font-size:24px;line-height:1.6;color:#fff2f2;">
                        __DESCRIPTION__
                      </div>
                    </td>
                  </tr>
                </table>
                """
                .replace("__SUB_LINE__", escapeHtml(subLine))
                .replace("__DESCRIPTION__", descriptionHtml);
    }

    private String buildFeatureRow(String firstCell, String secondCell, String thirdCell) {
        return """
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                  <tr>
                    __FIRST_CELL__
                    __SECOND_CELL__
                    __THIRD_CELL__
                  </tr>
                </table>
                """
                .replace("__FIRST_CELL__", firstCell)
                .replace("__SECOND_CELL__", secondCell)
                .replace("__THIRD_CELL__", thirdCell);
    }

    private String buildFeatureCell(String symbol, String title, String description, boolean withRightPadding) {
        String paddingStyle = withRightPadding ? "padding-right:22px;" : "";
        return """
                <td valign="top" style="width:33.33%;__PADDING__">
                  <div style="margin-bottom:14px;">__ICON__</div>
                  <div style="font-size:18px;font-weight:700;color:#ffffff;">__TITLE__</div>
                  <div style="padding-top:8px;font-size:15px;line-height:1.6;color:#ffe5e5;">__DESCRIPTION__</div>
                </td>
                """
                .replace("__PADDING__", paddingStyle)
                .replace("__ICON__", buildCenteredIcon(symbol, "transparent", "#ffffff", 28, 42))
                .replace("__TITLE__", escapeHtml(title))
                .replace("__DESCRIPTION__", escapeHtml(description));
    }

    private String buildCredentialRow(String symbol, String label, String value) {
        return """
                <tr>
                  <td style="width:54px;vertical-align:middle;">
                    __ICON_WRAPPER__
                  </td>
                  <td style="font-size:15px;font-weight:700;color:#e02525;text-transform:uppercase;vertical-align:middle;">__LABEL__</td>
                  <td align="right" style="font-size:22px;font-weight:800;color:#2d2d2d;font-family:'Courier New',Courier,monospace;vertical-align:middle;">__VALUE__</td>
                </tr>
                """
                .replace("__ICON_WRAPPER__", buildBubbleIcon(symbol, "#ffe9e9", "#e02525", 46, 22))
                .replace("__LABEL__", escapeHtml(label))
                .replace("__VALUE__", escapeHtml(value));
    }

    private String buildActionButton(String label, String url) {
        return """
                <a href="__URL__" style="display:inline-block;min-width:320px;padding:18px 28px;border-radius:12px;background:#d71921;color:#ffffff;text-decoration:none;font-size:20px;font-weight:700;box-shadow:0 8px 16px rgba(215,25,33,0.25);">
                  <span style="display:inline-block;vertical-align:middle;margin-right:8px;font-size:20px;line-height:1;">&#10140;</span>
                  <span style="vertical-align:middle;">__LABEL__</span>
                </a>
                """
                .replace("__URL__", escapeHtml(url))
                .replace("__LABEL__", escapeHtml(label));
    }

    private String buildNoticeBox(String text) {
        return """
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="border-radius:12px;background:#fff8ec;">
                  <tr>
                    <td style="padding:16px 20px;">
                      <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                        <tr>
                          <td style="width:44px;vertical-align:top;">
                            __ICON__
                          </td>
                          <td style="font-size:15px;line-height:1.6;color:#5f6368;padding-left:8px;">
                            __TEXT__
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
                """
                .replace("__ICON__", buildCenteredIcon("&#9888;", "transparent", "#f59e0b", 24, 32))
                .replace("__TEXT__", escapeHtml(text));
    }

    private String buildFooterMeta(String supportEmail) {
        return """
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                  <tr>
                    <td style="font-size:13px;color:#8a8f98;vertical-align:middle;">
                      <span style="display:inline-block;vertical-align:middle;margin-right:6px;font-size:15px;line-height:1;">&#128737;</span>
                      <span style="vertical-align:middle;">Bảo mật tuyệt đối</span>
                    </td>
                    <td align="center" style="font-size:13px;color:#8a8f98;vertical-align:middle;">
                      <span style="display:inline-block;vertical-align:middle;margin-right:6px;font-size:15px;line-height:1;">&#127911;</span>
                      <span style="vertical-align:middle;">Hỗ trợ 24/7</span>
                    </td>
                    <td align="right" style="font-size:13px;color:#8a8f98;vertical-align:middle;">
                      <span style="display:inline-block;vertical-align:middle;margin-right:6px;font-size:15px;line-height:1;">&#9993;</span>
                      <span style="vertical-align:middle;">__EMAIL__</span>
                    </td>
                  </tr>
                </table>
                """
                .replace("__EMAIL__", escapeHtml(supportEmail));
    }

    private String buildBubbleIcon(String symbol, String background, String color, int boxSize, int fontSize) {
        return """
                <div style="width:__BOX__px;height:__BOX__px;border-radius:999px;background:__BACKGROUND__;text-align:center;line-height:__BOX__px;">
                  <span style="display:inline-block;vertical-align:middle;color:__COLOR__;font-size:__FONT__px;line-height:1;">__SYMBOL__</span>
                </div>
                """
                .replace("__BOX__", Integer.toString(boxSize))
                .replace("__BACKGROUND__", background)
                .replace("__COLOR__", color)
                .replace("__FONT__", Integer.toString(fontSize))
                .replace("__SYMBOL__", symbol);
    }

    private String buildCenteredIcon(String symbol, String background, String color, int fontSize, int boxSize) {
        return """
                <span style="display:inline-block;width:__BOX__px;height:__BOX__px;line-height:__BOX__px;text-align:center;border-radius:999px;background:__BACKGROUND__;color:__COLOR__;font-size:__FONT__px;">
                  __SYMBOL__
                </span>
                """
                .replace("__BOX__", Integer.toString(boxSize))
                .replace("__BACKGROUND__", background)
                .replace("__COLOR__", color)
                .replace("__FONT__", Integer.toString(fontSize))
                .replace("__SYMBOL__", symbol);
    }

    private String buildVoucherGiftEmailHtml(
            String fullName,
            String maPhieu,
            String tenPhieu,
            Integer loai,
            BigDecimal giaTri,
            BigDecimal giamToiDa,
            BigDecimal giaTriToiThieu,
            Instant ngayBatDau,
            Instant ngayKetThuc
    ) {
        boolean phanTram = loai != null && loai == 1;
        String giaTriText = phanTram
                ? ("Giảm ngay " + formatPercent(giaTri) + "%")
                : ("Giảm ngay " + formatCurrency(giaTri));
        String toiDaText = (phanTram && giamToiDa != null && giamToiDa.signum() > 0)
                ? (" (tối đa " + formatCurrency(giamToiDa) + ")")
                : "";
        String donToiThieuText = (giaTriToiThieu != null && giaTriToiThieu.signum() > 0)
                ? ("cho đơn hàng từ " + formatCurrency(giaTriToiThieu))
                : "áp dụng cho mọi đơn hàng";
        String batDauText = ngayBatDau != null
                ? VOUCHER_DATE_FORMAT.format(ngayBatDau)
                : "Ngay bây giờ";
        String hanText = ngayKetThuc != null
                ? ("đến hết ngày " + VOUCHER_DATE_FORMAT.format(ngayKetThuc))
                : "Không giới hạn thời gian";

        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Quà tặng từ SportShoe</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f4f7;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
                  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background:#f4f4f7;padding:32px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="max-width:480px;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 12px 30px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="padding:32px 32px 4px 32px;text-align:center;">
                              <div style="font-size:25px;font-weight:800;color:#cf1018;">&#127873; Quà tặng từ SportShoe</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:18px 36px 0 36px;font-size:15px;line-height:1.6;color:#374151;">
                              Chào bạn <b>__FULL_NAME__</b>,
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:10px 36px 0 36px;font-size:15px;line-height:1.6;color:#6b7280;">
                              Chúng tôi xin gửi tặng bạn một phiếu giảm giá đặc biệt để tri ân sự ủng hộ của bạn dành cho SportShoe.
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 36px 8px 36px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="border-radius:18px;background:linear-gradient(135deg,#870e14 0%,#cf1018 45%,#ef1e24 100%);">
                                <tr>
                                  <td style="padding:26px 22px;text-align:center;">
                                    <div style="font-size:13px;font-weight:700;letter-spacing:0.12em;color:#ffd9d9;text-transform:uppercase;">Mã ưu đãi của bạn</div>
                                    <div style="margin:16px auto;padding:14px 10px;border:2px dashed rgba(255,255,255,0.65);border-radius:12px;max-width:300px;font-size:30px;font-weight:800;letter-spacing:0.08em;color:#ffffff;font-family:'Courier New',Courier,monospace;">__MA__</div>
                                    <div style="font-size:15px;font-weight:600;color:#ffeaea;">__TEN__</div>
                                    <div style="margin-top:14px;font-size:18px;font-weight:800;color:#ffffff;">__GIA_TRI____TOI_DA__</div>
                                    <div style="margin-top:6px;font-size:13px;color:#ffdede;">__DON_TOI_THIEU__</div>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:6px 36px 0 36px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="border-radius:10px;background:#fff8ec;border:1px solid #fde2b8;">
                                <tr>
                                  <td style="padding:12px 16px;font-size:13px;color:#92600a;line-height:1.5;">
                                    <b>&#128197; Hiệu lực từ:</b> __BAT_DAU__<br>
                                    <b>&#9200; Hạn sử dụng:</b> __HAN__
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:26px 36px 6px 36px;text-align:center;">
                              <a href="__URL__" style="display:inline-block;padding:14px 42px;border-radius:999px;background:#cf1018;color:#ffffff;text-decoration:none;font-size:16px;font-weight:700;box-shadow:0 8px 16px rgba(207,16,24,0.28);">Sử dụng ngay</a>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:18px 36px 30px 36px;text-align:center;font-size:12px;color:#9ca3af;line-height:1.6;">
                              Email này được gửi tự động từ hệ thống SportShoe.<br>Vui lòng không trả lời trực tiếp email này.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """;

        return html
                .replace("__FULL_NAME__", escapeHtml(fullName))
                .replace("__MA__", escapeHtml(maPhieu))
                .replace("__TEN__", escapeHtml(tenPhieu))
                .replace("__GIA_TRI__", escapeHtml(giaTriText))
                .replace("__TOI_DA__", escapeHtml(toiDaText))
                .replace("__DON_TOI_THIEU__", escapeHtml(donToiThieuText))
                .replace("__BAT_DAU__", escapeHtml(batDauText))
                .replace("__HAN__", escapeHtml(hanText))
                .replace("__URL__", escapeHtml(CUSTOMER_STORE_URL));
    }

    private String buildVoucherUpdatedEmailHtml(
            String fullName,
            String maPhieu,
            String tenPhieu,
            Integer loai,
            BigDecimal giaTri,
            BigDecimal giamToiDa,
            BigDecimal giaTriToiThieu,
            Instant ngayBatDau,
            Instant ngayKetThuc
    ) {
        boolean phanTram = loai != null && loai == 1;
        String giaTriText = phanTram
                ? (formatPercent(giaTri) + "%")
                : formatCurrency(giaTri);
        String toiDaText = (phanTram && giamToiDa != null && giamToiDa.signum() > 0)
                ? (" (tối đa " + formatCurrency(giamToiDa) + ")")
                : "";
        String donToiThieuText = (giaTriToiThieu != null && giaTriToiThieu.signum() > 0)
                ? formatCurrency(giaTriToiThieu)
                : "Không giới hạn";
        String batDauText = ngayBatDau != null
                ? VOUCHER_DATE_FORMAT.format(ngayBatDau)
                : "Ngay bây giờ";
        String ketThucText = ngayKetThuc != null
                ? VOUCHER_DATE_FORMAT.format(ngayKetThuc)
                : "Không giới hạn";

        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Cập nhật phiếu giảm giá</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f4f7;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
                  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background:#f4f4f7;padding:32px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="max-width:480px;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 12px 30px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="padding:32px 32px 4px 32px;text-align:center;">
                              <div style="font-size:25px;font-weight:800;color:#cf1018;">&#128276; Cập nhật phiếu giảm giá</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:18px 36px 0 36px;font-size:15px;line-height:1.6;color:#374151;">
                              Chào bạn <b>__FULL_NAME__</b>,
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:10px 36px 0 36px;font-size:15px;line-height:1.6;color:#6b7280;">
                              Phiếu giảm giá <b>__MA__</b> của bạn vừa được cập nhật thông tin. Vui lòng kiểm tra các thông tin mới dưới đây.
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 36px 8px 36px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="border-radius:18px;background:linear-gradient(135deg,#870e14 0%,#cf1018 45%,#ef1e24 100%);">
                                <tr>
                                  <td style="padding:26px 22px;text-align:center;">
                                    <div style="font-size:13px;font-weight:700;letter-spacing:0.12em;color:#ffd9d9;text-transform:uppercase;">Mã phiếu</div>
                                    <div style="margin:12px auto;padding:14px 10px;border:2px dashed rgba(255,255,255,0.65);border-radius:12px;max-width:300px;font-size:28px;font-weight:800;letter-spacing:0.08em;color:#ffffff;font-family:'Courier New',Courier,monospace;">__MA__</div>
                                    <div style="font-size:15px;font-weight:600;color:#ffeaea;">__TEN__</div>
                                    <div style="margin-top:14px;font-size:18px;font-weight:800;color:#ffffff;">Giảm __GIA_TRI____TOI_DA__</div>
                                    <div style="margin-top:6px;font-size:13px;color:#ffdede;">Đơn tối thiểu: __DON_TOI_THIEU__</div>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:6px 36px 0 36px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="border-radius:10px;background:#fff8ec;border:1px solid #fde2b8;">
                                <tr>
                                  <td style="padding:12px 16px;font-size:13px;color:#92600a;line-height:1.8;">
                                    <b>&#128197; Ngày bắt đầu:</b> __BAT_DAU__<br>
                                    <b>&#9200; Ngày kết thúc:</b> __KET_THUC__
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:26px 36px 6px 36px;text-align:center;">
                              <a href="__URL__" style="display:inline-block;padding:14px 42px;border-radius:999px;background:#cf1018;color:#ffffff;text-decoration:none;font-size:16px;font-weight:700;box-shadow:0 8px 16px rgba(207,16,24,0.28);">Sử dụng ngay</a>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:18px 36px 30px 36px;text-align:center;font-size:12px;color:#9ca3af;line-height:1.6;">
                              Email này được gửi tự động từ hệ thống SportShoe.<br>Vui lòng không trả lời trực tiếp email này.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """;

        return html
                .replace("__FULL_NAME__", escapeHtml(fullName))
                .replace("__MA__", escapeHtml(maPhieu))
                .replace("__TEN__", escapeHtml(tenPhieu))
                .replace("__GIA_TRI__", escapeHtml(giaTriText))
                .replace("__TOI_DA__", escapeHtml(toiDaText))
                .replace("__DON_TOI_THIEU__", escapeHtml(donToiThieuText))
                .replace("__BAT_DAU__", escapeHtml(batDauText))
                .replace("__KET_THUC__", escapeHtml(ketThucText))
                .replace("__URL__", escapeHtml(CUSTOMER_STORE_URL));
    }

    private String buildVoucherExpiredEmailHtml(
            String fullName,
            String maPhieu,
            String tenPhieu,
            Instant ngayKetThuc
    ) {
        String ketThucText = ngayKetThuc != null
                ? VOUCHER_DATE_FORMAT.format(ngayKetThuc)
                : "Không rõ";

        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Phiếu giảm giá đã hết hạn</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f4f7;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
                  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background:#f4f4f7;padding:32px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="max-width:480px;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 12px 30px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="padding:32px 32px 4px 32px;text-align:center;">
                              <div style="font-size:25px;font-weight:800;color:#6b7280;">&#128683; Phiếu giảm giá đã hết hạn</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:18px 36px 0 36px;font-size:15px;line-height:1.6;color:#374151;">
                              Chào bạn <b>__FULL_NAME__</b>,
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:10px 36px 0 36px;font-size:15px;line-height:1.6;color:#6b7280;">
                              Rất tiếc, phiếu giảm giá dưới đây của bạn đã hết hạn và không còn sử dụng được nữa.
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 36px 8px 36px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="border-radius:18px;background:linear-gradient(135deg,#4b5563 0%,#6b7280 45%,#9ca3af 100%);">
                                <tr>
                                  <td style="padding:26px 22px;text-align:center;">
                                    <div style="font-size:13px;font-weight:700;letter-spacing:0.12em;color:#e5e7eb;text-transform:uppercase;">Mã phiếu đã hết hạn</div>
                                    <div style="margin:12px auto;padding:14px 10px;border:2px dashed rgba(255,255,255,0.4);border-radius:12px;max-width:300px;font-size:28px;font-weight:800;letter-spacing:0.08em;color:#e5e7eb;font-family:'Courier New',Courier,monospace;text-decoration:line-through;">__MA__</div>
                                    <div style="font-size:15px;font-weight:600;color:#d1d5db;">__TEN__</div>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:6px 36px 0 36px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="border-radius:10px;background:#fef2f2;border:1px solid #fecaca;">
                                <tr>
                                  <td style="padding:12px 16px;font-size:13px;color:#991b1b;line-height:1.8;">
                                    <b>&#9200; Phiếu đã hết hạn lúc:</b> __KET_THUC__<br>
                                    Phiếu giảm giá này chưa được sử dụng và đã hết hiệu lực.
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:26px 36px 6px 36px;text-align:center;">
                              <a href="__URL__" style="display:inline-block;padding:14px 42px;border-radius:999px;background:#cf1018;color:#ffffff;text-decoration:none;font-size:16px;font-weight:700;box-shadow:0 8px 16px rgba(207,16,24,0.28);">Khám phá ưu đãi mới</a>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:18px 36px 30px 36px;text-align:center;font-size:12px;color:#9ca3af;line-height:1.6;">
                              Email này được gửi tự động từ hệ thống SportShoe.<br>Vui lòng không trả lời trực tiếp email này.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """;

        return html
                .replace("__FULL_NAME__", escapeHtml(fullName))
                .replace("__MA__", escapeHtml(maPhieu))
                .replace("__TEN__", escapeHtml(tenPhieu))
                .replace("__KET_THUC__", escapeHtml(ketThucText))
                .replace("__URL__", escapeHtml(CUSTOMER_STORE_URL));
    }

    private String formatCurrency(BigDecimal value) {
        BigDecimal amount = value == null ? BigDecimal.ZERO : value;
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " VNĐ";
    }

    private String formatPercent(BigDecimal value) {
        BigDecimal amount = value == null ? BigDecimal.ZERO : value.stripTrailingZeros();
        return amount.toPlainString();
    }

    private EmailDispatchResult emailFailure(String userMessage, String recipient, Exception exception) {
        log.error("Không thể gửi email tới {}", recipient, exception);
        return EmailDispatchResult.failure(userMessage);
    }

    private String buildVoucherDeactivatedEmailHtml(
            String fullName,
            String maPhieu,
            String tenPhieu
    ) {
        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Phiếu giảm giá đã ngừng hoạt động</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f4f7;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
                  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background:#f4f4f7;padding:32px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="max-width:480px;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 12px 30px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="padding:32px 32px 4px 32px;text-align:center;">
                              <div style="font-size:25px;font-weight:800;color:#ef4444;">⚠️ Phiếu giảm giá đã ngừng hoạt động</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:18px 36px 0 36px;font-size:15px;line-height:1.6;color:#374151;">
                              Chào bạn <b>__FULL_NAME__</b>,
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:10px 36px 0 36px;font-size:15px;line-height:1.6;color:#6b7280;">
                              Chúng tôi xin thông báo phiếu giảm giá dưới đây của bạn đã được ngưng hoạt động bởi ban quản trị SportShoe.
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 36px 8px 36px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="border-radius:18px;background:linear-gradient(135deg,#f87171 0%,#ef4444 45%,#dc2626 100%);">
                                <tr>
                                  <td style="padding:26px 22px;text-align:center;">
                                    <div style="font-size:13px;font-weight:700;letter-spacing:0.12em;color:#fee2e2;text-transform:uppercase;">Mã phiếu</div>
                                    <div style="margin:12px auto;padding:14px 10px;border:2px dashed rgba(255,255,255,0.4);border-radius:12px;max-width:300px;font-size:28px;font-weight:800;letter-spacing:0.08em;color:#ffffff;font-family:'Courier New',Courier,monospace;">__MA__</div>
                                    <div style="font-size:15px;font-weight:600;color:#fee2e2;">__TEN__</div>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:6px 36px 0 36px;">
                              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="border-radius:10px;background:#fef2f2;border:1px solid #fee2e2;">
                                <tr>
                                  <td style="padding:12px 16px;font-size:13px;color:#991b1b;line-height:1.8;">
                                    Phiếu giảm giá này hiện không còn áp dụng được nữa. Vui lòng sử dụng các mã ưu đãi khác hoặc liên hệ bộ phận hỗ trợ nếu cần thiết.
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:26px 36px 6px 36px;text-align:center;">
                              <a href="__URL__" style="display:inline-block;padding:14px 42px;border-radius:999px;background:#cf1018;color:#ffffff;text-decoration:none;font-size:16px;font-weight:700;box-shadow:0 8px 16px rgba(207,16,24,0.28);">Khám phá ưu đãi khác</a>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:18px 36px 30px 36px;text-align:center;font-size:12px;color:#9ca3af;line-height:1.6;">
                              Email này được gửi tự động từ hệ thống SportShoe.<br>Vui lòng không trả lời trực tiếp email này.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """;

        return html
                .replace("__FULL_NAME__", escapeHtml(fullName))
                .replace("__MA__", escapeHtml(maPhieu))
                .replace("__TEN__", escapeHtml(tenPhieu))
                .replace("__URL__", escapeHtml(CUSTOMER_STORE_URL));
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
