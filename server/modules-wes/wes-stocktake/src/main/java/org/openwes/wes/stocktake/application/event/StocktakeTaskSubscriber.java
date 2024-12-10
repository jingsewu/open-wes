package org.openwes.wes.stocktake.application.event;

import com.google.common.eventbus.Subscribe;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.stocktake.constants.StocktakeRecordStatusEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeTaskStatusEnum;
import org.openwes.wes.api.stocktake.event.StocktakeOrderCompleteEvent;
import org.openwes.wes.api.stocktake.event.StocktakeRecordSubmitEvent;
import org.openwes.wes.api.stocktake.event.StocktakeTaskCloseEvent;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;
import org.openwes.wes.stocktake.domain.repository.StocktakeRecordRepository;
import org.openwes.wes.stocktake.domain.repository.StocktakeTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StocktakeTaskSubscriber {

    private final StocktakeTaskRepository stocktakeTaskRepository;
    private final StocktakeRecordRepository stocktakeRecordRepository;

    @Subscribe
    public void onRecordSubmit(StocktakeRecordSubmitEvent event) {

        List<StocktakeRecord> stocktakeRecords = stocktakeRecordRepository.findAllByTaskDetailId(event.getStocktakeTaskDetailId());
        boolean anyMatch = stocktakeRecords.stream().anyMatch(v -> v.getStocktakeRecordStatus() == StocktakeRecordStatusEnum.NEW);
        if (anyMatch) {
            return;
        }
        StocktakeTask stocktakeTask = stocktakeTaskRepository.findById(event.getStocktakeTaskId());
        stocktakeTask.submit(event.getStocktakeTaskDetailId());
        stocktakeTaskRepository.saveOrderAndDetail(stocktakeTask);

        List<StocktakeTask> stocktakeTasks = stocktakeTaskRepository.findAllByStocktakeOrderId(stocktakeTask.getStocktakeOrderId());

        boolean result = stocktakeTasks.stream().anyMatch(v -> v.getStocktakeTaskStatus() == StocktakeTaskStatusEnum.NEW
                || v.getStocktakeTaskStatus() == StocktakeTaskStatusEnum.STARTED);

        if (result) {
            return;
        }

        DomainEventPublisher.sendAsyncDomainEvent(new StocktakeOrderCompleteEvent().setStocktakeOrderId(stocktakeTask.getStocktakeOrderId()));
    }

    @Subscribe
    public void onClose(StocktakeTaskCloseEvent event) {
        List<StocktakeTask> stocktakeTasks = stocktakeTaskRepository.findAllByStocktakeOrderId(event.getStocktakeOrderId());

        boolean result = stocktakeTasks.stream().anyMatch(v -> v.getStocktakeTaskStatus() == StocktakeTaskStatusEnum.NEW
                || v.getStocktakeTaskStatus() == StocktakeTaskStatusEnum.STARTED);

        if (result) {
            return;
        }

        DomainEventPublisher.sendAsyncDomainEvent(new StocktakeOrderCompleteEvent().setStocktakeOrderId(event.getStocktakeOrderId()));
    }
}
