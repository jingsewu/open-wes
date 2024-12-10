package org.openwes.wes.inbound.domain.transfer;

import org.openwes.wes.api.inbound.dto.PutAwayTaskDTO;
import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;
import org.openwes.wes.inbound.domain.entity.PutAwayTask;
import org.openwes.wes.inbound.domain.entity.PutAwayTaskDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT
)
public interface PutAwayTaskTransfer {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "containerCode", source = "detail.targetContainerCode")
    @Mapping(target = "containerSpecCode", source = "detail.targetContainerSpecCode")
    PutAwayTask toDO(AcceptOrder acceptOrder, AcceptOrderDetail detail);

    @Mapping(target = "acceptOrderDetailId", source = "id")
    @Mapping(target = "containerId", source = "targetContainerId")
    @Mapping(target = "containerCode", source = "targetContainerCode")
    @Mapping(target = "containerSlotCode", source = "targetContainerSlotCode")
    @Mapping(target = "containerFace", source = "targetContainerFace")
    @Mapping(target = "qtyPutAway", source = "qtyAccepted")
    @Mapping(ignore = true, target = "id")
    PutAwayTaskDetail toDetailDO(AcceptOrderDetail acceptOrderDetail);

    List<PutAwayTask> toDOs(List<PutAwayTaskDTO> putAwayTaskDTOs);

    List<PutAwayTaskDTO> toDTOs(List<PutAwayTask> putAwayTasks);
}
