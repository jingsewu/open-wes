package org.openwes.user.application.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openwes.user.api.IUserRoleApi;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.User;
import org.openwes.user.domain.entity.UserRole;
import org.openwes.user.domain.repository.RoleMapper;
import org.openwes.user.domain.repository.UserMapper;
import org.openwes.user.domain.repository.UserRoleMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Primary
@Service
@RequiredArgsConstructor
public class UserRoleApiImpl implements IUserRoleApi {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @Override
    public Set<String> findRoleCodes(String username) {
        if (StringUtils.isBlank(username)) {
            return Set.of();
        }
        User user = userMapper.findByAccount(username);
        if (user == null) {
            return Set.of();
        }
        List<UserRole> links = userRoleMapper.findByUserId(user.getId());
        if (links.isEmpty()) {
            return Set.of();
        }
        List<Long> roleIds = links.stream().map(UserRole::getRoleId).distinct().toList();
        return roleMapper.findAllById(roleIds).stream()
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
