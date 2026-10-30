
package com.livebid.security;

import com.livebid.user.dto.LoginRequest;
import com.livebid.user.dto.RegisterRequest;
import com.livebid.user.entity.User;
import com.livebid.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(
            @Valid @RequestBody RegisterRequest request) {

        try {
            User user = userService.register(request);
            return new RegistrationResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getDisplayName(),
                    "Registration successful."
            );
        } catch (IllegalArgumentException exception) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            jakarta.servlet.http.HttpServletResponse servletResponse) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.email().trim(),
                            request.password()
                    )
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Persist the authenticated context in the HTTP session.
            securityContextRepository.saveContext(
                    context, servletRequest, servletResponse);

            User user = userService.getByEmail(authentication.getName());

            return new LoginResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getDisplayName(),
                    "Login successful."
            );
        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password."
            );
        }
    }

    public record RegistrationResponse(
            Long id, String email, String displayName, String message) {}

    public record LoginResponse(
            Long id, String email, String displayName, String message) {}
}
