package org.openwes.user.domain.repository;

import org.openwes.user.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RoleMapper extends JpaRepository<Role, Long> {

    List<Role> findByCodeIn(Collection<String> codes);

    Role findByCode(String code);
}
