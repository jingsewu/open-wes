package org.openwes.wes.basic.work_station.domain.entity;

import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import lombok.Data;

@Data
public class WorkStationConfig {

    private Long id;
    private Long workStationId;

    private WorkStationConfigDTO.InboundStationConfigDTO inboundStationConfig;
    private WorkStationConfigDTO.PickingStationConfigDTO pickingStationConfig = new WorkStationConfigDTO.PickingStationConfigDTO();
    private WorkStationConfigDTO.StocktakeStationConfigDTO stocktakeStationConfig;
    private WorkStationConfigDTO.RelocationStationConfigDTO relocationStationConfig;

    private Long version;

    public void initialize() {
//        inboundStationConfig.initialize();
        pickingStationConfig.initialize();
//        stocktakeStationConfig.initialize();
//        relocationStationConfig.initialize();
    }
}
