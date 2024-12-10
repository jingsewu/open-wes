package org.openwes.wes.api.basic.dto;

import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.constants.WorkStationStatusEnum;
import org.openwes.wes.api.ems.proxy.constants.WorkLocationTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkStationDTO implements Serializable {

    private Long id;

    @NotNull
    private String stationCode;
    @NotEmpty
    private String stationName;

    private WorkStationStatusEnum workStationStatus;

    @NotEmpty
    private String warehouseCode;
    @NotNull
    private Long warehouseAreaId;
    private boolean enable;

    private WorkStationModeEnum workStationMode;

    private List<WorkStationModeEnum> allowWorkStationModes;

    private List<WorkLocation<? extends WorkLocationSlot>> workLocations;
    private List<PutWallDTO> putWalls;
    private WorkStationConfigDTO workStationConfig;

    private PositionDTO position;

    private Long version;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkLocation<T extends WorkLocationSlot> implements Serializable {

        private String stationCode;
        /**
         * like SHELF, ROBOT, CONVEYOR and so on
         */
        private WorkLocationTypeEnum workLocationType;
        private String workLocationCode;

        private boolean enable;

        private List<T> workLocationSlots;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkLocationSlot implements Serializable {

        private String workLocationCode;

        // a set of location slots that the container can be put into in a location.
        // it is a logical definition.
        // like robot code is the groupCode of robot, cache shelf code is the groupCode of cache shelf.
        private String groupCode;

        private String slotCode;
        private Integer level;
        private Integer bay;
        private boolean enable;
    }
}
