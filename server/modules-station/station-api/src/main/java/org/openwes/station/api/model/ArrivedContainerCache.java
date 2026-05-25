package org.openwes.station.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.station.api.constants.ProcessStatusEnum;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArrivedContainerCache {

    private Long workStationId;
    private String containerCode;
    private String face;
    private Integer rotationAngle;
    private String forwardFace;

    private String locationCode;
    private String workLocationCode;
    private String groupCode = "";
    private String robotCode;
    private String robotType;
    private Integer level;
    private Integer bay;

    private List<String> taskCodes;
    private boolean empty;
    private ProcessStatusEnum processStatus;
    private Map<String, Object> containerAttributes;
    private ContainerSpecDTO containerSpec;

    public void init() {
        this.processStatus = ProcessStatusEnum.UNDO;
    }

    public void proceed() {
        this.processStatus = ProcessStatusEnum.PROCEED;
    }

    public void processing() {
        this.processStatus = ProcessStatusEnum.PROCESSING;
    }
}
