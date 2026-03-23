package com._404s.attireflow.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class TestAuthController {

    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @GetMapping("/test-login/{role}")
    public String testLogin(@PathVariable AppRole role,
                            HttpServletRequest request,
                            HttpServletResponse response) {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                role.getDisplayName(),
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );

        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        return "redirect:/dashboard";
    }

    @GetMapping("/test-logout")
    public String testLogout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        var emptyContext = SecurityContextHolder.createEmptyContext();
        securityContextRepository.saveContext(emptyContext, request, response);

        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/dashboard";
    }
}
