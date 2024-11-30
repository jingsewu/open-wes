package org.openwes.wes.inbound.domain.aggregate;

import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.basic.dto.CreateEmptyContainerDTO;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.common.facade.ContainerTaskApiFacade;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrder;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrderDetail;
import org.openwes.wes.inbound.domain.repository.EmptyContainerInboundRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmptyContainerInboundAggregate {

    private final IContainerApi containerApi;
    private final ContainerTaskApiFacade containerTaskApiFacade;
    private final EmptyContainerInboundRepository emptyContainerInboundRepository;

    @Transactional(rollbackFor = Exception.class)
    public void create(EmptyContainerInboundOrder emptyContainerInboundOrder,
                       List<ContainerDTO> existContainerDTOS) {
        String warehouseCode = emptyContainerInboundOrder.getWarehouseCode();

        List<EmptyContainerInboundOrderDetail> savedDetails = emptyContainerInboundRepository.saveOrderAndDetails(emptyContainerInboundOrder);

        // 创建容器或更新容器规格
        createOrUpdateContainer(warehouseCode, savedDetails, existContainerDTOS);

        // 调用 ems 创建容器入库任务
        createContainerInboundTask(emptyContainerInboundOrder, savedDetails);
    }

    private void createOrUpdateContainer(String warehouseCode,
                                         List<EmptyContainerInboundOrderDetail> details,
                                         List<ContainerDTO> existContainerDTOS) {
        Map<String, ContainerDTO> existContainerMap = existContainerDTOS.stream()
                .collect(Collectors.toMap(ContainerDTO::getContainerCode, dto -> dto));

        List<CreateEmptyContainerDTO> createEmptyContainerDTOS = new ArrayList<>();
        List<ContainerDTO> changeSpecContainerDTOS = new ArrayList<>();
        for (EmptyContainerInboundOrderDetail detail : details) {
            String containerCode = detail.getContainerCode();

            // 已存在的容器
            if (existContainerMap.containsKey(containerCode)) {
                ContainerDTO containerDTO = existContainerMap.get(containerCode);

                // 已存在的容器的规格与新传入的规格不一致，就需要修改容器的规格
                if (StringUtils.equals(detail.getContainerSpecCode(), containerDTO.getContainerSpecCode())) {
                    // 修改料箱的规格
                    containerDTO.setContainerSpecCode(detail.getContainerSpecCode());
                    changeSpecContainerDTOS.add(containerDTO);
                }
            } else {
                createEmptyContainerDTOS.add(new CreateEmptyContainerDTO(detail.getContainerCode(), detail.getContainerSpecCode()));
            }
        }

        // 新容器
        containerApi.createContainer(warehouseCode, createEmptyContainerDTOS);

        // 已存在的容器调整规格
        for (ContainerDTO dto : changeSpecContainerDTOS) {
            String containerCode = dto.getContainerCode();
            String containerSpecCode = dto.getContainerSpecCode();
            containerApi.changeContainerSpec(warehouseCode, containerCode, containerSpecCode);
        }
    }

    private void createContainerInboundTask(EmptyContainerInboundOrder emptyContainerInboundOrder,
                                            List<EmptyContainerInboundOrderDetail> details) {
        List<CreateContainerTaskDTO> createContainerTaskDTOS = details.stream().map(detail -> {
            CreateContainerTaskDTO task = new CreateContainerTaskDTO();

            task.setCustomerTaskId(detail.getId());
            task.setContainerTaskType(ContainerTaskTypeEnum.INBOUND);
            task.setBusinessTaskType(BusinessTaskTypeEnum.EMPTY_CONTAINER_INBOUND);
            task.setContainerCode(detail.getContainerCode());
            task.setContainerSpecCode(detail.getContainerSpecCode());

            task.setStartLocation(detail.getLocationCode());
            task.setDestinations(new ArrayList<>());

            task.setTaskGroupCode(emptyContainerInboundOrder.getOrderNo());
            task.setTaskPriority(0);
            task.setTaskGroupPriority(0);

            return task;
        }).toList();

        // 调用 ems，并返回容器号与任务编码的映射关系
        containerTaskApiFacade.createContainerTasks(createContainerTaskDTOS);
    }
}
