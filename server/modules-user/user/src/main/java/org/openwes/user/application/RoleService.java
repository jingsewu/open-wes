package org.openwes.user.application;

import org.openwes.user.controller.param.role.RoleDTO;
import org.openwes.user.controller.param.role.RoleMenuUpdateParam;
import org.openwes.user.domain.entity.Role;
import jakarta.validation.Valid;

public interface RoleService {

    /**
     * 判断角色编号是否存在
     *
     * @param roleCode 角色编号
     *
     * @return 是否存在
     */
    boolean exist(String roleCode);

    /**
     * 添加角色
     *
     * @param roleDTO 添加参数
     *
     * @throws Exception Exception 异常
     */
    void addRole(@Valid RoleDTO roleDTO);

    /**
     * 修改角色
     *
     * @param param 修改参数
     *
     * @throws Exception 异常
     */
    void updateRole(RoleDTO param);

    /**
     * 删除角色
     *
     * @param roleId 角色id
     *
     * @throws Exception 删除角色异常
     */
    void deleteRole(Long roleId);


    /**
     * 修改角色状态
     *
     * @param roleId 角色id
     * @param status 角色状态
     *
     * @throws Exception 修改异常
     */
    void updateStatus(Long roleId, Integer status);

    /**
     * 修改角色权限
     *
     * @param param 修改角色权限bean
     *
     * @throws Exception
     */
    void updateRoleMenu(RoleMenuUpdateParam param);

    Role getRole(Long id);
}
