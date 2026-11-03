
package com.livebid.security;

import com.livebid.user.service.PasswordResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;

@Controller
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(
            PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        passwordResetService.createResetToken(email);

        redirectAttributes.addFlashAttribute(
                "message",
                "If an account exists with that email, a password reset link will be sent."
        );

        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam(required = false) String token,
            Model model) {

        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (password == null || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            redirectAttributes.addFlashAttribute(
                    "resetError",
                    "Password must be at least 8 characters and no more than 72 UTF-8 bytes."
            );
            return "redirect:/reset-password?token=" + token;
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute(
                    "resetError",
                    "Passwords do not match."
            );
            return "redirect:/reset-password?token=" + token;
        }

        try {
            passwordResetService.resetPassword(token, password);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Password updated successfully. Please log in."
            );

            return "redirect:/login";

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "resetError",
                    "Invalid or expired reset link. Please request a new one."
            );

            return "redirect:/forgot-password";
        }
    }
}
