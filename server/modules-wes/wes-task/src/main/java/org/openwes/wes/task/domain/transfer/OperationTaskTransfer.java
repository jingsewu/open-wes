package org.openwes.wes.task.domain.transfer;

import org.mapstruct.*;
import org.openwes.api.platform.api.dto.callback.wms.ContainerSealedDetailDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.task.domain.entity.OperationTask;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OperationTaskTransfer {

    List<OperationTaskDTO> toDTOs(List<OperationTask> operationTasks);

    List<OperationTask> toDOs(List<OperationTaskDTO> operationTaskDTOS);

    @Mappings({
            @Mapping(source = "pickingOrderDTO.warehouseAreaId", target = "warehouseAreaId"),
            @Mapping(source = "task.workStationId", target = "workStationId"),
            @Mapping(source = "task.updateUser", target = "operator"),
            @Mapping(source = "task.targetLocationCode", target = "putWallSlotCode"),
            @Mapping(source = "pickingOrderDetailDTO.ownerCode", target = "ownerCode"),
            @Mapping(source = "outboundPlanOrderDTO.waveNo", target = "waveNo"),
            @Mapping(source = "outboundPlanOrderDTO.customerOrderNo", target = "customerOrderNo"),
            @Mapping(source = "outboundPlanOrderDTO.customerOrderType", target = "customerOrderType"),
            @Mapping(source = "outboundPlanOrderDTO.carrierCode", target = "carrierCode"),
            @Mapping(source = "outboundPlanOrderDTO.waybillNo", target = "waybillNo"),
            @Mapping(source = "outboundPlanOrderDTO.origPlatformCode", target = "origPlatformCode"),
            @Mapping(source = "outboundPlanOrderDTO.expiredTime", target = "expiredTime"),
            @Mapping(source = "outboundPlanOrderDTO.priority", target = "priority"),
            @Mapping(source = "outboundPlanOrderDTO.orderNo", target = "orderNo"),
            @Mapping(source = "outboundPlanOrderDTO.extendFields", target = "extendFields"),
            @Mapping(source = "outboundPlanOrderDTO.destinations", target = "destinations"),
            @Mapping(source = "skuMainDataDTO.skuCode", target = "skuCode"),
            @Mapping(source = "skuMainDataDTO.skuName", target = "skuName"),
            @Mapping(source = "pickingOrderDetailDTO.batchAttributes", target = "batchAttributes"),
            @Mapping(source = "task.requiredQty", target = "qtyRequired"),
            @Mapping(source = "task.operatedQty", target = "qtyActual")
    })
    ContainerSealedDetailDTO toContainerSealedDetailDTO(
            OperationTask task,
            PickingOrderDTO pickingOrderDTO,
            PickingOrderDTO.PickingOrderDetailDTO pickingOrderDetailDTO,
            OutboundPlanOrderDTO outboundPlanOrderDTO,
            SkuMainDataDTO skuMainDataDTO
    );

}
