package org.openwes.wes.outbound.domain.aggregate;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.common.facade.ContainerTaskApiFacade;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrder;
import org.openwes.wes.outbound.domain.repository.EmptyContainerOutboundOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmptyContainerOutboundAggregate {

    private final EmptyContainerOutboundOrderRepository repository;
    private final IContainerApi containerApi;
    private final ContainerTaskApiFacade containerTaskApiFacade;

    @Transactional(rollbackFor = Exception.class)
    public void create(EmptyContainerOutboundOrder order, List<ContainerDTO> containerDTOs) {
        Set<String> containerCodes = containerDTOs.stream().map(ContainerDTO::getContainerCode).collect(Collectors.toSet());
        containerApi.lockContainer(order.getWarehouseCode(), containerCodes);
        repository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void execute(List<EmptyContainerOutboundOrder> emptyContainerOutboundOrders) {

        emptyContainerOutboundOrders.forEach(EmptyContainerOutboundOrder::execute);
        repository.saveAll(emptyContainerOutboundOrders);

        List<CreateContainerTaskDTO> createContainerTaskDTOS = emptyContainerOutboundOrders.stream().flatMap(emptyContainerOutboundOrder -> emptyContainerOutboundOrder.getDetails().stream().map(detail -> {
                    CreateContainerTaskDTO task = new CreateContainerTaskDTO();

                    task.setCustomerTaskId(detail.getId());
                    task.setContainerTaskType(ContainerTaskTypeEnum.OUTBOUND);
                    task.setBusinessTaskType(BusinessTaskTypeEnum.EMPTY_CONTAINER_INBOUND);
                    task.setContainerCode(detail.getContainerCode());

                    task.setDestinations(Lists.newArrayList(String.valueOf(emptyContainerOutboundOrder.getWorkStationId())));

                    task.setTaskGroupCode(emptyContainerOutboundOrder.getOrderNo());
                    task.setTaskPriority(0);
                    task.setTaskGroupPriority(0);
                    return task;
                }).toList().stream()
        ).toList();

        containerTaskApiFacade.createContainerTasks(createContainerTaskDTOS);
    }
}
