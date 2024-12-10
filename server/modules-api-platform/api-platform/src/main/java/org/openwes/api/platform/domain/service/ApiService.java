package org.openwes.api.platform.domain.service;

import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.api.platform.controller.param.api.ApiAddParam;
import org.openwes.api.platform.controller.param.api.ApiUpdateParam;

public interface ApiService {
    ApiPO getByCode(String targetApi);

    void addApi(ApiAddParam param);

    void updateApi(ApiUpdateParam param);

    void deleteApiById(Long id);
}
