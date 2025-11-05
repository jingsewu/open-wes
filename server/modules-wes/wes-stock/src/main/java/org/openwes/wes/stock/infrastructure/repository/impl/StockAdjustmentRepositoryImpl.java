package org.openwes.wes.stock.infrastructure.repository.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.domain.event.AggregatorRoot;
import org.openwes.wes.stock.domain.entity.StockAdjustmentOrder;
import org.openwes.wes.stock.domain.repository.StockAdjustmentRepository;
import org.openwes.wes.stock.infrastructure.persistence.mapper.StockAdjustmentDetailPORepository;
import org.openwes.wes.stock.infrastructure.persistence.mapper.StockAdjustmentOrderPORepository;
import org.openwes.wes.stock.infrastructure.persistence.po.StockAdjustmentDetailPO;
import org.openwes.wes.stock.infrastructure.persistence.po.StockAdjustmentOrderPO;
import org.openwes.wes.stock.infrastructure.persistence.transfer.StockAdjustmentPOTransfer;
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
    public StockAdjustmentOrder saveOrderAndDetails(StockAdjustmentOrder stockAdjustmentOrder) {

        stockAdjustmentOrder.sendAndClearEvents();

        StockAdjustmentOrderPO stockAdjustmentOrderPO = stockAdjustmentPOTransfer.toPO(stockAdjustmentOrder);
        StockAdjustmentOrderPO saved = stockAdjustmentOrderPORepository.save(stockAdjustmentOrderPO);

        List<StockAdjustmentDetailPO> detailPOs = stockAdjustmentPOTransfer.toDetailPOs(stockAdjustmentOrder.getDetails());
        detailPOs.forEach(v -> v.setStockAdjustmentOrderId(saved.getId()));
        List<StockAdjustmentDetailPO> stockAdjustmentDetailPOS = stockAdjustmentDetailPORepository.saveAll(detailPOs);
        return stockAdjustmentPOTransfer.toDO(stockAdjustmentOrderPO, stockAdjustmentDetailPOS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrders(List<StockAdjustmentOrder> stockAdjustmentOrders) {

        stockAdjustmentOrders.forEach(AggregatorRoot::sendAndClearEvents);

        List<StockAdjustmentOrderPO> stockAdjustmentOrderPOS = stockAdjustmentPOTransfer.toPOs(stockAdjustmentOrders);
        stockAdjustmentOrderPORepository.saveAll(stockAdjustmentOrderPOS);
    }

    @Override
    public StockAdjustmentOrder findById(Long id) {
        return stockAdjustmentPOTransfer.toDO(stockAdjustmentOrderPORepository.findById(id).orElseThrow());
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
    public StockAdjustmentOrder findByOrderNo(String orderNo) {
        StockAdjustmentOrderPO stockAdjustmentOrderPO = stockAdjustmentOrderPORepository.findByOrderNo(orderNo);
        return stockAdjustmentPOTransfer.toDO(stockAdjustmentOrderPO);
    }

}
