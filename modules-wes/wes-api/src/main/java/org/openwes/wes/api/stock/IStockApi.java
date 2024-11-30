package org.openwes.wes.api.stock;

import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.stock.dto.ContainerStockLockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockLockDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.Collection;
import java.util.List;

public interface IStockApi {

    void addAndLockSkuBatchStock(@Valid List<SkuBatchStockLockDTO> skuBatchStockLockDTOS);

    void lockSkuBatchStock(@Valid List<SkuBatchStockLockDTO> skuBatchStockLockDTOS);

    void lockContainerStock(@Valid List<ContainerStockLockDTO> containerStockLockDTOS);

    void addAndLockContainerStock(@Valid List<ContainerStockLockDTO> containerStockLockDTOS);

    void freezeContainerStock(Long id, int qty);

    void unfreezeContainerStock(Long id, int qty);

    /**
     * query sku batch stock by sku batch attribute ids
     *
     * @param skuBatchAttributeIds sku batch attribute ids
     * @return
     */
    List<SkuBatchStockDTO> getBySkuBatchAttributeIds(@NotEmpty Collection<Long> skuBatchAttributeIds);

    List<SkuBatchStockDTO> getBySkuBatchAttributeId(Long skuBatchAttributeId);

    List<ContainerStockDTO> getContainerStockBySkuBatchStockIds(@NotEmpty Collection<Long> skuBatchStockIds);

    List<ContainerStockDTO> getContainerStocks(Collection<String> containerCodes, String warehouseCode);

    List<ContainerStockDTO> getContainerStocks(List<Long> containerStockIds);

    SkuBatchStockDTO getSkuBatchStock(Long skuBatchStockId);

    List<SkuBatchStockDTO> getSkuBatchStocks(Collection<Long> skuBatchStockIds);

    List<ContainerStockDTO> getContainerStocksBySlotCode(String warehouseCode, String containerCode, String containerSlotCode);

    List<ContainerStockDTO> getBySkuIds(List<Long> skuIds);

    List<ContainerStockDTO> getByContainerAndFace(String warehouseCode, String containerCode, String containerFace);

}
