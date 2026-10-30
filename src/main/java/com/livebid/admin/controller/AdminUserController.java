
package com.livebid.admin.controller;

import com.livebid.admin.dto.UserStatusResponse;
import com.livebid.user.entity.User;
import com.livebid.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{userId}/suspend")
    @ResponseStatus(HttpStatus.OK)
    public UserStatusResponse suspendUser(
            @PathVariable Long userId,
            Authentication authentication) {

        User admin = userService.getByEmail(authentication.getName());

        User user = userService.suspendUser(
                userId,
                admin.getId()
        );

        return new UserStatusResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getStatus(),
                "User account suspended successfully."
        );
    }

    @PostMapping("/{userId}/reactivate")
    @ResponseStatus(HttpStatus.OK)
    public UserStatusResponse reactivateUser(
            @PathVariable Long userId) {

        User user = userService.reactivateUser(userId);

        return new UserStatusResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getStatus(),
                "User account reactivated successfully."
        );
    }
}
