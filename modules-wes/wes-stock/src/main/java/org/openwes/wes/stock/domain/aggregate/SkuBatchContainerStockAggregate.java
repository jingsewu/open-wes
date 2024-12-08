package org.openwes.wes.stock.domain.aggregate;

import org.openwes.wes.api.stock.dto.StockCreateDTO;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.stock.domain.entity.ContainerStock;
import org.openwes.wes.stock.domain.entity.ContainerStockTransaction;
import org.openwes.wes.stock.domain.entity.SkuBatchStock;
import org.openwes.wes.stock.domain.repository.ContainerStockRepository;
import org.openwes.wes.stock.domain.repository.ContainerStockTransactionRepository;
import org.openwes.wes.stock.domain.repository.SkuBatchStockRepository;
import org.openwes.wes.stock.domain.service.StockService;
import org.openwes.wes.stock.domain.transfer.ContainerStockTransactionTransfer;
import org.openwes.wes.stock.domain.transfer.ContainerStockTransfer;
import org.openwes.wes.stock.domain.transfer.SkuBatchStockTransfer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class SkuBatchContainerStockAggregate {

    private final SkuBatchStockRepository skuBatchStockRepository;
    private final SkuBatchStockTransfer skuBatchStockTransfer;
    private final ContainerStockTransactionRepository containerStockTransactionRepository;
    private final ContainerStockTransactionTransfer containerStockTransactionTransfer;
    private final ContainerStockTransfer containerStockTransfer;
    private final ContainerStockRepository containerStockRepository;
    private final StockService stockService;

    @Transactional(rollbackFor = Exception.class)
    public void createStock(@Valid StockCreateDTO stockCreateDTO, Long eventId) {

        //1. create sku batch stock
        SkuBatchStock skuBatchStock = skuBatchStockRepository
                .findBySkuBatchAttributeIdAndWarehouseAreaId(stockCreateDTO.getSkuBatchAttributeId(), stockCreateDTO.getWarehouseAreaId());
        if (skuBatchStock == null) {
            skuBatchStock = skuBatchStockTransfer.fromCreateDTOtoDO(stockCreateDTO);
        } else {
            skuBatchStock.addQty(stockCreateDTO.getTransferQty());
        }
        SkuBatchStock savedSkuBatchStock = skuBatchStockRepository.save(skuBatchStock);

        //2. create container stock transaction
        ContainerStockTransaction containerStockTransaction = containerStockTransactionTransfer.fromCreateDTOtoDO(stockCreateDTO, savedSkuBatchStock.getId());
        containerStockTransaction.setOperationTaskType(OperationTaskTypeEnum.PUT_AWAY);
        containerStockTransaction.setId(eventId);
        containerStockTransactionRepository.save(containerStockTransaction);

        //3. create container stock
        ContainerStock targetContainerStock = containerStockRepository.findByContainerAndSlotAndSkuBatch(
                stockCreateDTO.getTargetContainerCode(), stockCreateDTO.getTargetContainerSlotCode(), savedSkuBatchStock.getId());
        if (targetContainerStock != null) {
            targetContainerStock.addQty(stockCreateDTO.getTransferQty());
        } else {
            targetContainerStock = containerStockTransfer.fromCreateDTOtoDO(stockCreateDTO, savedSkuBatchStock.getId());
        }
        containerStockRepository.save(targetContainerStock);
    }

    @Transactional(rollbackFor = Exception.class)
    public void transferStock(StockTransferDTO stockTransferDTO, ContainerStock containerStock, Long eventId) {
        saveTransactionRecord(stockTransferDTO, containerStock, eventId);

        SkuBatchStock skuBatchStock = skuBatchStockRepository.findById(stockTransferDTO.getSkuBatchStockId());
        SkuBatchStock targetSkuBatchStock = stockService.transferSkuBatchStock(skuBatchStock, stockTransferDTO, false);

        skuBatchStockRepository.save(skuBatchStock);
        SkuBatchStock saveNewSkuBatchStock = skuBatchStockRepository.save(targetSkuBatchStock);

        ContainerStock targetContainerStock = stockService.transferContainerStock(stockTransferDTO, containerStock, saveNewSkuBatchStock.getId(), false);

        containerStockRepository.save(containerStock);
        containerStockRepository.save(targetContainerStock);
    }

    @Transactional(rollbackFor = Exception.class)
    public void transferAndUnlockStock(StockTransferDTO stockTransferDTO, ContainerStock containerStock, Long eventId) {
        saveTransactionRecord(stockTransferDTO, containerStock, eventId);

        SkuBatchStock skuBatchStock = skuBatchStockRepository.findById(stockTransferDTO.getSkuBatchStockId());
        SkuBatchStock targetSkuBatchStock = stockService.transferSkuBatchStock(skuBatchStock, stockTransferDTO, true);

        skuBatchStockRepository.save(skuBatchStock);

        SkuBatchStock saveNewSkuBatchStock = skuBatchStockRepository.save(targetSkuBatchStock);
        ContainerStock targetContainerStock = stockService.transferContainerStock(stockTransferDTO, containerStock, saveNewSkuBatchStock.getId(), true);

        containerStockRepository.save(containerStock);
        containerStockRepository.save(targetContainerStock);
    }

    private void saveTransactionRecord(StockTransferDTO stockTransferDTO, ContainerStock containerStock, Long eventId) {
        ContainerStockTransaction containerStockTransaction = containerStockTransactionTransfer.toDO(stockTransferDTO);
        //use evenId as the transaction record id to ensure the event only be executed once.
        containerStockTransaction.setId(eventId);
        containerStockTransaction.setSourceContainerCode(containerStock.getContainerCode());
        containerStockTransaction.setSourceContainerSlotCode(containerStock.getContainerSlotCode());
        containerStockTransaction.setContainerCode(containerStock.getContainerCode());
        containerStockTransaction.setContainerSlotCode(containerStock.getContainerSlotCode());
        containerStockTransactionRepository.save(containerStockTransaction);
    }

    @Transactional(rollbackFor = Exception.class)
    public void freezeStock(Long containerStockId, int qty) {
        ContainerStock containerStock = containerStockRepository.findById(containerStockId);
        containerStock.freezeQty(qty);
        containerStockRepository.save(containerStock);

        SkuBatchStock skuBatchStock = skuBatchStockRepository.findById(containerStock.getSkuBatchStockId());
        skuBatchStock.freezeQty(qty);
        skuBatchStockRepository.save(skuBatchStock);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unfreezeStock(Long containerStockId, int qty) {
        ContainerStock containerStock = containerStockRepository.findById(containerStockId);
        containerStock.unfreezeQty(qty);
        containerStockRepository.save(containerStock);

        SkuBatchStock skuBatchStock = skuBatchStockRepository.findById(containerStock.getSkuBatchStockId());
        skuBatchStock.unfreezeQty(qty);
        skuBatchStockRepository.save(skuBatchStock);
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearStock(List<ContainerStock> containerStocks) {
        Map<Long, Set<Long>> containerStockIdAndBatchIdMap = containerStocks.stream()
                .collect(Collectors.groupingBy(ContainerStock::getId, Collectors.mapping(ContainerStock::getSkuBatchStockId, Collectors.toSet())));
        containerStockRepository.clearContainerStockByIds(containerStockIdAndBatchIdMap.keySet());
        skuBatchStockRepository.clearSkuBatchStockByIds(containerStockIdAndBatchIdMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));
    }
}
