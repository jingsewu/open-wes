package org.openwes.wes.config.domain.repository;

import org.openwes.wes.config.domain.entity.SystemConfig;

public interface SystemConfigRepository {

    void save(SystemConfig systemConfig);

    SystemConfig findSystemConfig();

}
