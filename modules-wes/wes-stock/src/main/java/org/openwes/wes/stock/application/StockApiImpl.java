package org.openwes.wes.stock.application;

import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.stock.dto.ContainerStockLockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockLockDTO;
import org.openwes.wes.stock.domain.aggregate.SkuBatchContainerStockAggregate;
import org.openwes.wes.stock.domain.entity.ContainerStock;
import org.openwes.wes.stock.domain.entity.SkuBatchStock;
import org.openwes.wes.stock.domain.repository.ContainerStockRepository;
import org.openwes.wes.stock.domain.repository.SkuBatchStockRepository;
import org.openwes.wes.stock.domain.transfer.ContainerStockTransfer;
import org.openwes.wes.stock.domain.transfer.SkuBatchStockTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@DubboService
@Service
@RequiredArgsConstructor
@Validated
public class StockApiImpl implements IStockApi {

    private final ContainerStockRepository containerStockRepository;
    private final ContainerStockTransfer containerStockTransfer;
    private final SkuBatchStockRepository skuBatchStockRepository;
    private final SkuBatchStockTransfer skuBatchStockTransfer;
    private final SkuBatchContainerStockAggregate skuBatchContainerStockAggregate;

    @Override
    public void addAndLockSkuBatchStock(List<SkuBatchStockLockDTO> skuBatchStockLockDTOS) {
        List<Long> skuBatchStockIds = skuBatchStockLockDTOS.stream().map(SkuBatchStockLockDTO::getSkuBatchStockId).toList();
        List<SkuBatchStock> skuBatchStocks = skuBatchStockRepository.findAllByIds(skuBatchStockIds);

        skuBatchStocks.forEach(skuBatchStock -> skuBatchStockLockDTOS.forEach(skuBatchStockLockDTO -> {
            if (Objects.equals(skuBatchStock.getId(), skuBatchStockLockDTO.getSkuBatchStockId())) {
                skuBatchStock.addAndLockQty(skuBatchStockLockDTO.getLockQty(), skuBatchStockLockDTO.getLockType());
            }
        }));
        skuBatchStockRepository.saveAll(skuBatchStocks);
    }

    @Override
    public void lockSkuBatchStock(List<SkuBatchStockLockDTO> skuBatchStockLockDTOS) {
        List<Long> skuBatchStockIds = skuBatchStockLockDTOS.stream().map(SkuBatchStockLockDTO::getSkuBatchStockId).toList();
        List<SkuBatchStock> skuBatchStocks = skuBatchStockRepository.findAllByIds(skuBatchStockIds);

        skuBatchStocks.forEach(skuBatchStock -> skuBatchStockLockDTOS.forEach(skuBatchStockLockDTO -> {
            if (Objects.equals(skuBatchStock.getId(), skuBatchStockLockDTO.getSkuBatchStockId())) {
                skuBatchStock.lockQty(skuBatchStockLockDTO.getLockQty(), skuBatchStockLockDTO.getLockType());
            }
        }));
        skuBatchStockRepository.saveAll(skuBatchStocks);
    }

    @Override
    public void lockContainerStock(List<ContainerStockLockDTO> containerStockLockDTOS) {
        List<Long> containerStockIds = containerStockLockDTOS.stream().map(ContainerStockLockDTO::getContainerStockId).toList();
        List<ContainerStock> containerStocks = containerStockRepository.findAllByIds(containerStockIds);

        containerStocks.forEach(containerStock -> containerStockLockDTOS.forEach(containerStockLockDTO -> {
            if (Objects.equals(containerStock.getId(), containerStockLockDTO.getContainerStockId())) {
                containerStock.lockQty(containerStockLockDTO.getLockQty(), containerStockLockDTO.getLockType());
            }
        }));
        containerStockRepository.saveAll(containerStocks);
    }

