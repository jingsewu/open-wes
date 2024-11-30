package org.openwes.user.application.impl;

import org.openwes.user.application.RoleMenuService;
import org.openwes.user.domain.repository.RoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class RoleMenuServiceImpl implements RoleMenuService {

    private final RoleMenuMapper roleMenuMapper;

    @Override
    public void removeByMenuId(Collection<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }

        roleMenuMapper.deleteByMenuIdIn(menuIds);
    }

    @Override
    public void removeByRoleId(Long roleId) {
        if (null == roleId) {
            return;
        }
        roleMenuMapper.deleteByRoleId(roleId);
    }
}
