
package com.livebid.user.service;

import com.livebid.user.entity.PasswordResetToken;
import com.livebid.user.entity.User;
import com.livebid.user.repository.PasswordResetTokenRepository;
import com.livebid.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 15;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetEmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void createResetToken(String email) {
        userRepository.findByEmail(email.trim().toLowerCase())
                .filter(user -> "ACTIVE".equals(user.getStatus()))
                .ifPresent(user -> {
                    tokenRepository.deleteByUserId(user.getId());

                    byte[] randomBytes = new byte[32];
                    secureRandom.nextBytes(randomBytes);

                    String token = Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(randomBytes);

                    PasswordResetToken resetToken = new PasswordResetToken(
                            token,
                            user,
                            LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES)
                    );

                    tokenRepository.save(resetToken);

                    emailService.sendResetEmail(user.getEmail(), token);
                });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid or expired reset link."));

        if (resetToken.isUsed()
                || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Invalid or expired reset link.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        resetToken.markAsUsed();
    }
}
