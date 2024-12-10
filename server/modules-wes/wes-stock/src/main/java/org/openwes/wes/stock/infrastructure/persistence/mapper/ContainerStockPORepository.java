package org.openwes.wes.stock.infrastructure.persistence.mapper;

import org.openwes.wes.stock.infrastructure.persistence.po.ContainerStockPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ContainerStockPORepository extends JpaRepository<ContainerStockPO, Long> {

    ContainerStockPO findBySkuBatchStockIdAndContainerCodeAndContainerSlotCode(Long skuBatchStockId, String containerCode, String containerSlotCode);

    List<ContainerStockPO> findAllByContainerCodeInAndWarehouseCode(Collection<String> containerCodes, String warehouseCode);

    List<ContainerStockPO> findAllBySkuIdIn(Collection<Long> skuIds);

    List<ContainerStockPO> findAllBySkuId(Long skuId);

    Page<ContainerStockPO> findAllBySkuBatchStockIdIn(Collection<Long> skuBatchStockIds, PageRequest pageRequest);

    Page<ContainerStockPO> findAllBySkuIdIn(Collection<Long> skuIds, PageRequest pageRequest);

    List<ContainerStockPO> findAllByWarehouseCodeAndContainerCodeAndContainerSlotCode(String warehouseCode, String containerCode, String containerSlotCode);

    List<ContainerStockPO> findAllByWarehouseCodeAndContainerCodeAndContainerFace(String warehouseCode, String containerCode, String containerFace);
}