    @Override
    public void addAndLockContainerStock(List<ContainerStockLockDTO> containerStockLockDTOS) {
        List<Long> containerStockIds = containerStockLockDTOS.stream().map(ContainerStockLockDTO::getContainerStockId).toList();
        List<ContainerStock> containerStocks = containerStockRepository.findAllByIds(containerStockIds);

        containerStocks.forEach(containerStock -> containerStockLockDTOS.forEach(containerStockLockDTO -> {
            if (Objects.equals(containerStock.getId(), containerStockLockDTO.getContainerStockId())) {
                containerStock.addAndLockQty(containerStockLockDTO.getLockQty(), containerStockLockDTO.getLockType());
            }
        }));
        containerStockRepository.saveAll(containerStocks);
    }

    @Override
    public void freezeContainerStock(Long id, int qty) {
        skuBatchContainerStockAggregate.freezeStock(id, qty);
    }

    @Override
    public void unfreezeContainerStock(Long id, int qty) {
        skuBatchContainerStockAggregate.unfreezeStock(id, qty);
    }

    @Override
    public List<SkuBatchStockDTO> getBySkuBatchAttributeIds(Collection<Long> skuBatchAttributeIds) {
        List<SkuBatchStock> skuBatchStocks = skuBatchStockRepository.findAllBySkuBatchAttributeIds(skuBatchAttributeIds);
        return skuBatchStockTransfer.toDTOs(skuBatchStocks);
    }

    @Override
    public List<SkuBatchStockDTO> getBySkuBatchAttributeId(Long skuBatchAttributeId) {
        List<SkuBatchStock> skuBatchStocks = skuBatchStockRepository.findAllBySkuBatchAttributeId(skuBatchAttributeId);
        return skuBatchStockTransfer.toDTOs(skuBatchStocks);
    }

    @Override
    public List<ContainerStockDTO> getContainerStockBySkuBatchStockIds(Collection<Long> skuBatchStockIds) {
        return containerStockTransfer.toDTOs(containerStockRepository.findAllBySkuBatchStockIds(skuBatchStockIds));
    }

    @Override
    public List<ContainerStockDTO> getContainerStocks(Collection<String> containerCodes, String warehouseCode) {
        return containerStockTransfer.toDTOs(containerStockRepository.findAllByContainerCodesAndWarehouseCode(containerCodes, warehouseCode));
    }

    @Override
    public List<ContainerStockDTO> getContainerStocks(List<Long> containerStockIds) {
        List<ContainerStock> containerStocks = containerStockRepository.findAllByIds(containerStockIds);
        return containerStockTransfer.toDTOs(containerStocks);
    }

    @Override
    public SkuBatchStockDTO getSkuBatchStock(Long skuBatchStockId) {
        SkuBatchStock skuBatchStock = skuBatchStockRepository.findById(skuBatchStockId);
        return skuBatchStockTransfer.toDTO(skuBatchStock);
    }

    @Override
    public List<SkuBatchStockDTO> getSkuBatchStocks(Collection<Long> skuBatchStockIds) {
        List<SkuBatchStock> skuBatchStocks = skuBatchStockRepository.findAllByIds(skuBatchStockIds);
        return skuBatchStockTransfer.toDTOs(skuBatchStocks);
    }

    @Override
    public List<ContainerStockDTO> getContainerStocksBySlotCode(String warehouseCode, String containerCode, String containerSlotCode) {
        return containerStockTransfer.toDTOs(containerStockRepository.findAllByContainerAndSlotCode(warehouseCode, containerCode, containerSlotCode));
    }

    @Override
    public List<ContainerStockDTO> getBySkuIds(List<Long> skuIds) {
        List<ContainerStock> containerStocks = containerStockRepository.findAllBySkuIds(skuIds);
        return containerStockTransfer.toDTOs(containerStocks);
    }

    @Override
    public List<ContainerStockDTO> getByContainerAndFace(String warehouseCode, String containerCode, String containerFace) {
        List<ContainerStock> containerStocks = containerStockRepository.findAllByContainerAndFaceAndWarehouseCode(containerCode, containerFace, warehouseCode);
        return containerStockTransfer.toDTOs(containerStocks);
    }

}
