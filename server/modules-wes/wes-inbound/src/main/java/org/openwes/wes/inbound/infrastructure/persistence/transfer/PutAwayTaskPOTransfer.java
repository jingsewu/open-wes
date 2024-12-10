package org.openwes.wes.inbound.infrastructure.persistence.transfer;

import org.openwes.wes.inbound.domain.entity.PutAwayTask;
import org.openwes.wes.inbound.domain.entity.PutAwayTaskDetail;
import org.openwes.wes.inbound.infrastructure.persistence.po.PutAwayTaskDetailPO;
import org.openwes.wes.inbound.infrastructure.persistence.po.PutAwayTaskPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PutAwayTaskPOTransfer {

    PutAwayTaskPO toPO(PutAwayTask putAwayTask);

    PutAwayTask toDO(PutAwayTaskPO putAwayTaskPO, List<PutAwayTaskDetailPO> putAwayTaskDetails);

    List<PutAwayTaskDetail> toDetailDOs(List<PutAwayTaskDetailPO> putAwayTaskDetailPOS);

    PutAwayTaskDetailPO toDetailPO(PutAwayTaskDetail putAwayTaskDetail);

    List<PutAwayTask> toDOs(List<PutAwayTaskPO> putAwayTaskPOS);

    List<PutAwayTaskPO> toPOs(List<PutAwayTask> putAwayTasks);
}
