package org.openwes.api.platform.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.api.platform.domain.entity.ApiKeyPO;
import org.openwes.api.platform.domain.repository.ApiKeyRepository;
import org.openwes.api.platform.domain.service.ApiKeyService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyPO createApiKey(ApiKeyPO apiKey) {

        apiKey.generateApiKey();
        return apiKeyRepository.save(apiKey);
    }

    public List<ApiKeyPO> getAllApiKeys() {
        return apiKeyRepository.findAll();
    }

    public Optional<ApiKeyPO> getApiKeyById(Long id) {
        return apiKeyRepository.findById(id);
    }

    public void deleteApiKey(Long id) {
        apiKeyRepository.deleteById(id);
    }

    @Cacheable(value = "apiKeyCache", key = "#apiKey")
    @Override
    public ApiKeyPO getApiKey(String apiKey) {
        return apiKeyRepository.findByApiKey(apiKey);
    }
}
