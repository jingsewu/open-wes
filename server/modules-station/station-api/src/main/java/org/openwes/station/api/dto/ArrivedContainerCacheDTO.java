package org.openwes.station.api.dto;

import org.openwes.station.api.constants.ProcessStatusEnum;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ArrivedContainer wouldn't pass to WMS system , it will be restored to redis and memory .
 * if Station system crashes then it will be got from redis when the system is restarted.
 */
@Data
public class ArrivedContainerCacheDTO {

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
    private Set<String> activeSlotCodes;
    /**
     * GRIDS THAT NEED TO BE DISABLED
     */
    private Set<String> disabledSlotCodes;
    /**
     * recommend slot codes
     */
    private Set<String> recommendSlotCodes;

    /**
     * when container arrived , it's contains other attributes. like height, weight, temperature, etc.
     */
    private Map<String, Object> containerAttributes;

    /**
     * layout
     */
    private ContainerSpecDTO containerSpec;

    /**
     * use configuration to get the rotation angle,identify two scan type. e.g. manual scan and equipment scan .
     * equipment scan used to container arrived and manual scan used to operator scan container.
     */
//    public void getRotationAngle() {
//        //TODO
//    }
}
