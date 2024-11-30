package org.openwes.wes.stock.infrastructure.repository.impl;

import org.openwes.wes.stock.domain.entity.StockAdjustmentOrder;
import org.openwes.wes.stock.domain.repository.StockAdjustmentRepository;
import org.openwes.wes.stock.infrastructure.persistence.mapper.StockAdjustmentDetailPORepository;
import org.openwes.wes.stock.infrastructure.persistence.mapper.StockAdjustmentOrderPORepository;
import org.openwes.wes.stock.infrastructure.persistence.po.StockAdjustmentDetailPO;
import org.openwes.wes.stock.infrastructure.persistence.po.StockAdjustmentOrderPO;
import org.openwes.wes.stock.infrastructure.persistence.transfer.StockAdjustmentPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockAdjustmentRepositoryImpl implements StockAdjustmentRepository {

    private final StockAdjustmentOrderPORepository stockAdjustmentOrderPORepository;
    private final StockAdjustmentDetailPORepository stockAdjustmentDetailPORepository;
    private final StockAdjustmentPOTransfer stockAdjustmentPOTransfer;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public StockAdjustmentOrder createOrderAndDetails(StockAdjustmentOrder stockAdjustmentOrder) {
        StockAdjustmentOrderPO stockAdjustmentOrderPO = stockAdjustmentPOTransfer.toPO(stockAdjustmentOrder);
        StockAdjustmentOrderPO saved = stockAdjustmentOrderPORepository.save(stockAdjustmentOrderPO);

        List<StockAdjustmentDetailPO> detailPOs = stockAdjustmentPOTransfer.toDetailPOs(stockAdjustmentOrder.getDetails());
        detailPOs.forEach(v -> v.setStockAdjustmentOrderId(saved.getId()));
        List<StockAdjustmentDetailPO> stockAdjustmentDetailPOS = stockAdjustmentDetailPORepository.saveAll(detailPOs);
        return stockAdjustmentPOTransfer.toDO(stockAdjustmentOrderPO, stockAdjustmentDetailPOS);
    }

    @Override
    public List<StockAdjustmentOrder> findByIds(List<Long> ids) {

        List<StockAdjustmentOrderPO> stockAdjustmentOrderPOS = stockAdjustmentOrderPORepository.findAllById(ids);
        return stockAdjustmentOrderPOS.stream().map(stockAdjustmentOrderPO -> {
            List<StockAdjustmentDetailPO> detailPOS = stockAdjustmentDetailPORepository
                    .findAllByStockAdjustmentOrderId(stockAdjustmentOrderPO.getId());
            return stockAdjustmentPOTransfer.toDO(stockAdjustmentOrderPO, detailPOS);
        }).toList();

    }

    @Override
    public void saveOrders(List<StockAdjustmentOrder> stockAdjustmentOrders) {
        List<StockAdjustmentOrderPO> stockAdjustmentOrderPOS = stockAdjustmentPOTransfer.toPOs(stockAdjustmentOrders);
        stockAdjustmentOrderPORepository.saveAll(stockAdjustmentOrderPOS);
    }

}
