package org.openwes.user.application.model;

import com.google.common.collect.Sets;
import org.openwes.user.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class UserDetailsModel implements UserDetails {


    private final User user;
    private final Set<? extends GrantedAuthority> grantedAuthorities;

    public UserDetailsModel(User user, Set<? extends GrantedAuthority> grantedAuthorities) {
        this.user = user;

        if (grantedAuthorities == null) {
            this.grantedAuthorities = Sets.newHashSet();
        } else {
            this.grantedAuthorities = grantedAuthorities;
        }
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getAccount();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getLocked() == 0;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        Integer status = user.getStatus();
        return status != null && status == 1;
    }

    public User getUser() {
        return user;
    }
}
