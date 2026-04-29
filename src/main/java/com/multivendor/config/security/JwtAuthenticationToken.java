package com.multivendor.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String email;
    private final Long userId;
    private final String role;

    public JwtAuthenticationToken(String email, Long userId, String role,
                                   Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.email = email;
        this.userId = userId;
        this.role = role;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getPrincipal() { return email; }

    public Long getUserId() { return userId; }

    public String getRole() { return role; }
}
