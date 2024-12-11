package org.openwes.wes.stock.application.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.config.ISystemConfigApi;
import org.openwes.wes.api.config.dto.SystemConfigDTO;
import org.openwes.wes.api.stock.IStockAbnormalRecordApi;
import org.openwes.wes.api.stock.event.StockAbnormalRecordCreatedEvent;
import org.openwes.wes.stock.domain.entity.StockAbnormalRecord;
import org.openwes.wes.stock.domain.repository.StockAbnormalRecordRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockAbnormalRecordSubscriber {

    private final IStockAbnormalRecordApi stockAbnormalRecordApi;
    private final ISystemConfigApi systemConfigApi;
    private final StockAbnormalRecordRepository stockAbnormalRecordRepository;

    @Subscribe
    public void onCreatedEvent(@Valid StockAbnormalRecordCreatedEvent event) {

        SystemConfigDTO.StockConfigDTO stockConfig = systemConfigApi.getStockConfig();
        if (!stockConfig.isStockAbnormalAutoCreateAdjustmentOrder()) {
            return;
        }
        StockAbnormalRecord stockAbnormalRecord = stockAbnormalRecordRepository.findByOrderNo(event.getOrderNo());
        stockAbnormalRecordApi.createAdjustmentOrder(Lists.newArrayList(stockAbnormalRecord.getId()));
    }
}
