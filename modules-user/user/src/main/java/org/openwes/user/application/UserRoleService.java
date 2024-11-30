package org.openwes.user.application;

import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.UserRole;

import java.util.List;
import java.util.Set;

public interface UserRoleService {

    /**
     * 新增映射
     *
     * @param userId  用户id
     * @param roleIds 角色id集合
     */
    void add(Long userId, Set<Long> roleIds);

    /**
     * 删除角色与用户的关联
     *
     * @param roleId 角色id
     */
    void removeByRoleId(Long roleId);

    /**
     * 删除角色与用户的关联
     *
     * @param userId 用户id
     */
    void removeByUserId(Long userId);

    /**
     * 通过用户名获取角色关联
     *
     * @param userId 用户id
     *
     * @return List<UserRole>
     */
    List<UserRole> getByUserId(Long userId);

    List<UserRole> getByRoleId(Long roleId);

    List<Role> getByUserName(String currentUser);
}
