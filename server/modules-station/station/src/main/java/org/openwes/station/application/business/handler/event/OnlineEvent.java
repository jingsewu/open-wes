package org.openwes.station.application.business.handler.event;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;

@Data
public class OnlineEvent {

    @NotNull
    private WorkStationModeEnum workStationMode;
    private boolean hasOrder;
}
