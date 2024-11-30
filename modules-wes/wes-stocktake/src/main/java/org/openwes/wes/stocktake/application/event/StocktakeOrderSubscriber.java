package org.openwes.wes.stocktake.application.event;

import com.google.common.eventbus.Subscribe;
import org.openwes.wes.api.stocktake.event.StocktakeOrderCompleteEvent;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.repository.StocktakeOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StocktakeOrderSubscriber {

    private final StocktakeOrderRepository stocktakeorderRepository;

    @Subscribe
    public void onStocktakeTaskCompleted(StocktakeOrderCompleteEvent event) {
        StocktakeOrder stocktakeOrder = stocktakeorderRepository.findById(event.getStocktakeOrderId());
        stocktakeOrder.complete();
        stocktakeorderRepository.saveStocktakeOrder(stocktakeOrder);
    }
}
