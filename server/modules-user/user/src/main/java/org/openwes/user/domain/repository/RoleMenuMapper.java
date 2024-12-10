package org.openwes.user.domain.repository;

import org.openwes.user.domain.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RoleMenuMapper extends JpaRepository<RoleMenu, Long> {

    List<RoleMenu> findByRoleIdIn(Collection<Long> roleIds);

    void deleteByMenuIdIn(Collection<Long> menuIds);

    void deleteByRoleId(Long roleId);
}
