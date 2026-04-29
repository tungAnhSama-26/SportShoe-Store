package com.example.server.infrastructure.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class EmailService {

    private static final String SYSTEM_LOGIN_URL = "http://localhost:3000/login";
    private static final String CUSTOMER_LOGIN_URL = "http://localhost:3000/login";
    private static final String LOGO_CONTENT_ID = "sportshoe-logo";
    private static final Path LOGO_PATH = Paths.get(
            "..", "..", "FE", "sport-shoe", "src", "assets", "logo", "delete-background-logo.png"
    ).normalize().toAbsolutePath();

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username:}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendRegistrationEmail(String to, String fullName, String username, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(to);
            helper.setSubject("SportShoe Internal - Tài khoản hệ thống của bạn");
            helper.setText(buildRegistrationEmailHtml(fullName, username, password), true);
            addInlineLogo(helper);
            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Không thể tạo email tài khoản nhân viên", exception);
        }
    }

    public void sendCustomerRegistrationEmail(String to, String fullName, String username, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applyFrom(helper);
            helper.setTo(to);
            helper.setSubject("Chào mừng đến với SportShoe - Tài khoản của bạn");
            helper.setText(buildCustomerRegistrationEmailHtml(fullName, username, password), true);
            addInlineLogo(helper);
            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Không thể tạo email tài khoản khách hàng", exception);
        }
    }

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        applyFrom(message);
        message.setTo(to);
        message.setSubject("Mã xác nhận quên mật khẩu - SportShoe");
        message.setText(String.format(
                "Chào bạn,\n\n" +
                        "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản SportShoe.\n" +
                        "Mã xác nhận của bạn là: %s\n\n" +
                        "Mã này sẽ hết hạn sau ít phút. Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ SportShoe",
                otp
        ));
        mailSender.send(message);
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
        String logoBlock = Files.exists(LOGO_PATH)
                ? """
                  <tr>
                    <td style="padding:0 0 28px 0;">
                      <img src="cid:%s" alt="SportShoe" style="display:block;width:230px;max-width:100%%;height:auto;">
                    </td>
                  </tr>
                  """.formatted(LOGO_CONTENT_ID)
                : "";

        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>SportShoe Internal</title>
                </head>
                <body style="margin:0;padding:0;background:#c91018;font-family:Arial,Helvetica,sans-serif;color:#111827;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:linear-gradient(135deg,#870e14 0%%,#cf1018 38%%,#ef1e24 100%%);">
                    <tr>
                      <td align="center" style="padding:24px;">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="max-width:1420px;">
                          <tr>
                            <td style="padding:0;">
                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                <tr>
                                  <td valign="top" style="width:52%%;padding:48px 36px 24px 24px;">
                                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                      %s
                                      <tr>
                                        <td>
                                          <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                              <td style="padding-right:28px;border-right:1px solid rgba(255,255,255,0.35);">
                                                <div style="font-size:70px;line-height:0.95;font-weight:800;color:#ffffff;">SportShoe</div>
                                                <div style="font-size:68px;line-height:1.02;font-weight:300;color:#ffe3e3;">Internal</div>
                                              </td>
                                              <td style="padding-left:28px;vertical-align:middle;">
                                                <div style="font-size:24px;line-height:1.6;color:#fff2f2;">
                                                  Hệ thống quản lý nội bộ<br>
                                                  chuyên nghiệp và hiệu quả
                                                </div>
                                              </td>
                                            </tr>
                                          </table>
                                        </td>
                                      </tr>
                                      <tr>
                                        <td style="padding-top:110px;">
                                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                              <td valign="top" style="width:33.33%%;padding-right:22px;">
                                                <div style="margin-bottom:14px;"><img src="https://img.icons8.com/ios/100/ffffff/security-checked--v1.png" width="42" height="42" style="display:block;opacity:0.9;" alt="Bảo mật"></div>
                                                <div style="font-size:18px;font-weight:700;color:#ffffff;">Bảo mật</div>
                                                <div style="padding-top:8px;font-size:15px;line-height:1.6;color:#ffe5e5;">An toàn thông tin</div>
                                              </td>
                                              <td valign="top" style="width:33.33%%;padding-right:22px;">
                                                <div style="margin-bottom:14px;"><img src="https://img.icons8.com/ios/100/ffffff/speed.png" width="42" height="42" style="display:block;opacity:0.9;" alt="Hiệu quả"></div>
                                                <div style="font-size:18px;font-weight:700;color:#ffffff;">Hiệu quả</div>
                                                <div style="padding-top:8px;font-size:15px;line-height:1.6;color:#ffe5e5;">Tối ưu quy trình</div>
                                              </td>
                                              <td valign="top" style="width:33.33%%;">
                                                <div style="margin-bottom:14px;"><img src="https://img.icons8.com/ios/100/ffffff/user-group-man-man.png" width="42" height="42" style="display:block;opacity:0.9;" alt="Kết nối"></div>
                                                <div style="font-size:18px;font-weight:700;color:#ffffff;">Kết nối</div>
                                                <div style="padding-top:8px;font-size:15px;line-height:1.6;color:#ffe5e5;">Làm việc cùng nhau</div>
                                              </td>
                                            </tr>
                                          </table>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                  <td valign="top" style="width:48%%;padding:30px 0 0 0;">
                                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#ffffff;border-radius:28px;overflow:hidden;box-shadow:0 30px 60px rgba(93,11,14,0.18);">
                                      <tr>
                                        <td style="padding:42px 54px 0 54px;">
                                          <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                              <td style="width:72px;height:72px;border-radius:999px;background:#fff3f3;text-align:center;vertical-align:middle;">
                                                <img src="https://img.icons8.com/ios/100/e02525/user--v1.png" width="34" height="34" style="display:inline-block;vertical-align:middle;" alt="User">
                                              </td>
                                              <td style="padding-left:24px;">
                                                <div style="font-size:28px;line-height:1.3;font-weight:700;color:#404040;">Xin chào,</div>
                                                <div style="font-size:34px;line-height:1.35;font-weight:800;color:#e02525;">%s 👋</div>
                                              </td>
                                            </tr>
                                          </table>
                                          <div style="padding-top:34px;font-size:18px;line-height:1.65;color:#6b7280;">
                                            Tài khoản truy cập hệ thống của bạn đã được tạo.<br>
                                            Vui lòng sử dụng thông tin dưới đây để đăng nhập.
                                          </div>
                                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="margin-top:28px;border:1px solid #ffd6d6;border-radius:16px;background:#fff7f7;">
                                            <tr>
                                              <td style="padding:18px 22px 14px 22px;">
                                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                                  <tr>
                                                    <td style="width:54px;vertical-align:middle;">
                                                      <div style="width:46px;height:46px;border-radius:999px;background:#ffe9e9;text-align:center;line-height:46px;">
                                                        <img src="https://img.icons8.com/ios/100/e02525/user--v1.png" width="22" height="22" style="display:inline-block;vertical-align:middle;" alt="Username">
                                                      </div>
                                                    </td>
                                                    <td style="font-size:15px;font-weight:700;color:#e02525;text-transform:uppercase;vertical-align:middle;">Username</td>
                                                    <td align="right" style="font-size:22px;font-weight:800;color:#2d2d2d;font-family:'Courier New',Courier,monospace;vertical-align:middle;">%s</td>
                                                  </tr>
                                                  <tr>
                                                    <td colspan="3" style="padding:16px 0 12px 0;">
                                                      <div style="height:1px;background:#ffdede;"></div>
                                                    </td>
                                                  </tr>
                                                  <tr>
                                                    <td style="width:54px;vertical-align:middle;">
                                                      <div style="width:46px;height:46px;border-radius:999px;background:#ffe9e9;text-align:center;line-height:46px;">
                                                        <img src="https://img.icons8.com/ios/100/e02525/lock--v1.png" width="22" height="22" style="display:inline-block;vertical-align:middle;" alt="Password">
                                                      </div>
                                                    </td>
                                                    <td style="font-size:15px;font-weight:700;color:#e02525;text-transform:uppercase;vertical-align:middle;">Temp password</td>
                                                    <td align="right" style="font-size:22px;font-weight:800;color:#2d2d2d;font-family:'Courier New',Courier,monospace;vertical-align:middle;">%s</td>
                                                  </tr>
                                                </table>
                                              </td>
                                            </tr>
                                          </table>
                                          <div style="padding:36px 0 32px 0;text-align:center;">
                                            <a href="%s" style="display:inline-block;min-width:320px;padding:18px 28px;border-radius:12px;background:#d71921;color:#ffffff;text-decoration:none;font-size:20px;font-weight:700;box-shadow:0 8px 16px rgba(215,25,33,0.25);">
                                              <img src="https://img.icons8.com/ios/100/ffffff/lock--v1.png" width="22" height="22" style="display:inline-block;vertical-align:middle;margin-right:8px;margin-bottom:2px;" alt="Lock">
                                              <span style="vertical-align:middle;">Đăng nhập hệ thống</span>
                                            </a>
                                          </div>
                                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="border-radius:12px;background:#fff8ec;">
                                            <tr>
                                              <td style="padding:16px 20px;">
                                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                                  <tr>
                                                    <td style="width:44px;vertical-align:top;">
                                                      <div style="width:32px;height:32px;text-align:center;line-height:32px;">
                                                        <img src="https://img.icons8.com/ios/100/f59e0b/error--v1.png" width="28" height="28" style="display:inline-block;vertical-align:middle;" alt="Warning">
                                                      </div>
                                                    </td>
                                                    <td style="font-size:15px;line-height:1.6;color:#5f6368;padding-left:8px;">
                                                      Bạn cần đổi mật khẩu ngay sau lần đăng nhập đầu tiên để đảm bảo bảo mật.
                                                    </td>
                                                  </tr>
                                                </table>
                                              </td>
                                            </tr>
                                          </table>
                                          <div style="margin-top:28px;height:1px;background:#ececec;"></div>
                                          <div style="padding:26px 0 28px 0;font-size:16px;line-height:1.65;color:#5f6368;">
                                            <div>Trân trọng,</div>
                                            <div style="padding-top:8px;font-size:19px;font-weight:800;color:#1f2937;">
                                              IT Department<br>SportShoe
                                            </div>
                                          </div>
                                        </td>
                                      </tr>
                                      <tr>
                                        <td style="border-top:1px solid #f2e5e5;padding:18px 26px 20px 26px;background:#fafafa;">
                                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                              <td style="font-size:13px;color:#8a8f98;vertical-align:middle;">
                                                <img src="https://img.icons8.com/ios/100/d71921/security-checked--v1.png" width="16" height="16" style="display:inline-block;vertical-align:middle;margin-right:6px;" alt="Shield">
                                                <span style="vertical-align:middle;">Bảo mật tuyệt đối</span>
                                              </td>
                                              <td align="center" style="font-size:13px;color:#8a8f98;vertical-align:middle;">
                                                <img src="https://img.icons8.com/ios/100/d71921/headset--v1.png" width="16" height="16" style="display:inline-block;vertical-align:middle;margin-right:6px;" alt="Headset">
                                                <span style="vertical-align:middle;">Hỗ trợ 24/7</span>
                                              </td>
                                              <td align="right" style="font-size:13px;color:#8a8f98;vertical-align:middle;">
                                                <img src="https://img.icons8.com/ios/100/d71921/mail.png" width="16" height="16" style="display:inline-block;vertical-align:middle;margin-right:6px;" alt="Mail">
                                                <span style="vertical-align:middle;">it-support@sportshoe.vn</span>
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
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                logoBlock,
                escapeHtml(fullName),
                escapeHtml(username),
                escapeHtml(password),
                SYSTEM_LOGIN_URL
        );
    }

    private String buildCustomerRegistrationEmailHtml(String fullName, String username, String password) {
        String logoBlock = Files.exists(LOGO_PATH)
                ? """
                  <tr>
                    <td style="padding:0 0 28px 0;">
                      <img src="cid:%s" alt="SportShoe" style="display:block;width:230px;max-width:100%%;height:auto;">
                    </td>
                  </tr>
                  """.formatted(LOGO_CONTENT_ID)
                : "";

        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>SportShoe Store</title>
                </head>
                <body style="margin:0;padding:0;background:#c91018;font-family:Arial,Helvetica,sans-serif;color:#111827;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:linear-gradient(135deg,#870e14 0%%,#cf1018 38%%,#ef1e24 100%%);">
                    <tr>
                      <td align="center" style="padding:24px;">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="max-width:1420px;">
                          <tr>
                            <td style="padding:0;">
                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                <tr>
                                  <td valign="top" style="width:52%%;padding:48px 36px 24px 24px;">
                                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                      %s
                                      <tr>
                                        <td>
                                          <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                              <td style="padding-right:28px;border-right:1px solid rgba(255,255,255,0.35);">
                                                <div style="font-size:70px;line-height:0.95;font-weight:800;color:#ffffff;">SportShoe</div>
                                                <div style="font-size:68px;line-height:1.02;font-weight:300;color:#ffe3e3;">Store</div>
                                              </td>
                                              <td style="padding-left:28px;vertical-align:middle;">
                                                <div style="font-size:24px;line-height:1.6;color:#fff2f2;">
                                                  Trải nghiệm mua sắm<br>
                                                  giày thể thao đỉnh cao
                                                </div>
                                              </td>
                                            </tr>
                                          </table>
                                        </td>
                                      </tr>
                                      <tr>
                                        <td style="padding-top:110px;">
                                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                              <td valign="top" style="width:33.33%%;padding-right:22px;">
                                                <div style="margin-bottom:14px;"><img src="https://img.icons8.com/ios/100/ffffff/shopping-bag--v1.png" width="42" height="42" style="display:block;opacity:0.9;" alt="Chất lượng"></div>
                                                <div style="font-size:18px;font-weight:700;color:#ffffff;">Chất lượng</div>
                                                <div style="padding-top:8px;font-size:15px;line-height:1.6;color:#ffe5e5;">Sản phẩm chính hãng</div>
                                              </td>
                                              <td valign="top" style="width:33.33%%;padding-right:22px;">
                                                <div style="margin-bottom:14px;"><img src="https://img.icons8.com/ios/100/ffffff/truck.png" width="42" height="42" style="display:block;opacity:0.9;" alt="Giao hàng"></div>
                                                <div style="font-size:18px;font-weight:700;color:#ffffff;">Giao hàng</div>
                                                <div style="padding-top:8px;font-size:15px;line-height:1.6;color:#ffe5e5;">Nhanh chóng tiện lợi</div>
                                              </td>
                                              <td valign="top" style="width:33.33%%;">
                                                <div style="margin-bottom:14px;"><img src="https://img.icons8.com/ios/100/ffffff/like--v1.png" width="42" height="42" style="display:block;opacity:0.9;" alt="Ưu đãi"></div>
                                                <div style="font-size:18px;font-weight:700;color:#ffffff;">Ưu đãi</div>
                                                <div style="padding-top:8px;font-size:15px;line-height:1.6;color:#ffe5e5;">Nhiều khuyến mãi hấp dẫn</div>
                                              </td>
                                            </tr>
                                          </table>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                  <td valign="top" style="width:48%%;padding:30px 0 0 0;">
                                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#ffffff;border-radius:28px;overflow:hidden;box-shadow:0 30px 60px rgba(93,11,14,0.18);">
                                      <tr>
                                        <td style="padding:42px 54px 0 54px;">
                                          <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                              <td style="width:72px;height:72px;border-radius:999px;background:#fff3f3;text-align:center;vertical-align:middle;">
                                                <img src="https://img.icons8.com/ios/100/e02525/user--v1.png" width="34" height="34" style="display:inline-block;vertical-align:middle;" alt="User">
                                              </td>
                                              <td style="padding-left:24px;">
                                                <div style="font-size:28px;line-height:1.3;font-weight:700;color:#404040;">Xin chào,</div>
                                                <div style="font-size:34px;line-height:1.35;font-weight:800;color:#e02525;">%s 👋</div>
                                              </td>
                                            </tr>
                                          </table>
                                          <div style="padding-top:34px;font-size:18px;line-height:1.65;color:#6b7280;">
                                            Tài khoản của bạn đã được đăng ký thành công trên SportShoe.<br>
                                            Vui lòng sử dụng thông tin dưới đây để đăng nhập.
                                          </div>
                                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="margin-top:28px;border:1px solid #ffd6d6;border-radius:16px;background:#fff7f7;">
                                            <tr>
                                              <td style="padding:18px 22px 14px 22px;">
                                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                                  <tr>
                                                    <td style="width:54px;vertical-align:middle;">
                                                      <div style="width:46px;height:46px;border-radius:999px;background:#ffe9e9;text-align:center;line-height:46px;">
                                                        <img src="https://img.icons8.com/ios/100/e02525/user--v1.png" width="22" height="22" style="display:inline-block;vertical-align:middle;" alt="Username">
                                                      </div>
                                                    </td>
                                                    <td style="font-size:15px;font-weight:700;color:#e02525;text-transform:uppercase;vertical-align:middle;">Tên đăng nhập</td>
                                                    <td align="right" style="font-size:22px;font-weight:800;color:#2d2d2d;font-family:'Courier New',Courier,monospace;vertical-align:middle;">%s</td>
                                                  </tr>
                                                  <tr>
                                                    <td colspan="3" style="padding:16px 0 12px 0;">
                                                      <div style="height:1px;background:#ffdede;"></div>
                                                    </td>
                                                  </tr>
                                                  <tr>
                                                    <td style="width:54px;vertical-align:middle;">
                                                      <div style="width:46px;height:46px;border-radius:999px;background:#ffe9e9;text-align:center;line-height:46px;">
                                                        <img src="https://img.icons8.com/ios/100/e02525/lock--v1.png" width="22" height="22" style="display:inline-block;vertical-align:middle;" alt="Password">
                                                      </div>
                                                    </td>
                                                    <td style="font-size:15px;font-weight:700;color:#e02525;text-transform:uppercase;vertical-align:middle;">Mật khẩu</td>
                                                    <td align="right" style="font-size:22px;font-weight:800;color:#2d2d2d;font-family:'Courier New',Courier,monospace;vertical-align:middle;">%s</td>
                                                  </tr>
                                                </table>
                                              </td>
                                            </tr>
                                          </table>
                                          <div style="padding:36px 0 32px 0;text-align:center;">
                                            <a href="%s" style="display:inline-block;min-width:320px;padding:18px 28px;border-radius:12px;background:#d71921;color:#ffffff;text-decoration:none;font-size:20px;font-weight:700;box-shadow:0 8px 16px rgba(215,25,33,0.25);">
                                              <img src="https://img.icons8.com/ios/100/ffffff/login-rounded-right--v1.png" width="22" height="22" style="display:inline-block;vertical-align:middle;margin-right:8px;margin-bottom:2px;" alt="Login">
                                              <span style="vertical-align:middle;">Đăng nhập mua sắm</span>
                                            </a>
                                          </div>
                                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="border-radius:12px;background:#fff8ec;">
                                            <tr>
                                              <td style="padding:16px 20px;">
                                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                                  <tr>
                                                    <td style="width:44px;vertical-align:top;">
                                                      <div style="width:32px;height:32px;text-align:center;line-height:32px;">
                                                        <img src="https://img.icons8.com/ios/100/f59e0b/error--v1.png" width="28" height="28" style="display:inline-block;vertical-align:middle;" alt="Warning">
                                                      </div>
                                                    </td>
                                                    <td style="font-size:15px;line-height:1.6;color:#5f6368;padding-left:8px;">
                                                      Bạn nên đổi mật khẩu ngay sau lần đăng nhập đầu tiên để đảm bảo bảo mật.
                                                    </td>
                                                  </tr>
                                                </table>
                                              </td>
                                            </tr>
                                          </table>
                                          <div style="margin-top:28px;height:1px;background:#ececec;"></div>
                                          <div style="padding:26px 0 28px 0;font-size:16px;line-height:1.65;color:#5f6368;">
                                            <div>Trân trọng,</div>
                                            <div style="padding-top:8px;font-size:19px;font-weight:800;color:#1f2937;">
                                              Đội ngũ SportShoe
                                            </div>
                                          </div>
                                        </td>
                                      </tr>
                                      <tr>
                                        <td style="border-top:1px solid #f2e5e5;padding:18px 26px 20px 26px;background:#fafafa;">
                                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                              <td style="font-size:13px;color:#8a8f98;vertical-align:middle;">
                                                <img src="https://img.icons8.com/ios/100/d71921/security-checked--v1.png" width="16" height="16" style="display:inline-block;vertical-align:middle;margin-right:6px;" alt="Shield">
                                                <span style="vertical-align:middle;">Bảo mật tuyệt đối</span>
                                              </td>
                                              <td align="center" style="font-size:13px;color:#8a8f98;vertical-align:middle;">
                                                <img src="https://img.icons8.com/ios/100/d71921/headset--v1.png" width="16" height="16" style="display:inline-block;vertical-align:middle;margin-right:6px;" alt="Headset">
                                                <span style="vertical-align:middle;">Hỗ trợ 24/7</span>
                                              </td>
                                              <td align="right" style="font-size:13px;color:#8a8f98;vertical-align:middle;">
                                                <img src="https://img.icons8.com/ios/100/d71921/mail.png" width="16" height="16" style="display:inline-block;vertical-align:middle;margin-right:6px;" alt="Mail">
                                                <span style="vertical-align:middle;">support@sportshoe.vn</span>
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
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                logoBlock,
                escapeHtml(fullName),
                escapeHtml(username),
                escapeHtml(password),
                CUSTOMER_LOGIN_URL
        );
    }

    private void addInlineLogo(MimeMessageHelper helper) throws MessagingException {
        if (!Files.exists(LOGO_PATH)) {
            return;
        }
        helper.addInline(LOGO_CONTENT_ID, new FileSystemResource(LOGO_PATH));
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
