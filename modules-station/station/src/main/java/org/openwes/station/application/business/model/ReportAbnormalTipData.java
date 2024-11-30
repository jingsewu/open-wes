package org.openwes.station.application.business.model;

import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import lombok.Data;

import java.util.Collection;

@Data
public class ReportAbnormalTipData {

    private Integer totalToBeRequiredQty;

    private ArrivedContainerCache arrivedContainer;

    private Collection<OperationTaskDTO> operationTaskDTOS;

    private Collection<SkuMainDataDTO> skuMainDataDTOS;
}
