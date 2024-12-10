package org.openwes.user.domain.repository;

import org.openwes.user.domain.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginLogMapper extends JpaRepository<LoginLog, Long> {

}
