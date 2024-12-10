package org.openwes.api.platform.domain.service;

import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.openwes.api.platform.controller.param.apiconfig.ApiConfigUpdateParam;

public interface ApiConfigService {

    ApiConfigPO getByCode(String apiCode);

    void updateConfig(ApiConfigUpdateParam apiConfigUpdateParam);
}
