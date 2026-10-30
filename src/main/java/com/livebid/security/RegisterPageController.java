
package com.livebid.security;

import com.livebid.user.dto.RegisterRequest;
import com.livebid.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterPageController {

    private final UserService userService;

    public RegisterPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registration", new RegisterRequest("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registration") RegisterRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.register(request);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("registrationError", exception.getMessage());
            return "register";
        }
    }
}
