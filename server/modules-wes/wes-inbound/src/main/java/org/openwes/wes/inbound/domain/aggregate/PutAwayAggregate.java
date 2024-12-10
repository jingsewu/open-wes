package org.openwes.wes.inbound.domain.aggregate;

import com.google.common.collect.Lists;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.api.stock.dto.StockCreateDTO;
import org.openwes.wes.api.stock.event.StockCreateEvent;
import org.openwes.wes.common.facade.ContainerTaskApiFacade;
import org.openwes.wes.inbound.domain.entity.PutAwayTask;
import org.openwes.wes.inbound.domain.entity.PutAwayTaskDetail;
import org.openwes.wes.inbound.domain.repository.PutAwayTaskRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PutAwayAggregate {

    private final PutAwayTaskRepository putAwayTaskRepository;
    private final ContainerTaskApiFacade containerTaskApiFacade;

    @Transactional(rollbackFor = Exception.class)
    public void createPutAwayTasks(List<PutAwayTask> putAwayTasks) {
        List<PutAwayTask> savedPutAwayTasks = putAwayTaskRepository.saveAllOrdersAndDetails(putAwayTasks);

        List<CreateContainerTaskDTO> createContainerTaskDTOS = Lists.newArrayList();
        savedPutAwayTasks.forEach(putAwayTask -> putAwayTask.getPutAwayTaskDetails()
                .stream().collect(Collectors.groupingBy(PutAwayTaskDetail::getContainerFace))
                .forEach((containerFace, putAwayTaskDetails) -> {
                    CreateContainerTaskDTO createContainerTaskDTO = new CreateContainerTaskDTO();
                    createContainerTaskDTO.setTaskPriority(0);
                    createContainerTaskDTO.setTaskGroupPriority(0);
                    createContainerTaskDTO.setCustomerTaskId(putAwayTask.getId());
                    createContainerTaskDTO.setBusinessTaskType(BusinessTaskTypeEnum.PUT_AWAY);
                    createContainerTaskDTO.setContainerTaskType(ContainerTaskTypeEnum.INBOUND);
                    createContainerTaskDTO.setTaskGroupCode(StringUtils.EMPTY);
                    createContainerTaskDTO.setContainerCode(putAwayTask.getContainerCode());
                    createContainerTaskDTO.setContainerFace(containerFace);
                    createContainerTaskDTO.setContainerSpecCode(putAwayTask.getContainerSpecCode());
                    createContainerTaskDTOS.add(createContainerTaskDTO);
                }));
        containerTaskApiFacade.createContainerTasks(createContainerTaskDTOS);

        savedPutAwayTasks.forEach(putAwayTask -> putAwayTask.getPutAwayTaskDetails().forEach(detail -> {

            StockCreateDTO stockCreateDTO = StockCreateDTO.builder()
                    .boxStock(StringUtils.isNotEmpty(detail.getBoxNo()))
                    .orderNo(putAwayTask.getTaskNo())
                    .skuBatchAttributeId(detail.getSkuBatchAttributeId())
                    .skuId(detail.getSkuId())
                    .boxNo(detail.getBoxNo())
                    .sourceContainerCode(putAwayTask.getTaskNo())
                    .sourceContainerSlotCode("")
                    .targetContainerId(detail.getContainerId())
                    .targetContainerCode(detail.getContainerCode())
                    .targetContainerSlotCode(detail.getContainerSlotCode())
                    .targetContainerFace(detail.getContainerFace())
                    .transferQty(detail.getQtyPutAway())
                    .warehouseCode(putAwayTask.getWarehouseCode())
                    .warehouseAreaId(putAwayTask.getWarehouseAreaId())
                    .build();

            DomainEventPublisher.sendAsyncDomainEvent(StockCreateEvent.builder().stockCreateDTO(stockCreateDTO).build());
        }));
    }
}
