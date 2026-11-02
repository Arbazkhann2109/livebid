package com.livebid.admin.controller;

import com.livebid.user.entity.User;
import com.livebid.user.repository.UserRepository;
import com.livebid.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserPageController {

    private final UserRepository userRepository;
    private final UserService userService;

    public AdminUserPageController(
            UserRepository userRepository,
            UserService userService
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    public String users(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        int pageNumber = Math.max(page, 0);

        PageRequest pageable = PageRequest.of(
                pageNumber,
                20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> users;

        if (search.isBlank()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository
                    .findByDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            search.trim(),
                            search.trim(),
                            pageable
                    );
        }

        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", pageNumber);

        return "admin/users";
    }

    @PostMapping("/{userId}/suspend")
    public String suspendUser(
            @PathVariable Long userId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User admin = userService.getByEmail(authentication.getName());

        userService.suspendUser(userId, admin.getId());

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "User account suspended successfully."
        );

        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/reactivate")
    public String reactivateUser(
            @PathVariable Long userId,
            RedirectAttributes redirectAttributes
    ) {
        userService.reactivateUser(userId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "User account reactivated successfully."
        );

        return "redirect:/admin/users";
    }
}