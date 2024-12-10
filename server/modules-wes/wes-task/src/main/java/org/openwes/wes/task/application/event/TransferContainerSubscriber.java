package org.openwes.wes.task.application.event;

import com.google.common.eventbus.Subscribe;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.api.dto.callback.wms.ContainerSealedDTO;
import org.openwes.api.platform.api.dto.callback.wms.ContainerSealedDetailDTO;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.openwes.wes.api.outbound.IPickingOrderApi;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.task.ITaskApi;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.event.TransferContainerArrivedEvent;
import org.openwes.wes.api.task.event.TransferContainerSealedEvent;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.task.domain.entity.TransferContainer;
import org.openwes.wes.task.domain.entity.TransferContainerRecord;
import org.openwes.wes.task.domain.repository.TransferContainerRecordRepository;
import org.openwes.wes.task.domain.repository.TransferContainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferContainerSubscriber {

    private final IPickingOrderApi pickingOrderApi;
    private final TransferContainerRepository transferContainerRepository;
    private final ITaskApi taskApi;
    private final IOutboundPlanOrderApi outboundPlanOrderApi;
    private final ISkuMainDataApi skuMainDataApi;
    private final TransferContainerRecordRepository transferContainerRecordRepository;
    private final IWarehouseAreaApi warehouseAreaApi;
    private final CallbackApiFacade callbackApiFacade;

    @Subscribe
    public void onTransferContainerSealed(TransferContainerSealedEvent transferContainerSealedEvent) {

        TransferContainerRecord transferContainerRecord = transferContainerRecordRepository.findById(transferContainerSealedEvent.getTransferContainerRecordId());
        TransferContainer transferContainer = transferContainerRepository
                .findByContainerCodeAndWarehouseCode(transferContainerRecord.getTransferContainerCode(), transferContainerRecord.getWarehouseCode());

        List<Long> currentPeriodRelateRecordIds = transferContainer.getCurrentPeriodRelateRecordIds();
        List<TransferContainerRecord> transferContainerRecords = transferContainerRecordRepository.findAllById(currentPeriodRelateRecordIds);

        ContainerSealedDTO containerSealedDTO = buildContainerSealedDetails(transferContainerRecord, transferContainerRecords);

        callbackApiFacade.callback(CallbackApiTypeEnum.OUTBOUND_SEAL_CONTAINER, "", containerSealedDTO);
    }

    private ContainerSealedDTO buildContainerSealedDetails(TransferContainerRecord transferContainerRecord, List<TransferContainerRecord> transferContainerRecords) {

        ContainerSealedDTO containerSealedDTO = new ContainerSealedDTO();
        containerSealedDTO.setTransferContainerCode(transferContainerRecord.getTransferContainerCode());
        containerSealedDTO.setWarehouseCode(transferContainerRecord.getWarehouseCode());

        List<OperationTaskDTO> operationTaskDTOS = taskApi.queryByTransferContainerRecordIds(transferContainerRecords.stream().map(TransferContainerRecord::getId).toList());
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
                    detail.setReservedFields(outboundPlanOrderDTO.getReservedFields());
//                        detail.setDestinations(outboundPlanOrderDTO.getDestinations());
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

    @Subscribe
    public void onTransferContainerArrived(TransferContainerArrivedEvent transferContainerArriveEvent) {

        WarehouseAreaDTO warehouseAreaDTO = warehouseAreaApi.getById(transferContainerArriveEvent.getWarehouseAreaId());

        List<TransferContainer> transferContainers = transferContainerArriveEvent.getDetails().stream().map(v -> {
            TransferContainer transferContainer = transferContainerRepository.findByContainerCodeAndWarehouseCode(v.getContainerCode(), warehouseAreaDTO.getWarehouseCode());
            if (transferContainer == null) {
                log.warn("transfer container: {} not found at location: {}", v.getContainerCode(),
                        v.getLocationCode());
                return null;
            }

            transferContainer.updateLocation(warehouseAreaDTO.getId(), v.getLocationCode());

            return transferContainer;
        }).filter(Objects::nonNull).toList();

        transferContainerRepository.saveAll(transferContainers);
    }

}
