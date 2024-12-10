package org.openwes.api.platform.domain.repository;

import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiConfigPORepository extends JpaRepository<ApiConfigPO, Long> {

    ApiConfigPO findByCode(String code);
}
