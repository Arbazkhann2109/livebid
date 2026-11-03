
package com.livebid.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.base-url}")
    private String baseUrl;

    public PasswordResetEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetEmail(String recipientEmail, String token) {
        String resetLink = baseUrl
                + "/reset-password?token="
                + java.net.URLEncoder.encode(
                token,
                java.nio.charset.StandardCharsets.UTF_8
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("LiveBid - Password Reset Request");
        message.setText(
                "Hello,\n\n"
                        + "We received a request to reset your LiveBid password.\n\n"
                        + "Click the link below to reset your password:\n"
                        + resetLink + "\n\n"
                        + "This link expires in 15 minutes and can only be used once.\n\n"
                        + "If you did not request this, please ignore this email.\n\n"
                        + "Regards,\nLiveBid Team"
        );

        mailSender.send(message);
    }
}
