
package com.livebid.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountPageController {

    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @PostMapping("/logout")
    public String logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        request.getSession(false);

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        SecurityContextHolder.clearContext();

        return "redirect:/";
    }
}
