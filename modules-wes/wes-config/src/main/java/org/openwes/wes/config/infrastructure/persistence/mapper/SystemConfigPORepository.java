package org.openwes.wes.config.infrastructure.persistence.mapper;

import org.openwes.wes.config.infrastructure.persistence.po.SystemConfigPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigPORepository extends JpaRepository<SystemConfigPO, Long> {
}
