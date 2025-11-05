package org.openwes.wes.inbound.application.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.inbound.event.AcceptOrderCompletionEvent;
import org.openwes.wes.inbound.domain.aggregate.PutAwayAggregate;
import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;
import org.openwes.wes.inbound.domain.entity.PutAwayTask;
import org.openwes.wes.inbound.domain.entity.PutAwayTaskDetail;
import org.openwes.wes.inbound.domain.repository.AcceptOrderRepository;
import org.openwes.wes.inbound.domain.service.PutAwayTaskService;
import org.openwes.wes.inbound.domain.transfer.PutAwayTaskTransfer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutAwayTaskSubscriber {

    private final AcceptOrderRepository acceptOrderRepository;
    private final PutAwayTaskTransfer putAwayTaskTransfer;
    private final PutAwayAggregate putAwayAggregate;
    private final PutAwayTaskService putAwayTaskService;

    @Subscribe
    public void onCreation(AcceptOrderCompletionEvent putAwayEvent) {

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

}
