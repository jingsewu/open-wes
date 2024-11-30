package org.openwes.api.platform.domain.service.impl;

import org.openwes.api.platform.domain.repository.ApiLogPORepository;
import org.openwes.api.platform.domain.service.ApiLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ApiLogServiceImpl implements ApiLogService {

    private final ApiLogPORepository apiLogPORepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByDate(Date date) {
        apiLogPORepository.deleteByCreateTimeBefore(date.getTime());
    }
}
