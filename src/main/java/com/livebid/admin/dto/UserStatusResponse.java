
package com.livebid.admin.dto;

public record UserStatusResponse(
        Long userId,
        String email,
        String displayName,
        String status,
        String message
) {
}
