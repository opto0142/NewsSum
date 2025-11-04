package com.newssum.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.newssum.domain.User;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Spring Security {@link UserDetails} wrapper for {@link User} documents.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPrincipal implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = -1823301668788018938L;

    private final String id;
    private final String email;
    private final String password;
    private final boolean premium;
    private final List<GrantedAuthority> authorities;

    public static UserPrincipal from(final User user) {
        final List<GrantedAuthority> grantedAuthorities = user.getRoles().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toUnmodifiableList());
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), user.isPremium(), grantedAuthorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
