package org.openwes.user.application.impl;

import com.google.common.base.Objects;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.UserErrorDescEnum;
import org.openwes.user.api.dto.constants.YesOrNoEnum;
import org.openwes.user.application.RoleMenuService;
import org.openwes.user.application.RoleService;
import org.openwes.user.application.UserRoleService;
import org.openwes.user.config.prop.SystemProp;
import org.openwes.user.controller.param.role.RoleDTO;
import org.openwes.user.controller.param.role.RoleMenuUpdateParam;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.RoleMenu;
import org.openwes.user.domain.repository.RoleMapper;
import org.openwes.user.domain.repository.RoleMenuMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMenuService roleMenuService;
    private final UserRoleService userRoleService;
    private final SystemProp systemProp;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public boolean exist(String roleCode) {
        if (StringUtils.isEmpty(roleCode)) {
            return false;
        }
        return roleMapper.findByCode(roleCode) != null;
    }

    @Override
    public void addRole(@Valid RoleDTO roleDTO) {
        checkRoleCode(roleDTO.getCode());
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        roleMapper.save(role);
    }

    @Override
    public void updateRole(RoleDTO param) {
        Role role = roleMapper.findById(param.getId()).orElseThrow();
        if (!Objects.equal(role.getCode(), param.getCode())) {
            checkRoleCode(param.getCode());
        }
        checkSuperRole(role);
        BeanUtils.copyProperties(param, role);
        roleMapper.save(role);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteRole(Long roleId) {
        if (null == roleId) {
            return;
        }
        Role role = roleMapper.findById(roleId).orElseThrow(() -> new WmsException(UserErrorDescEnum.ERR_ROLE_NOT_FOUND));
        checkSuperRole(role);
        checkRoleStatus(role);
        // 先删除角色, 再删除关联权限和菜单
        roleMapper.delete(role);
        roleMenuService.removeByRoleId(roleId);
        // 解除该角色对应的用户
        userRoleService.removeByRoleId(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateStatus(Long roleId, Integer status) {
        if (null == roleId) {
            return;
        }
        Role role = roleMapper.findById(roleId).orElseThrow(() -> new WmsException(UserErrorDescEnum.ERR_ROLE_NOT_FOUND));
        checkSuperRole(role);
        if (Objects.equal(role.getStatus(), status)) {
            return;
        }
        role.setStatus(status);
        roleMapper.save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleMenu(RoleMenuUpdateParam param) {
        Role role = roleMapper.findById(param.getRoleId()).orElseThrow(() -> new WmsException(UserErrorDescEnum.ERR_ROLE_NOT_FOUND));
        checkSuperRole(role);
        roleMenuService.removeByRoleId(param.getRoleId());
        Set<Long> menus = param.getMenuSet();
        if (menus == null || menus.isEmpty()) {
            return;
        }
        List<RoleMenu> roleMenus = menus.stream()
            .map(m -> {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(param.getRoleId());
                roleMenu.setMenuId(m);
                return roleMenu;
            }).toList();

        roleMenuMapper.saveAll(roleMenus);
    }

    @Override
    public Role getRole(Long id) {
        return roleMapper.findById(id).orElseThrow();
    }

    private void checkSuperRole(Role role) {
        String superAdminCode = systemProp.getSuperRoleCode();
        if (Objects.equal(superAdminCode, role.getCode())) {
            throw new WmsException(UserErrorDescEnum.ERR_ROLE_ADMIN_IS_IMMUTABLE);
        }
    }

    private void checkRoleCode(String roleCode) {
        if (exist(roleCode)) {
            throw new WmsException(UserErrorDescEnum.ERR_ROLE_CODE_EXISTS);
        }
    }

    private void checkRoleStatus(Role role) {
        // 当角色处于启用状态且被用户关联时，不允许删除
        if (Objects.equal(role.getStatus().toString(), YesOrNoEnum.YES.getValue())
            && !CollectionUtils.isEmpty(userRoleService.getByRoleId(role.getId()))) {
            throw new WmsException(UserErrorDescEnum.ERR_ROLE_IS_ENABLE_AND_USED);
        }
    }
}
