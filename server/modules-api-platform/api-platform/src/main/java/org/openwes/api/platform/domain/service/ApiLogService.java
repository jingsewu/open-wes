package org.openwes.api.platform.domain.service;

import org.openwes.api.platform.domain.entity.ApiLogPO;

import java.util.Date;

public interface ApiLogService {
    void removeByDate(Date date);

    void saveApiLogAsync(ApiLogPO apiLogPO);
}
