package org.openwes.wes.stock.infrastructure.repository.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.stock.domain.entity.ContainerStock;
import org.openwes.wes.stock.domain.repository.ContainerStockRepository;
import org.openwes.wes.stock.infrastructure.persistence.mapper.ContainerStockPORepository;
import org.openwes.wes.stock.infrastructure.persistence.po.ContainerStockPO;
import org.openwes.wes.stock.infrastructure.persistence.transfer.ContainerStockPOTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContainerStockRepositoryImpl implements ContainerStockRepository {

    private final ContainerStockPORepository containerStockPORepository;
    private final ContainerStockPOTransfer containerStockPOTransfer;

    @Override
    public void save(ContainerStock containerStock) {
        containerStockPORepository.save(containerStockPOTransfer.toPO(containerStock));
    }

    @Override
    public void saveAll(List<ContainerStock> containerStocks) {
        containerStockPORepository.saveAll(containerStockPOTransfer.toPOs(containerStocks));
    }

    @Override
    public ContainerStock findById(Long stockId) {
        ContainerStockPO containerStockPO = containerStockPORepository.findById(stockId).orElseThrow();
        return containerStockPOTransfer.toDO(containerStockPO);
    }

    @Override
    public List<ContainerStock> findAllByIds(Collection<Long> containerStockIds) {
        List<ContainerStockPO> containerStockPOS = containerStockPORepository.findAllById(containerStockIds);
        return containerStockPOTransfer.toDOs(containerStockPOS);
    }

    @Override
    public ContainerStock findByContainerAndSlotAndSkuBatch(String containerCode, String containerSlotCode, Long skuBatchStockId) {
        ContainerStockPO containerStockPO = containerStockPORepository
                .findBySkuBatchStockIdAndContainerCodeAndContainerSlotCode(skuBatchStockId, containerCode, containerSlotCode);
        return containerStockPOTransfer.toDO(containerStockPO);
    }

    @Override
    public List<ContainerStock> findAllBySkuBatchStockIds(Collection<Long> skuBatchStockIds) {

        int limitSize = 500;
        List<ContainerStock> containerStocks = Lists.newArrayList();
        PageRequest pageRequest = PageRequest.of(0, limitSize);
        while (true) {
            Page<ContainerStockPO> containerStockPOs = containerStockPORepository.findAllBySkuBatchStockIdIn(skuBatchStockIds, pageRequest);
            containerStocks.addAll(containerStockPOTransfer.toDOs(containerStockPOs.getContent()));
            if (containerStockPOs.getContent().size() < limitSize) {
                break;
            }

            pageRequest = pageRequest.next();
        }

        return containerStocks;
    }

    @Override
    public List<ContainerStock> findAllByContainerCodesAndWarehouseCode(Collection<String> containerCodes, String warehouseCode) {
        return containerStockPOTransfer.toDOs(containerStockPORepository.findAllByContainerCodeInAndWarehouseCode(containerCodes, warehouseCode));
    }

    @Override
    public List<ContainerStock> findAllBySkuIds(Collection<Long> skuIds) {

        int limitSize = 500;
        List<ContainerStock> containerStocks = Lists.newArrayList();
        PageRequest pageRequest = PageRequest.of(0, limitSize);
        while (true) {
            Page<ContainerStockPO> containerStockPOs = containerStockPORepository.findAllBySkuIdIn(skuIds, pageRequest);
            containerStocks.addAll(containerStockPOTransfer.toDOs(containerStockPOs.getContent()));
            if (containerStockPOs.getContent().size() < limitSize) {
                break;
            }

            pageRequest = pageRequest.next();
        }

        return containerStocks;
    }

    @Override
    public List<ContainerStock> findAllBySkuId(Long skuId) {
        return containerStockPOTransfer.toDOs(containerStockPORepository.findAllBySkuId(skuId));
    }

    @Override
    public void clearContainerStockByIds(Set<Long> stockIds) {
        containerStockPORepository.deleteAllByIdInBatch(stockIds);
    }

    @Override
    public List<ContainerStock> findAllByContainerAndSlotCode(String warehouseCode, String containerCode, String containerSlotCode) {
        List<ContainerStockPO> containerStockPOS = containerStockPORepository.findAllByWarehouseCodeAndContainerCodeAndContainerSlotCode(warehouseCode, containerCode, containerSlotCode);
        return containerStockPOTransfer.toDOs(containerStockPOS);
    }

    @Override
    public List<ContainerStock> findAllByContainerAndFaceAndWarehouseCode(String containerCode, String containerFace, String warehouseCode) {
        List<ContainerStockPO> containerStockPOS = containerStockPORepository.findAllByWarehouseCodeAndContainerCodeAndContainerFace(warehouseCode, containerCode, containerFace);
        return containerStockPOTransfer.toDOs(containerStockPOS);
    }
}
