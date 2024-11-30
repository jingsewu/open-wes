package org.openwes.user.domain.repository;

import org.openwes.user.domain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleMapper extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    void deleteByRoleId(Long roleId);

    void deleteByUserId(Long userId);
}
