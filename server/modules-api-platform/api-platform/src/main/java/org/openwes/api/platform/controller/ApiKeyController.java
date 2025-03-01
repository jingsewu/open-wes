package org.openwes.api.platform.controller;

import lombok.RequiredArgsConstructor;
import org.openwes.api.platform.domain.entity.ApiKeyPO;
import org.openwes.api.platform.domain.service.ApiKeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping("create")
    public ResponseEntity<ApiKeyPO> createApiKey(@RequestBody ApiKeyPO apiKey) {
        ApiKeyPO createdApiKey = apiKeyService.createApiKey(apiKey);
        return ResponseEntity.ok(createdApiKey);
    }

    @PostMapping("getAll")
    public ResponseEntity<List<ApiKeyPO>> getAllApiKeys() {
        List<ApiKeyPO> apiKeys = apiKeyService.getAllApiKeys();
        return ResponseEntity.ok(apiKeys);
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiKeyPO> getApiKeyById(@PathVariable Long id) {
        Optional<ApiKeyPO> apiKey = apiKeyService.getApiKeyById(id);
        return apiKey.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApiKey(@PathVariable Long id) {
        apiKeyService.deleteApiKey(id);
        return ResponseEntity.noContent().build();
    }
}
