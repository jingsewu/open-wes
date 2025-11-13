package org.openwes.api.platform.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.api.platform.domain.entity.ApiLogPO;
import org.openwes.api.platform.domain.repository.ApiLogPORepository;
import org.openwes.api.platform.domain.service.ApiLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiLogServiceImpl implements ApiLogService {

    private final ApiLogPORepository apiLogPORepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByDate(Date date) {
        apiLogPORepository.deleteByCreateTimeBefore(date.getTime());
    }

    @Override
    @Async("logExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void saveApiLogAsync(ApiLogPO apiLogPO) {
        try {
            apiLogPORepository.save(apiLogPO);
        } catch (Exception e) {
            log.error("Failed to save log asynchronously: {}", apiLogPO, e);
        }
    }
}
