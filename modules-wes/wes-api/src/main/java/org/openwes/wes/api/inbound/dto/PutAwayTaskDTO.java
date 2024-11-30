package org.openwes.wes.api.inbound.dto;

import org.openwes.wes.api.inbound.constants.PutAwayTaskStatusEnum;
import org.openwes.wes.api.inbound.constants.PutAwayTaskTypeEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class PutAwayTaskDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 2628361273620765293L;

    private Long id;

    private String taskNo;
    private PutAwayTaskTypeEnum taskType;

    private String warehouseCode;
    private String containerCode;
    private String containerSpecCode;

    private Long warehouseAreaId;
    private Long workStationId;

    private String locationCode;
    private List<PutAwayTaskDetailDTO> putAwayTaskDetails;

    private Map<String, Object> extendFields;

    private PutAwayTaskStatusEnum taskStatus;

}
