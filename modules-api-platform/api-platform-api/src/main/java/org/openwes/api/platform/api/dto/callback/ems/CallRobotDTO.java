package org.openwes.api.platform.api.dto.callback.ems;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CallRobotDTO {

    @NotNull
    private Long workStationId;
    private String locationCode;
}
