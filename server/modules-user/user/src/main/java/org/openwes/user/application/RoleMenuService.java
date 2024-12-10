package org.openwes.user.application;

import java.util.Collection;

public interface RoleMenuService {

    void removeByMenuId(Collection<Long> menuIds);

    void removeByRoleId(Long roleId);

}
