package org.openwes.wes.stock.application.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.config.ISystemConfigApi;
import org.openwes.wes.api.config.dto.SystemConfigDTO;
import org.openwes.wes.api.stock.IStockAdjustmentApi;
import org.openwes.wes.api.stock.event.StockAbnormalRecordCreatedEvent;
import org.openwes.wes.api.stock.event.StockAdjustmentOrderCreatedEvent;
import org.openwes.wes.stock.domain.aggregate.StockAbnormalAggregate;
import org.openwes.wes.stock.domain.entity.StockAbnormalRecord;
import org.openwes.wes.stock.domain.entity.StockAdjustmentOrder;
import org.openwes.wes.stock.domain.repository.StockAbnormalRecordRepository;
import org.openwes.wes.stock.domain.repository.StockAdjustmentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockAdjustmentOrderSubscriber {

    private final IStockAdjustmentApi stockAdjustmentApi;
    private final ISystemConfigApi systemConfigApi;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StockAbnormalRecordRepository stockAbnormalRecordRepository;
    private final StockAbnormalAggregate stockAbnormalAggregate;

    @Subscribe
    public void onStockAbnormalRecordCreatedEvent(@Valid StockAbnormalRecordCreatedEvent event) {

        SystemConfigDTO.StockConfigDTO stockConfig = systemConfigApi.getStockConfig();
        if (!stockConfig.isStockAbnormalAutoCreateAdjustmentOrder()) {
            return;
        }
        StockAbnormalRecord stockAbnormalRecord = stockAbnormalRecordRepository.findById(event.getAggregatorId());
        stockAbnormalAggregate.createAdjustmentOrder(Lists.newArrayList(stockAbnormalRecord));
    }

    @Subscribe
    public void onStockAdjustmentOrderCreatedEvent(@Valid StockAdjustmentOrderCreatedEvent event) {
        SystemConfigDTO.StockConfigDTO stockConfig = systemConfigApi.getStockConfig();

        if (!stockConfig.isAdjustmentOrderAutoCompleteAdjustment()) {
            return;
        }

        StockAdjustmentOrder stockAdjustmentOrder = stockAdjustmentRepository.findById(event.getAggregatorId());
        stockAdjustmentApi.adjust(Lists.newArrayList(stockAdjustmentOrder.getId()));
    }
}
