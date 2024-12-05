package org.openwes.wes.basic.main.data.infrastructure.persistence.mapper;

import org.openwes.wes.basic.main.data.infrastructure.persistence.po.SkuBarCodeDataPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SkuBarCodeDataPORepository extends JpaRepository<SkuBarCodeDataPO, Long> {

    List<SkuBarCodeDataPO> findAllBySkuId(Long skuId);

    List<SkuBarCodeDataPO> findAllBySkuIdIn(Collection<Long> skuIds);

    List<SkuBarCodeDataPO> findAllByBarCodeOrSkuCode(String barCode, String skuCode);

    List<SkuBarCodeDataPO> findAllByBarCode(String barcode);

}
