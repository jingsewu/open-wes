package org.openwes.user.domain.repository;

import org.openwes.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMapper extends JpaRepository<User, Long> {
    User findByAccount(String account);
}
