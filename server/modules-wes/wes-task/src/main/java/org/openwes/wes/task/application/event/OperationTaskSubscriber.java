package org.openwes.wes.task.application.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.api.dto.callback.wms.ContainerSealedDTO;
import org.openwes.api.platform.api.dto.callback.wms.ContainerSealedDetailDTO;
import org.openwes.wes.api.basic.ITransferContainerApi;
import org.openwes.wes.api.basic.ITransferContainerRecordApi;
import org.openwes.wes.api.basic.dto.TransferContainerRecordDTO;
import org.openwes.wes.api.ems.proxy.IContainerTaskApi;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.openwes.wes.api.outbound.IPickingOrderApi;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.outbound.event.PickingOrderImprovePriorityEvent;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.dto.TransferContainerDTO;
import org.openwes.wes.api.task.event.TransferContainerSealedEvent;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.task.domain.entity.OperationTask;
import org.openwes.wes.task.domain.repository.OperationTaskRepository;
import org.openwes.wes.task.domain.transfer.OperationTaskTransfer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationTaskSubscriber {

    private final OperationTaskRepository operationTaskRepository;
    private final OperationTaskTransfer operationTaskTransfer;

    private final IPickingOrderApi pickingOrderApi;
    private final IOutboundPlanOrderApi outboundPlanOrderApi;
    private final ISkuMainDataApi skuMainDataApi;
    private final CallbackApiFacade callbackApiFacade;
    private final ITransferContainerRecordApi transferContainerRecordApi;
    private final ITransferContainerApi transferContainerApi;
    private final IContainerTaskApi containerTaskApi;

    @Subscribe
    public void onImprovePriority(PickingOrderImprovePriorityEvent event) {
        List<OperationTask> operationTasks = operationTaskRepository
                .findAllByPickingOrderIds(Lists.newArrayList(event.getPickingOrderId()))
                .stream().filter(v->v.getTaskStatus() == OperationTaskStatusEnum.NEW)
                .toList();

        if(operationTasks.isEmpty()){
            return;
        }

        operationTasks.forEach(operationTask -> operationTask.improvePriority(event.getPriority()));
        operationTaskRepository.saveAll(operationTasks);

        containerTaskApi.improvePriority(operationTasks.stream().map(OperationTask::getId).toList(),event.getPriority());
    }

    @Subscribe
    public void onTransferContainerSealed(TransferContainerSealedEvent transferContainerSealedEvent) {

        TransferContainerRecordDTO transferContainerRecord = transferContainerRecordApi.findById(transferContainerSealedEvent.getTransferContainerRecordId());
        TransferContainerDTO transferContainer = transferContainerApi
                .findByContainerCodeAndWarehouseCode(transferContainerRecord.getTransferContainerCode(), transferContainerRecord.getWarehouseCode());

        List<Long> currentPeriodRelateRecordIds = transferContainer.getCurrentPeriodRelateRecordIds();
        List<TransferContainerRecordDTO> transferContainerRecords = transferContainerRecordApi.findAllById(currentPeriodRelateRecordIds);

        ContainerSealedDTO containerSealedDTO = buildContainerSealedDetails(transferContainerRecord, transferContainerRecords);

        callbackApiFacade.callback(CallbackApiTypeEnum.OUTBOUND_SEAL_CONTAINER, "", containerSealedDTO);
    }

    private ContainerSealedDTO buildContainerSealedDetails(TransferContainerRecordDTO transferContainerRecord, List<TransferContainerRecordDTO> transferContainerRecords) {

        ContainerSealedDTO containerSealedDTO = new ContainerSealedDTO();
        containerSealedDTO.setTransferContainerCode(transferContainerRecord.getTransferContainerCode());
        containerSealedDTO.setWarehouseCode(transferContainerRecord.getWarehouseCode());

        List<OperationTask> operationTasks = operationTaskRepository.findAllByTransferContainerRecordIds(transferContainerRecords.stream().map(TransferContainerRecordDTO::getId).toList());

        if (CollectionUtils.isEmpty(operationTasks)) {
            log.error("transfer container record: {} and container: {} contains no operation tasks",
                    transferContainerRecord.getId(), transferContainerRecord.getTransferContainerCode());
            return null;
        }
        Set<Long> pickingOrderIds = operationTasks.stream().map(OperationTask::getOrderId).collect(Collectors.toSet());
        Set<Long> pickingOrderDetailIds = operationTasks.stream().map(OperationTask::getDetailId).collect(Collectors.toSet());
        List<PickingOrderDTO> pickingOrderDTOs = pickingOrderApi.getOrderAndDetailByPickingOrderIdsAndDetailIds(pickingOrderIds, pickingOrderDetailIds);

        Map<Long, PickingOrderDTO.PickingOrderDetailDTO> pickingOrderDetailDTOMap = pickingOrderDTOs.stream().flatMap(v -> v.getDetails().stream())
                .collect(Collectors.toMap(PickingOrderDTO.PickingOrderDetailDTO::getId, Function.identity()));

        Map<Long, PickingOrderDTO> pickingOrderDTOMap = pickingOrderDTOs.stream().collect(Collectors.toMap(PickingOrderDTO::getId, v -> v));

        Set<Long> outboundPlanOrderIds = pickingOrderDetailDTOMap.values().stream()
                .map(PickingOrderDTO.PickingOrderDetailDTO::getOutboundOrderPlanId).collect(Collectors.toSet());
        Map<Long, OutboundPlanOrderDTO> outboundPlanOrderDTOMap = outboundPlanOrderApi.getByIds(outboundPlanOrderIds).stream()
                .collect(Collectors.toMap(OutboundPlanOrderDTO::getId, Function.identity()));

        Set<Long> skuIds = operationTasks.stream().map(OperationTask::getSkuId).collect(Collectors.toSet());
        Map<Long, SkuMainDataDTO> skuMainDataDTOMap = skuMainDataApi.getByIds(skuIds).stream().collect(Collectors.toMap(SkuMainDataDTO::getId, Function.identity()));

        List<ContainerSealedDetailDTO> containerSealedDetailDTOS = operationTasks.stream()
                .filter(v -> pickingOrderDetailDTOMap.containsKey(v.getDetailId()))
                .map(task -> {
                    PickingOrderDTO.PickingOrderDetailDTO pickingOrderDetailDTO = pickingOrderDetailDTOMap.get(task.getDetailId());
                    PickingOrderDTO pickingOrderDTO = pickingOrderDTOMap.get(task.getOrderId());
                    OutboundPlanOrderDTO outboundPlanOrderDTO = outboundPlanOrderDTOMap.get(pickingOrderDetailDTO.getOutboundOrderPlanId());
                    SkuMainDataDTO skuMainDataDTO = skuMainDataDTOMap.get(task.getSkuId());

                    return operationTaskTransfer.toContainerSealedDetailDTO(task, pickingOrderDTO, pickingOrderDetailDTO, outboundPlanOrderDTO, skuMainDataDTO);
                }).toList();

        containerSealedDTO.setContainerSealedDetailDTOS(containerSealedDetailDTOS);

        return containerSealedDTO;
    }

}
