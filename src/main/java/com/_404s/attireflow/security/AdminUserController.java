package com._404s.attireflow.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminUserController {

    private final AppUserService appUserService;

    public AdminUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping("/admin/register-staff")
    public String registerStaff(@ModelAttribute StaffRegistrationForm staffRegistrationForm,
                                RedirectAttributes redirectAttributes) {
        try {
            appUserService.registerUser(
                    staffRegistrationForm.getUsername(),
                    staffRegistrationForm.getPassword(),
                    staffRegistrationForm.getRole()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Staff user registered successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}/update")
    public String updateUser(@PathVariable Long id,
                             @org.springframework.web.bind.annotation.RequestParam String username,
                             @org.springframework.web.bind.annotation.RequestParam AppRole role,
                             RedirectAttributes redirectAttributes) {
        try {
            appUserService.updateUser(id, username, role);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication != null ? authentication.getName() : null;

            AppUser userToDelete = appUserService.getUserById(id);

            if (userToDelete != null && userToDelete.getUsername().equals(currentUsername)) {
                throw new IllegalArgumentException("You cannot delete your own account.");
            }

            appUserService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin";
    }
}
