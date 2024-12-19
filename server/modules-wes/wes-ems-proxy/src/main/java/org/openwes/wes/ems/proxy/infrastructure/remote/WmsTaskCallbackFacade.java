package org.openwes.wes.ems.proxy.infrastructure.remote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.inbound.event.EmptyContainerInboundOrderCompletionEvent;
import org.openwes.wes.api.inbound.event.PutAwayTaskCompletionEvent;
import org.openwes.wes.api.outbound.event.EmptyContainerOutboundOrderCompletionEvent;
import org.openwes.wes.api.task.event.TransferContainerArrivedEvent;
import org.openwes.wes.ems.proxy.domain.entity.ContainerTask;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class WmsTaskCallbackFacade {

    public void wmsTaskCallback(List<ContainerTask> containerTasks, BusinessTaskTypeEnum businessTaskType) {

        switch (businessTaskType) {
            case EMPTY_CONTAINER_INBOUND:
                List<Long> customerTaskIds = containerTasks.stream().filter(v -> Objects.nonNull(v.getCustomerTaskIds()))
                        .flatMap(v -> v.getCustomerTaskIds().stream()).toList();

                DomainEventPublisher.sendAsyncDomainEvent(new EmptyContainerInboundOrderCompletionEvent()
                        .setEmptyContainerInboundOrderDetailIds(customerTaskIds));
                break;
            case EMPTY_CONTAINER_OUTBOUND:
                List<Long> emptyContainerOutboundDetailIds = containerTasks.stream().filter(v -> Objects.nonNull(v.getCustomerTaskIds()))
                        .flatMap(v -> v.getCustomerTaskIds().stream()).toList();

                DomainEventPublisher.sendAsyncDomainEvent(new EmptyContainerOutboundOrderCompletionEvent()
                        .setEmptyContainerOutboundOrderDetailIds(emptyContainerOutboundDetailIds));
                break;

            case PUT_AWAY:
                List<PutAwayTaskCompletionEvent.PutAwayTaskCompleteDetail> putAwayTaskCompleteDetails = containerTasks.stream()
                        .flatMap(v -> v.getCustomerTaskIds().stream().map(taskId ->
                                new PutAwayTaskCompletionEvent.PutAwayTaskCompleteDetail().setPutAwayTaskId(taskId).setLocationCode(v.getFinalDestination())))
                        .toList();

                DomainEventPublisher.sendAsyncDomainEvent(new PutAwayTaskCompletionEvent().setDetails(putAwayTaskCompleteDetails));
                break;
            default:
                log.warn("Unknown business task type: {}", businessTaskType);
                break;
        }

    }

    public void transferContainerArrive(ContainerArrivedEvent containerArrivedEvent) {

        List<TransferContainerArrivedEvent.TransferContainerArriveDetail> details = containerArrivedEvent.getContainerDetails().stream().map(v ->
                new TransferContainerArrivedEvent.TransferContainerArriveDetail()
                        .setContainerCode(v.getContainerCode())
                        .setLocationCode(v.getLocationCode())).toList();

        DomainEventPublisher.sendAsyncDomainEvent(new TransferContainerArrivedEvent()
                .setWarehouseAreaId(containerArrivedEvent.getWarehouseAreaId()).setDetails(details));
    }
}
