package com.locadora_rdt_backend.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityAuthenticationFacade implements AuthenticationFacade {

    private static final String SYSTEM_USER = "SYSTEM";

    @Override
    public String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return SYSTEM_USER;
        }

        String username = authentication.getName();

        if (username == null || username.trim().isEmpty()) {
            return SYSTEM_USER;
        }

        return username;
    }
}
