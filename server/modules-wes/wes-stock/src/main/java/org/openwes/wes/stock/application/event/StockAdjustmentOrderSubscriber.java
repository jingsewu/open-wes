package org.openwes.wes.stock.application.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.config.ISystemConfigApi;
import org.openwes.wes.api.config.dto.SystemConfigDTO;
import org.openwes.wes.api.stock.IStockAdjustmentApi;
import org.openwes.wes.api.stock.event.StockAdjustmentOrderCreatedEvent;
import org.openwes.wes.stock.domain.entity.StockAdjustmentOrder;
import org.openwes.wes.stock.domain.repository.StockAdjustmentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockAdjustmentOrderSubscriber {

    private final IStockAdjustmentApi stockAdjustmentApi;
    private final ISystemConfigApi systemConfigApi;
    private final StockAdjustmentRepository stockAdjustmentRepository;

    @Subscribe
    public void onCreatedEvent(@Valid StockAdjustmentOrderCreatedEvent event) {
        SystemConfigDTO.StockConfigDTO stockConfig = systemConfigApi.getStockConfig();

        if (!stockConfig.isAdjustmentOrderAutoCompleteAdjustment()) {
            return;
        }

        StockAdjustmentOrder stockAdjustmentOrder = stockAdjustmentRepository.findByOrderNo(event.getOrderNo());
        stockAdjustmentApi.adjust(Lists.newArrayList(stockAdjustmentOrder.getId()));
    }
}
