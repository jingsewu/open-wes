package org.openwes.station.domain.entity;

import org.openwes.station.api.constants.ProcessStatusEnum;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * ArrivedContainer wouldn't pass to WMS system , it will be restored to redis and memory .
 * if Station system crashes then it will be got from redis when the system is restarted.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArrivedContainerCache {

    private Long workStationId;
    /**
     * base information
     */
    private String containerCode;
    private String face;
    private Integer rotationAngle;
    // container forward face
    private String forwardFace;

    /**
     * location
     */
    private String locationCode;
    private String workLocationCode;

    // a set of locations that the container can be put into.
    // it is a logical definition.
    // like robotCode is the groupCode of robot, cache shelf code is the groupCode of cache shelf.
    private String groupCode;
    private String robotCode;
    private String robotType;
    private Integer level;
    private Integer bay;

    private List<String> taskCodes;

    /**
     * status
     */
    private boolean empty;
    /**
     * 0: unprocessed ,1: processing , 2: processed
     */
    private ProcessStatusEnum processStatus;

    /**
     * when container arrived , it's contains other attributes. like height, weight, temperature, etc.
     */
    private Map<String, Object> containerAttributes;

    /**
     * layout
     */
    private ContainerSpecDTO containerSpec;

    public void init() {
        this.processStatus = ProcessStatusEnum.UNDO;
    }
}
