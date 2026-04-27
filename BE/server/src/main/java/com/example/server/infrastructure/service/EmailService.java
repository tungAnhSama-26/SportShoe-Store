package com.example.server.infrastructure.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRegistrationEmail(String to, String fullName, String username, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Chào mừng bạn đến với SportShoe - Đăng ký thành công!");
        message.setText(String.format(
                "Chào %s,\n\n" +
                "Cảm ơn bạn đã đăng ký tài khoản tại cửa hàng SportShoe chúng tôi.\n" +
                "Dưới đây là thông tin đăng nhập của bạn:\n" +
                "- Tên đăng nhập: %s\n" +
                "- Mật khẩu: %s\n\n" +
                "Vui lòng đăng nhập và đổi mật khẩu để đảm bảo an toàn.\n" +
                "Chúc bạn có những trải nghiệm mua sắm tuyệt vời!\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ SportShoe",
                fullName, username, password
        ));
        mailSender.send(message);
    }

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
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
}
