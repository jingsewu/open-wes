package org.openwes.user.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Data
@AllArgsConstructor
public class PermissionGrantedAuthority implements GrantedAuthority {

    private final String permission;

    @Override
    public String getAuthority() {
        return permission;
    }

}
