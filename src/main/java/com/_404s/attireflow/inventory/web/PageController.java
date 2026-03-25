package com._404s.attireflow.inventory.web;

import com._404s.attireflow.security.AppUserService;
import com._404s.attireflow.security.StaffRegistrationForm;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final AppUserService appUserService;

    public PageController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/registration")
    public String registrationPage(Model model) {
        model.addAttribute("staffRegistrationForm", new StaffRegistrationForm());
        return "registration";
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("users", appUserService.getAllUsers());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserLabel = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "Not logged in";
        model.addAttribute("currentUserLabel", currentUserLabel);

        return "admin";
    }
}
