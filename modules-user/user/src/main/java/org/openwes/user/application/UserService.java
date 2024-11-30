package org.openwes.user.application;

import org.openwes.user.application.model.PermissionGrantedAuthority;
import org.openwes.user.controller.param.user.UserDTO;
import org.openwes.user.domain.entity.User;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

public interface UserService extends UserDetailsService {


    User getByAccount(String account);

    /**
     * 添加用户
     *
     * @param userDTO 添加用户参数
     *
     * @throws Exception 添加异常
     */
    void addUser(@Valid UserDTO userDTO);


    /**
     * 修改用户
     *
     * @param param 更新用户参数
     *
     * @throws Exception 添加异常
     */
    void updateUser(UserDTO param);

    /**
     * 修改用户状态
     *
     * @param userId 用户id
     * @param status 状态
     *
     * @throws Exception 更新状态异常
     */
    void updateStatus(Long userId, Integer status);

    /**
     * 重置用户密码
     *
     * @param userId      用户id
     * @param newPassword 重置的新密码
     *
     * @throws Exception 重置密码异常
     */
    void resetPassword(Long userId, String newPassword);


    /**
     * 通过用户id删除用户
     *
     * @param userId 用户id
     *
     * @throws Exception
     */
    void delete(Long userId);

    /**
     * 获取当前用户所拥有的权限
     *
     * @param user 系统用户
     *
     * @return 权限集合
     */
    Set<PermissionGrantedAuthority> getPermissionModels(User user);

    User getById(Long id);
}
