package org.openwes.wes.stock.application;

import org.openwes.wes.api.stock.IStockAdjustmentApi;
import org.openwes.wes.stock.domain.aggregate.StockAdjustmentAggregate;
import org.openwes.wes.stock.domain.entity.StockAdjustmentOrder;
import org.openwes.wes.stock.domain.repository.StockAdjustmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockAdjustmentApiImpl implements IStockAdjustmentApi {

    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StockAdjustmentAggregate stockAdjustmentAggregate;

    @Override
    public void adjust(List<Long> ids) {
        List<StockAdjustmentOrder> stockAdjustmentOrders = stockAdjustmentRepository.findByIds(ids);
        stockAdjustmentAggregate.adjust(stockAdjustmentOrders);
    }

    @Override
    public void close(List<Long> ids) {
        List<StockAdjustmentOrder> stockAdjustmentOrders = stockAdjustmentRepository.findByIds(ids);
        stockAdjustmentAggregate.close(stockAdjustmentOrders);
    }

}
