package org.openwes.user.application.impl;

import org.openwes.user.application.UserRoleService;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.User;
import org.openwes.user.domain.entity.UserRole;
import org.openwes.user.domain.repository.RoleMapper;
import org.openwes.user.domain.repository.UserMapper;
import org.openwes.user.domain.repository.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleMapper userRoleMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @Override
    public void add(Long userId, Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<UserRole> userRoles = roleIds.stream()
                .filter(Objects::nonNull)
                .map(roleId -> {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    return userRole;
                }).toList();
        if (userRoles.isEmpty()) {
            return;
        }
        userRoleMapper.saveAll(userRoles);
    }

    @Override
    public void removeByRoleId(Long roleId) {
        if (null == roleId) {
            return;
        }
        userRoleMapper.deleteByRoleId(roleId);
    }

    @Override
    public void removeByUserId(Long userId) {
        if (null == userId) {
            return;
        }
        userRoleMapper.deleteByUserId(userId);
    }

    @Override
    public List<UserRole> getByUserId(Long userId) {
        if (null == userId) {
            return Collections.emptyList();
        }
        return userRoleMapper.findByUserId(userId);
    }

    @Override
    public List<UserRole> getByRoleId(Long roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }

        return userRoleMapper.findByRoleId(roleId);
    }

    @Override
    public List<Role> getByUserName(String currentUser) {
        User user = userMapper.findByAccount(currentUser);
        List<UserRole> userRoles = userRoleMapper.findByUserId(user.getId());
        return roleMapper.findAllById(userRoles.stream().map(UserRole::getRoleId).toList());
    }
}
