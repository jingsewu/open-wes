package org.openwes.station.controller.view.context;

import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import lombok.Data;

@Data
public class ViewContext<T extends WorkStationCache> {

    private WorkStationVO workStationVO;

    private WorkStationDTO workStationDTO;

    private T workStationCache;

    public ViewContext(WorkStationDTO workStationDTO, T workStationCache) {
        this.workStationVO = new WorkStationVO();
        this.workStationDTO = workStationDTO;
        this.workStationCache = workStationCache;
    }
}
