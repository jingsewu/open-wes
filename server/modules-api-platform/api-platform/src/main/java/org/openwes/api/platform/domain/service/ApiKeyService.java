package org.openwes.api.platform.domain.service;

import org.openwes.api.platform.domain.entity.ApiKeyPO;

import java.util.List;
import java.util.Optional;

public interface ApiKeyService {


    ApiKeyPO createApiKey(ApiKeyPO apiKey);

    List<ApiKeyPO> getAllApiKeys();

    Optional<ApiKeyPO> getApiKeyById(Long id);

    void deleteApiKey(Long id);

    ApiKeyPO getApiKey(String apiKey);
}
