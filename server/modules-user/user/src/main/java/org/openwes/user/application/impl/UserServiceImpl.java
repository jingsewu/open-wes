package org.openwes.user.application.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.UserErrorDescEnum;
import org.openwes.common.utils.user.AuthConstants;
import org.openwes.common.utils.user.UserContext;
import org.openwes.user.api.dto.constants.UserTypeEnum;
import org.openwes.user.api.dto.constants.YesOrNoEnum;
import org.openwes.user.application.UserRoleService;
import org.openwes.user.application.UserService;
import org.openwes.user.application.model.PermissionGrantedAuthority;
import org.openwes.user.application.model.UserDetailsModel;
import org.openwes.user.config.prop.SystemProp;
import org.openwes.user.controller.param.user.UserDTO;
import org.openwes.user.domain.entity.*;
import org.openwes.user.domain.repository.*;
import org.openwes.user.domain.transfer.UserTransfer;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final RoleMapper roleMapper;
    private final UserRoleService userRoleService;
    private final MenuMapper menuMapper;
    private final SystemProp systemProp;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserTransfer userTransfer;

    @Override
    public User getByAccount(String account) {
        return userMapper.findByAccount(account);
    }

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        if (StringUtils.isEmpty(account)) {
            throw new WmsException(UserErrorDescEnum.ERR_WRONG_CREDENTIALS);
        }
        User user = userMapper.findByAccount(account);
        if (user == null) {
            throw new WmsException(UserErrorDescEnum.ERR_WRONG_CREDENTIALS);
        }

        if (!user.getAccount().equals(account)) {
            //mysql对大小写不敏感,单独处理大小写不匹配
            throw new WmsException(UserErrorDescEnum.ERR_WRONG_CREDENTIALS);
        }

        if (UserTypeEnum.NORMAL.getValue().equalsIgnoreCase(user.getType())) {
            return new UserDetailsModel(user, getPermissionModels(user));
        } else {
            throw new WmsException(UserErrorDescEnum.ERR_WRONG_CREDENTIALS);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUser(@Valid UserDTO userDTO) {
        String username = userDTO.getAccount();
        User databaseUser = getByAccount(username);
        if (databaseUser != null) {
            throw new WmsException(UserErrorDescEnum.ERR_USER_NAME_EXISTS);
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        // 设置密码
        String password = user.getPassword();
        user.setPassword(passwordEncoder.encode(password));
        user.setLocked(0);
        user.setTenantName("");
        userMapper.save(user);

        if (ObjectUtils.isNotEmpty(userDTO.getRoleIds())) {
            List<UserRole> userRoles = getUserRole(user.getId(), userDTO.getRoleIds());
            // 分配角色
            userRoleMapper.saveAll(userRoles);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUser(UserDTO param) {
        checkSuperUser(param.getId());
        User user = userMapper.findById(param.getId()).orElseThrow(() -> new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND));
        if (!Objects.equals(user.getAccount(), param.getAccount())) {
            if (getByAccount(param.getAccount()) != null) {
                throw new WmsException(UserErrorDescEnum.ERR_USER_NAME_EXISTS);
            }
            // 禁止第三方账号修改其在海柔系统内的识别标识：用户名
            if (!Objects.equals(UserTypeEnum.NORMAL, UserTypeEnum.getByCode(user.getType()))) {
                throw new WmsException("update external account username is not allowed");
            }
        }
        userTransfer.updateDO(user, param);
        userMapper.save(user);

        userRoleService.removeByUserId(param.getId());
        userRoleService.add(param.getId(), param.getRoleIds());
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        if (status == null) {
            return;
        }
        checkSuperUser(userId);
        User user = userMapper.findById(userId).orElseThrow(() -> new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND));
        if (Objects.equals(user.getStatus(), status)) {
            return;
        }
        if (Objects.equals(status.toString(), YesOrNoEnum.NO.getValue())) {
            checkSelfUser(userId);
        }
        user.setStatus(status);
        userMapper.save(user);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        if (newPassword.length() < 6) {
            throw new WmsException(UserErrorDescEnum.ERR_CRED_TOO_SHORT);
        }
        User user = userMapper.findById(userId).orElseThrow(() -> new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND));
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId) {
        if (null == userId) {
            return;
        }
        User user = userMapper.findById(userId).orElseThrow(() -> new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND));
        checkSuperUser(userId);
        checkDisabledUser(userId);
        checkSelfUser(userId);
        userMapper.delete(user);
        userRoleService.removeByUserId(userId);
    }

    @Override
    public Set<PermissionGrantedAuthority> getPermissionModels(User user) {
        List<UserRole> userRoles = userRoleMapper.findByUserId(user.getId());
        List<Role> roles = roleMapper.findAllById(userRoles.stream().map(UserRole::getRoleId).toList());

        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptySet();
        }

        boolean haveSuperRole = roles.stream().anyMatch(v -> Objects.equals(systemProp.getSuperRoleCode(), v.getCode()));
        if (haveSuperRole) {
            // 如果是超级角色, 则返回 *:* 权限
            return Sets.newHashSet(new PermissionGrantedAuthority(AuthConstants.SUPPER_PERMISSION));
        }

        // 普通用户，至少拥有获取权限的权限
        Set<PermissionGrantedAuthority> grantedAuthorities = Sets.newHashSet();
        List<RoleMenu> roleMenus = roleMenuMapper.findByRoleIdIn(roles.stream().map(Role::getId).toList());
        List<Menu> menus = menuMapper.findAllById(roleMenus.stream().map(RoleMenu::getMenuId).toList());
        // 拥有的权限
        if (CollectionUtils.isEmpty(menus)) {
            return grantedAuthorities;
        }

        for (Menu menu : menus.stream().filter(Objects::nonNull).toList()) {
            String permissionsString = menu.getPermissions();
            if (StringUtils.isEmpty(permissionsString)) {
                continue;
            }
            if (permissionsString.contains(",")) {
                List<String> splitPermissions = Splitter.on(",")
                        .trimResults()
                        .omitEmptyStrings()
                        .splitToList(permissionsString);
                for (String splitPermission : splitPermissions) {
                    grantedAuthorities.add(new PermissionGrantedAuthority(splitPermission));
                }
            } else {
                grantedAuthorities.add(new PermissionGrantedAuthority(permissionsString));
            }
        }
        return grantedAuthorities;
    }

    @Override
    public User getById(Long id) {
        return userMapper.findById(id).orElseThrow(() -> new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND));
    }

    private List<UserRole> getUserRole(Long userId, Set<Long> roleIds) {
        List<Role> roles = roleMapper.findAllById(roleIds);
        if (CollectionUtils.isEmpty(roles)) {
            throw new WmsException(UserErrorDescEnum.ERR_ROLE_NOT_FOUND);
        }
        List<UserRole> userRoles = Lists.newArrayListWithCapacity(roles.size());
        for (Role role : roles) {
            if (role == null) {
                continue;
            }
            if (role.getStatus() == 0) {
                throw new WmsException(UserErrorDescEnum.ERR_ROLE_IS_DISABLE);
            }
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRoles.add(userRole);
        }
        if (userRoles.isEmpty()) {
            throw new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND);
        }
        return userRoles;
    }

    private void checkSuperUser(Long userId) {
        Long superAdminId = systemProp.getSuperAdminId();
        if (Objects.equals(superAdminId, userId)) {
            throw new WmsException(UserErrorDescEnum.ERR_ROLE_ADMIN_IS_IMMUTABLE);
        }
    }

    private void checkDisabledUser(Long userId) {
        // 不允许删除启用中的用户
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        if (user.getStatus().toString().equals(YesOrNoEnum.YES.getValue())) {
            throw new WmsException(UserErrorDescEnum.ERR_USER_IS_NOT_DISABLED);
        }
    }

    private void checkSelfUser(Long userId) {
        // 不允许当前用户删除或禁用自身
        String currentUsername = UserContext.getCurrentUser();
        if (StringUtils.isEmpty(currentUsername)) {
            throw new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND);
        }
        User currentUser = getByAccount(currentUsername);
        if (currentUser == null) {
            throw new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND);
        }
        if (Objects.equals(currentUser.getId(), userId)) {
            throw new WmsException(UserErrorDescEnum.ERR_USER_CAN_NOT_DISABLE_OR_DELETE_SELF);
        }
    }
}
