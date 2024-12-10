package org.openwes.api.platform.domain.service.impl;

import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.api.platform.domain.repository.ApiPORepository;
import org.openwes.api.platform.controller.param.api.ApiAddParam;
import org.openwes.api.platform.controller.param.api.ApiUpdateParam;
import org.openwes.api.platform.domain.service.ApiService;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.ApiPlatformErrorDescEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiServiceImpl implements ApiService {

    private final ApiPORepository apiPORepository;

    @Override
    @Cacheable(value = RedisConstants.API_CACHE, key = "#code")
    public ApiPO getByCode(String code) {
        return apiPORepository.findByCode(code);
    }

    @Override
    public void addApi(ApiAddParam param) {
        ApiPO databaseApi = getByCode(param.getCode());
        if (databaseApi != null) {
            throw new WmsException(ApiPlatformErrorDescEnum.ERR_API_CODE_EXISTS);
        }

        ApiPO apiPO = new ApiPO();
        BeanUtils.copyProperties(param, apiPO);

        apiPORepository.save(apiPO);
    }

    @Override
    @CacheEvict(value = RedisConstants.API_CACHE, key = "#param.code")
    public void updateApi(ApiUpdateParam param) {
        Optional<ApiPO> apiPOOptional = apiPORepository.findById(param.getId());
        if (apiPOOptional.isEmpty()) {
            throw new WmsException(ApiPlatformErrorDescEnum.ERR_API_NOT_EXISTS);
        }

        ApiPO databaseApi = apiPOOptional.get();
        BeanUtils.copyProperties(param, databaseApi);
        apiPORepository.save(databaseApi);
    }

    @Override
    public void deleteApiById(Long id) {
        apiPORepository.deleteById(id);
    }
}
