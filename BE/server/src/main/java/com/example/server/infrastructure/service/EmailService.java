package com.example.server.infrastructure.service;

import com.example.server.infrastructure.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
    private static final DateTimeFormatter VOUCHER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

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
            Instant ngayKetThuc
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(to);
            helper.setSubject("🎁 Quà tặng từ SportShoe - Phiếu giảm giá dành riêng cho bạn");
            helper.setText(
                    buildVoucherGiftEmailHtml(fullName, maPhieu, tenPhieu, loai, giaTri, giamToiDa, giaTriToiThieu, ngayKetThuc),
                    true
            );
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            log.error("Không thể gửi email tặng phiếu giảm giá tới {}", to, exception);
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
                .replace("__HAN__", escapeHtml(hanText))
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
