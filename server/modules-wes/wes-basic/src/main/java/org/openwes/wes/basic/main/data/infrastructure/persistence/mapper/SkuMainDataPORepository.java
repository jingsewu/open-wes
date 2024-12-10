package org.openwes.wes.basic.main.data.infrastructure.persistence.mapper;

import org.openwes.wes.basic.main.data.infrastructure.persistence.po.SkuMainDataPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SkuMainDataPORepository extends JpaRepository<SkuMainDataPO, Long> {

    SkuMainDataPO findBySkuCodeAndOwnerCode(String skuCode, String ownerCode);

    SkuMainDataPO findFirstBySkuCode(String skuCode);

    List<SkuMainDataPO> findAllBySkuCodeInAndOwnerCode(Collection<String> skuCodes, String ownerCode);

    Page<SkuMainDataPO> findAllByHeatIn(Collection<String> heat, Pageable pageable);

    List<SkuMainDataPO> findAllBySkuCode(String skuCode);

    List<SkuMainDataPO> findAllBySkuCodeIn(Collection<String> skuCodes);
}
