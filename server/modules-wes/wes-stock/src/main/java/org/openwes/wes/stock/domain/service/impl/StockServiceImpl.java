package org.openwes.wes.stock.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.stock.domain.entity.ContainerStock;
import org.openwes.wes.stock.domain.entity.SkuBatchStock;
import org.openwes.wes.stock.domain.repository.ContainerStockRepository;
import org.openwes.wes.stock.domain.repository.SkuBatchStockRepository;
import org.openwes.wes.stock.domain.service.StockService;
import org.openwes.wes.stock.domain.transfer.ContainerStockTransfer;
import org.openwes.wes.stock.domain.transfer.SkuBatchStockTransfer;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final ContainerStockRepository containerStockRepository;
    private final SkuBatchStockRepository skuBatchStockRepository;
    private final SkuBatchStockTransfer skuBatchStockTransfer;
    private final ContainerStockTransfer containerStockTransfer;

    public ContainerStock transferContainerStock(StockTransferDTO stockTransferDTO, ContainerStock containerStock,
                                                 Long targetSkuBatchId, boolean unlock) {

        if (unlock) {
            containerStock.subtractAndUnlockQty(stockTransferDTO.getTransferQty(), stockTransferDTO.getLockType());
        } else {
            containerStock.adjustQty(-stockTransferDTO.getTransferQty());
        }

        //need add or update container stock
        ContainerStock targetContainerStock = containerStockRepository.findByContainerAndSlotAndSkuBatch(
                stockTransferDTO.getTargetContainerCode(), stockTransferDTO.getTargetContainerSlotCode(), targetSkuBatchId);
        if (targetContainerStock != null) {
            targetContainerStock.addQty(stockTransferDTO.getTransferQty());
        } else {
            targetContainerStock = containerStockTransfer.toDO(stockTransferDTO, targetSkuBatchId);
        }

        return targetContainerStock;
    }

    public SkuBatchStock transferSkuBatchStock(SkuBatchStock skuBatchStock, StockTransferDTO stockTransferDTO, boolean unlock) {

        if (Objects.equals(stockTransferDTO.getWarehouseAreaId(), skuBatchStock.getWarehouseAreaId())) {
            return skuBatchStock;
        }
        if (unlock) {
            skuBatchStock.subtractAndUnlockQty(stockTransferDTO.getTransferQty(), stockTransferDTO.getLockType());
        } else {
            skuBatchStock.adjustQty(-stockTransferDTO.getTransferQty());
        }

        SkuBatchStock targetSkuBatchStock = skuBatchStockRepository.findBySkuBatchAttributeIdAndWarehouseAreaId(
                stockTransferDTO.getSkuBatchAttributeId(), stockTransferDTO.getWarehouseAreaId());
        if (targetSkuBatchStock == null) {
            targetSkuBatchStock = skuBatchStockTransfer.toDO(stockTransferDTO);
        } else {
            targetSkuBatchStock.addQty(stockTransferDTO.getTransferQty());
        }

        return targetSkuBatchStock;
    }

}
