package ru.bank.auth_service.infrastructure.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.bank.auth_service.model.enums.Role;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Data
public class CustomUserDetails implements UserDetails {

    private final String identifier;
    private final Role role;
    private final UUID userId;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String identifier, Role role, UUID userId) {
        this.identifier = identifier;
        this.role = role;
        this.userId = userId;
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return identifier;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
