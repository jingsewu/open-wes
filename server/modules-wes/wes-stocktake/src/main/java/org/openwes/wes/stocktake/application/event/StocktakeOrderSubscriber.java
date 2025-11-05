package org.openwes.wes.stocktake.application.event;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.stocktake.constants.StocktakeTaskStatusEnum;
import org.openwes.wes.api.stocktake.event.StocktakeTaskCompletionEvent;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;
import org.openwes.wes.stocktake.domain.repository.StocktakeOrderRepository;
import org.openwes.wes.stocktake.domain.repository.StocktakeTaskRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StocktakeOrderSubscriber {

    private final StocktakeOrderRepository stocktakeorderRepository;
    private final StocktakeTaskRepository stocktakeTaskRepository;

    @Subscribe
    public void onStocktakeTaskCompleted(StocktakeTaskCompletionEvent event) {

        List<StocktakeTask> stocktakeTasks = stocktakeTaskRepository.findAllByStocktakeOrderId(event.getStocktakeOrderId());

        boolean result = stocktakeTasks.stream()
                .anyMatch(v -> v.getStocktakeTaskStatus() == StocktakeTaskStatusEnum.NEW
                        || v.getStocktakeTaskStatus() == StocktakeTaskStatusEnum.STARTED);

        if (result) {
            return;
        }

        StocktakeOrder stocktakeOrder = stocktakeorderRepository.findById(event.getStocktakeOrderId());
        stocktakeOrder.complete();
        stocktakeorderRepository.saveStocktakeOrder(stocktakeOrder);
    }
}
