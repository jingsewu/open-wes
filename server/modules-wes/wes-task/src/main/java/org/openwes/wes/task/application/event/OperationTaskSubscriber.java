package org.openwes.wes.task.application.event;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.api.dto.callback.wms.ContainerSealedDTO;
import org.openwes.api.platform.api.dto.callback.wms.ContainerSealedDetailDTO;
import org.openwes.wes.api.basic.ITransferContainerApi;
import org.openwes.wes.api.basic.ITransferContainerRecordApi;
import org.openwes.wes.api.basic.dto.TransferContainerRecordDTO;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.openwes.wes.api.outbound.IPickingOrderApi;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.task.ITaskApi;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.TransferContainerDTO;
import org.openwes.wes.api.task.event.TransferContainerSealedEvent;
import org.openwes.wes.common.facade.CallbackApiFacade;
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

    private final IPickingOrderApi pickingOrderApi;
    private final ITaskApi taskApi;
    private final IOutboundPlanOrderApi outboundPlanOrderApi;
    private final ISkuMainDataApi skuMainDataApi;
    private final CallbackApiFacade callbackApiFacade;
    private final ITransferContainerRecordApi transferContainerRecordApi;
    private final ITransferContainerApi transferContainerApi;

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

        List<OperationTaskDTO> operationTaskDTOS = taskApi.queryByTransferContainerRecordIds(transferContainerRecords.stream()
                .map(TransferContainerRecordDTO::getId).toList());
        if (CollectionUtils.isEmpty(operationTaskDTOS)) {
            log.error("transfer container record: {} and container: {} contains no operation tasks",
                    transferContainerRecord.getId(), transferContainerRecord.getTransferContainerCode());
            return null;
        }
        Set<Long> pickingOrderIds = operationTaskDTOS.stream().map(OperationTaskDTO::getOrderId).collect(Collectors.toSet());
        Set<Long> pickingOrderDetailIds = operationTaskDTOS.stream().map(OperationTaskDTO::getDetailId).collect(Collectors.toSet());
        List<PickingOrderDTO> pickingOrderDTOs = pickingOrderApi.getOrderAndDetailByPickingOrderIdsAndDetailIds(pickingOrderIds, pickingOrderDetailIds);

        Map<Long, PickingOrderDTO.PickingOrderDetailDTO> pickingOrderDetailDTOMap = pickingOrderDTOs.stream().flatMap(v -> v.getDetails().stream())
                .collect(Collectors.toMap(PickingOrderDTO.PickingOrderDetailDTO::getId, Function.identity()));

        Map<Long, PickingOrderDTO> pickingOrderDTOMap = pickingOrderDTOs.stream().collect(Collectors.toMap(PickingOrderDTO::getId, v -> v));

        Set<Long> outboundPlanOrderIds = pickingOrderDetailDTOMap.values().stream()
                .map(PickingOrderDTO.PickingOrderDetailDTO::getOutboundOrderPlanId).collect(Collectors.toSet());
        Map<Long, OutboundPlanOrderDTO> outboundPlanOrderDTOMap = outboundPlanOrderApi.getByIds(outboundPlanOrderIds).stream()
                .collect(Collectors.toMap(OutboundPlanOrderDTO::getId, Function.identity()));

        Set<Long> skuIds = operationTaskDTOS.stream().map(OperationTaskDTO::getSkuId).collect(Collectors.toSet());
        Map<Long, SkuMainDataDTO> skuMainDataDTOMap = skuMainDataApi.getByIds(skuIds).stream().collect(Collectors.toMap(SkuMainDataDTO::getId, Function.identity()));

        List<ContainerSealedDetailDTO> containerSealedDetailDTOS = operationTaskDTOS.stream()
                .filter(v -> pickingOrderDetailDTOMap.containsKey(v.getDetailId()))
                .map(task -> {
                    PickingOrderDTO.PickingOrderDetailDTO pickingOrderDetailDTO = pickingOrderDetailDTOMap.get(task.getDetailId());
                    PickingOrderDTO pickingOrderDTO = pickingOrderDTOMap.get(task.getOrderId());
                    OutboundPlanOrderDTO outboundPlanOrderDTO = outboundPlanOrderDTOMap.get(pickingOrderDetailDTO.getOutboundOrderPlanId());
                    SkuMainDataDTO skuMainDataDTO = skuMainDataDTOMap.get(task.getSkuId());

                    ContainerSealedDetailDTO detail = new ContainerSealedDetailDTO();
                    detail.setWarehouseAreaId(pickingOrderDTO.getWarehouseAreaId());
                    detail.setWorkStationId(task.getWorkStationId());
                    detail.setOperator(task.getUpdateUser());
                    detail.setPutWallSlotCode(task.getTargetLocationCode());
                    detail.setOwnerCode(pickingOrderDetailDTO.getOwnerCode());
                    detail.setWaveNo(outboundPlanOrderDTO.getWaveNo());
                    detail.setCustomerOrderNo(outboundPlanOrderDTO.getCustomerOrderNo());
                    detail.setCustomerOrderType(outboundPlanOrderDTO.getCustomerOrderType());
                    detail.setCurrierCode(outboundPlanOrderDTO.getCurrierCode());
                    detail.setWaybillNo(outboundPlanOrderDTO.getWaybillNo());
                    detail.setOrigPlatformCode(outboundPlanOrderDTO.getOrigPlatformCode());
                    detail.setExpiredTime(outboundPlanOrderDTO.getExpiredTime());
                    detail.setPriority(outboundPlanOrderDTO.getPriority());
                    detail.setOrderNo(outboundPlanOrderDTO.getOrderNo());
                    detail.setExtendFields(outboundPlanOrderDTO.getExtendFields());
                        detail.setDestinations(outboundPlanOrderDTO.getDestinations());
                    detail.setSkuCode(skuMainDataDTO.getSkuCode());
                    detail.setSkuName(skuMainDataDTO.getSkuName());
                    detail.setBatchAttributes(pickingOrderDetailDTO.getBatchAttributes());
                    detail.setQtyRequired(task.getRequiredQty());
                    detail.setQtyActual(task.getOperatedQty());
                    return detail;
                }).toList();

        containerSealedDTO.setContainerSealedDetailDTOS(containerSealedDetailDTOS);

        return containerSealedDTO;
    }

}
