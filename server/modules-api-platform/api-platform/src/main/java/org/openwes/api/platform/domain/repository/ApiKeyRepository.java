package org.openwes.api.platform.domain.repository;

import org.openwes.api.platform.domain.entity.ApiKeyPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKeyPO, Long> {
    ApiKeyPO findByApiKey(String apiKey);
}
