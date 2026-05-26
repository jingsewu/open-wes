package org.openwes.wes.outbound.application.usecase;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.api.outbound.constants.EmptyContainerOutboundOrderStatusEnum;
import org.openwes.wes.common.facade.ContainerTaskApiFacade;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrder;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrderDetail;
import org.openwes.wes.outbound.domain.repository.EmptyContainerOutboundOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmptyContainerOutboundUseCase {

    private final EmptyContainerOutboundOrderRepository repository;
    private final IContainerApi containerApi;
    private final ContainerTaskApiFacade containerTaskApiFacade;

    @Transactional(rollbackFor = Exception.class)
    public void create(EmptyContainerOutboundOrder order, Set<String> containerCodes) {
        containerApi.lockContainer(order.getWarehouseCode(), containerCodes);
        repository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void execute(List<EmptyContainerOutboundOrder> orders) {
        orders.forEach(EmptyContainerOutboundOrder::execute);
        repository.saveAll(orders);

        List<CreateContainerTaskDTO> tasks = orders.stream()
                .flatMap(order -> order.getDetails().stream().map(detail -> {
                    CreateContainerTaskDTO task = new CreateContainerTaskDTO();
                    task.setCustomerTaskId(detail.getId());
                    task.setContainerTaskType(ContainerTaskTypeEnum.OUTBOUND);
                    task.setBusinessTaskType(BusinessTaskTypeEnum.EMPTY_CONTAINER_OUTBOUND);
                    task.setContainerCode(detail.getContainerCode());
                    task.setDestinations(Lists.newArrayList(String.valueOf(order.getWorkStationId())));
                    task.setTaskGroupCode(order.getOrderNo());
                    task.setTaskPriority(0);
                    task.setTaskGroupPriority(0);
                    return task;
                })).toList();

        containerTaskApiFacade.createContainerTasks(tasks);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(List<EmptyContainerOutboundOrder> orders) {
        List<EmptyContainerOutboundOrderDetail> pendingDetails = orders.stream()
                .filter(v -> v.getEmptyContainerOutboundStatus() == EmptyContainerOutboundOrderStatusEnum.PENDING)
                .flatMap(order -> order.getDetails().stream().filter(detail -> !detail.isCompleted()))
                .toList();

        if (ObjectUtils.isNotEmpty(pendingDetails)) {
            containerTaskApiFacade.cancelTasks(
                    pendingDetails.stream().map(EmptyContainerOutboundOrderDetail::getId).toList());
        }

        orders.forEach(EmptyContainerOutboundOrder::cancel);
        repository.saveAll(orders);
    }
}
