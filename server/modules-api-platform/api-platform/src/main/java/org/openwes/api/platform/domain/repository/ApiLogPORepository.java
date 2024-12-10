package org.openwes.api.platform.domain.repository;

import org.openwes.api.platform.api.constants.ApiLogStatusEnum;
import org.openwes.api.platform.domain.entity.ApiLogPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiLogPORepository extends JpaRepository<ApiLogPO, Long> {

    List<ApiLogPO> findAllByStatusAndRetryCountLessThanAndCreateTimeAfter(ApiLogStatusEnum apiLogStatus, int retryCount, Long createTime);

    void deleteByCreateTimeBefore(Long createTime);
}
