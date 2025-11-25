package com.tuition.backend.Service;

import com.tuition.backend.Security.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities() == null) {
            throw new RuntimeException("Unauthorized: Unable to read user role");
        }

        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        return auth.getName();
    }
}
