package org.openwes.api.platform.domain.repository;

import org.openwes.api.platform.domain.entity.ApiPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiPORepository extends JpaRepository<ApiPO, Long> {

    ApiPO findByCode(String code);
}
