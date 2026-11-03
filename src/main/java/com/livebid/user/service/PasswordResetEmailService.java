
package com.livebid.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class PasswordResetEmailService {

    private final RestClient restClient;
    private final String fromEmail;
    private final String baseUrl;

    public PasswordResetEmailService(
            @Value("${app.resend.api-key}") String apiKey,
            @Value("${app.resend.from}") String fromEmail,
            @Value("${app.frontend.base-url}") String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        this.fromEmail = fromEmail;
        this.baseUrl = baseUrl;
    }

    public void sendResetEmail(String recipientEmail, String token) {

        String resetLink = baseUrl
                + "/reset-password?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);

        String emailText =
                "Hello,\n\n"
                        + "We received a request to reset your LiveBid password.\n\n"
                        + "Click the link below to reset your password:\n"
                        + resetLink + "\n\n"
                        + "This link expires in 15 minutes and can only be used once.\n\n"
                        + "If you did not request this, please ignore this email.\n\n"
                        + "Regards,\nLiveBid Team";

        Map<String, Object> email = Map.of(
                "from", fromEmail,
                "to", List.of(recipientEmail),
                "subject", "LiveBid - Password Reset Request",
                "text", emailText
        );

        restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(email)
                .retrieve()
                .toBodilessEntity();
    }
}
