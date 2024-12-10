package org.openwes.user.application.model;

import org.openwes.common.utils.user.AuthConstants;
import org.openwes.user.domain.entity.Role;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Data
public class RoleGrantedAuthority implements GrantedAuthority {

    private final String authority;

    public RoleGrantedAuthority(Role role) {
        this.authority = AuthConstants.ROLE_GRANTED_AUTHORITY_PREFIX + role.getCode();
    }

    @Override
    public String getAuthority() {
        return authority;
    }


}
