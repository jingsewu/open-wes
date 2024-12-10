package org.openwes.api.platform.domain.service.impl;

import org.openwes.api.platform.controller.param.apiconfig.ApiConfigUpdateParam;
import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.openwes.api.platform.domain.repository.ApiConfigPORepository;
import org.openwes.api.platform.domain.service.ApiConfigService;
import org.openwes.common.utils.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiConfigServiceImpl implements ApiConfigService {

    private final ApiConfigPORepository apiConfigPORepository;

    @Override
    @Cacheable(value = RedisConstants.API_CONFIG_CACHE, key = "#apiCode")
    public ApiConfigPO getByCode(String apiCode) {
        return apiConfigPORepository.findByCode(apiCode);
    }

    @Override
    @CacheEvict(value = RedisConstants.API_CONFIG_CACHE, key = "#apiConfigUpdateParam.code")
    public void updateConfig(ApiConfigUpdateParam apiConfigUpdateParam) {
        Long id = apiConfigUpdateParam.getId();
        Optional<ApiConfigPO> apiConfigPOOptional =
                id == null ? Optional.empty() : apiConfigPORepository.findById(id);
        ApiConfigPO apiConfigPO;
        apiConfigPO = apiConfigPOOptional.orElseGet(ApiConfigPO::new);
        BeanUtils.copyProperties(apiConfigUpdateParam, apiConfigPO);
        apiConfigPORepository.save(apiConfigPO);
    }
}
