package org.openwes.wes.inbound.application.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.wes.api.inbound.event.PutAwayCreationEvent;
import org.openwes.wes.api.inbound.event.PutAwayTaskCompletionEvent;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.inbound.domain.aggregate.PutAwayAggregate;
import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;
import org.openwes.wes.inbound.domain.entity.PutAwayTask;
import org.openwes.wes.inbound.domain.entity.PutAwayTaskDetail;
import org.openwes.wes.inbound.domain.repository.AcceptOrderRepository;
import org.openwes.wes.inbound.domain.repository.PutAwayTaskRepository;
import org.openwes.wes.inbound.domain.service.PutAwayTaskService;
import org.openwes.wes.inbound.domain.transfer.PutAwayTaskTransfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutAwayTaskSubscriber {

    private final AcceptOrderRepository acceptOrderRepository;
    private final PutAwayTaskTransfer putAwayTaskTransfer;
    private final PutAwayTaskRepository putAwayTaskRepository;
    private final PutAwayAggregate putAwayAggregate;
    private final PutAwayTaskService putAwayTaskService;
    private final CallbackApiFacade callbackApiFacade;

    @Subscribe
    public void onCreation(PutAwayCreationEvent putAwayEvent) {

        AcceptOrder acceptOrder = acceptOrderRepository.findById(putAwayEvent.getAcceptOrderId());

        List<PutAwayTask> putAwayTasks = Lists.newArrayList();
        acceptOrder.getDetails().stream().filter(v -> v.getQtyAccepted() > 0)
                .collect(Collectors.groupingBy(AcceptOrderDetail::getTargetContainerId))
                .forEach((containerCode, details) -> {
                    AcceptOrderDetail detail = details.iterator().next();

                    PutAwayTask putAwayTask = putAwayTaskTransfer.toDO(acceptOrder, detail);

                    List<PutAwayTaskDetail> putAwayTaskDetails = details.stream().map(putAwayTaskTransfer::toDetailDO).toList();
                    putAwayTask.setPutAwayTaskDetails(putAwayTaskDetails);

                    putAwayTask.initialize();

                    putAwayTasks.add(putAwayTask);
                });

        putAwayTaskService.calculateDirection(putAwayTasks);

        putAwayAggregate.createPutAwayTasks(putAwayTasks);
    }

    @Subscribe
    public void onCompletion(PutAwayTaskCompletionEvent event) {

        Map<Long, PutAwayTaskCompletionEvent.PutAwayTaskCompleteDetail> detailMap = event.getDetails()
                .stream().collect(Collectors.toMap(PutAwayTaskCompletionEvent.PutAwayTaskCompleteDetail::getPutAwayTaskId, v -> v, (v1, v2) -> v1));

        List<PutAwayTask> putAwayTasks = putAwayTaskRepository.findAllByIds(detailMap.keySet());

        putAwayTasks.forEach(putAwayTask -> {
            PutAwayTaskCompletionEvent.PutAwayTaskCompleteDetail putAwayTaskCompleteDetail = detailMap.get(putAwayTask.getId());
            putAwayTask.complete(putAwayTaskCompleteDetail.getLocationCode());
        });

        putAwayTaskRepository.saveAllOrders(putAwayTasks);
        callbackApiFacade.callback(CallbackApiTypeEnum.PUT_AWAY_TASK_COMPLETED, "", putAwayTasks);
    }

}
