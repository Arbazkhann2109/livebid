package com.livebid.security;

import com.livebid.user.entity.User;
import com.livebid.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GoogleOAuth2UserService extends OidcUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public GoogleOAuth2UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest request)
            throws OAuth2AuthenticationException {

        OidcUser googleUser = super.loadUser(request);
        Map<String, Object> attributes = googleUser.getClaims();

        String email = (String) attributes.get("email");
        Boolean emailVerified = (Boolean) attributes.get("email_verified");

        if (email == null || !Boolean.TRUE.equals(emailVerified)) {
            throw oauthError("Google account does not have a verified email.");
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        String displayName = (String) attributes.get("name");

        if (displayName == null || displayName.isBlank()) {
            displayName = email.substring(0, email.indexOf('@'));
        }

        displayName = displayName.trim();

        if (displayName.length() > 100) {
            displayName = displayName.substring(0, 100);
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = createGoogleUser(email, displayName);
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw oauthError("This account is suspended.");
        }

        return new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                googleUser.getIdToken(),
                googleUser.getUserInfo(),
                "email"
        );
    }

    private User createGoogleUser(String email, String displayName) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String randomPassword = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String passwordHash = passwordEncoder.encode(randomPassword);

        return userRepository.save(
                User.create(email, passwordHash, displayName)
        );
    }

    private OAuth2AuthenticationException oauthError(String message) {
        return new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user_info"),
                message
        );
    }
}