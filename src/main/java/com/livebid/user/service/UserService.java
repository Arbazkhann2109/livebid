
package com.livebid.user.service;

import com.livebid.user.dto.RegisterRequest;
import com.livebid.user.entity.User;
import com.livebid.user.repository.UserRepository;
import com.livebid.common.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + id));
    }

    public User getByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found for email: " + normalizedEmail));
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Registration request is required.");
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String displayName = request.displayName().trim();
        String password = request.password();

        if (email.isBlank() || displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "Email and display name are required.");
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Password must not exceed 72 UTF-8 bytes.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists.");
        }

        User user = User.create(
                email,
                passwordEncoder.encode(password),
                displayName
        );

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(
                    "An account with this email already exists.");
        }
    }

    // Suspend a user account.
    @Transactional
    public User suspendUser(Long userId, Long adminId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }

        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be positive.");
        }

        if (userId.equals(adminId)) {
            throw new IllegalStateException(
                    "Administrators cannot suspend their own account.");
        }

        User user = getById(userId);

        if ("SUSPENDED".equals(user.getStatus())) {
            throw new IllegalStateException(
                    "User account is already suspended.");
        }

        user.setStatus("SUSPENDED");

        return userRepository.save(user);
    }

    // Reactivate a suspended user account.
    @Transactional
    public User reactivateUser(Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }

        User user = getById(userId);

        if ("ACTIVE".equals(user.getStatus())) {
            throw new IllegalStateException(
                    "User account is already active.");
        }

        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }
}
