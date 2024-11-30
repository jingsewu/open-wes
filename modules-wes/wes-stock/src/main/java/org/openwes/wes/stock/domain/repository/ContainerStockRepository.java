package org.openwes.wes.stock.domain.repository;

import org.openwes.wes.stock.domain.entity.ContainerStock;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ContainerStockRepository {

    void save(ContainerStock containerStock);

    void saveAll(List<ContainerStock> toContainerStocks);

    ContainerStock findById(Long stockId);

    List<ContainerStock> findAllByIds(Collection<Long> containerStockIds);

    ContainerStock findByContainerAndSlotAndSkuBatch(String targetContainerCode, String targetContainerSlotCode, Long skuBatchStockId);

    List<ContainerStock> findAllBySkuBatchStockIds(Collection<Long> skuBatchIds);

    List<ContainerStock> findAllByContainerCodesAndWarehouseCode(Collection<String> containerCodes, String warehouseCode);

    List<ContainerStock> findAllBySkuIds(Collection<Long> skuIds);

    List<ContainerStock> findAllBySkuId(Long skuId);

    void clearContainerStockByIds(Set<Long> stockIds);

    List<ContainerStock> findAllByContainerAndSlotCode(String warehouseCode, String containerCode, String containerSlotCode);

    List<ContainerStock> findAllByContainerAndFaceAndWarehouseCode(String containerCode, String containerFace, String warehouseCode);
}
