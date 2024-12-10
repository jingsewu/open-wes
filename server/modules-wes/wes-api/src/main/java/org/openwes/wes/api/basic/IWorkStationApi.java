package org.openwes.wes.api.basic;

import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.dto.WorkStationDTO;

import java.util.List;

public interface IWorkStationApi {

    void save(WorkStationDTO workStationDTO);

    void enable(Long id);

    void disable(Long id);

    void delete(Long id);

    void online(Long id, WorkStationModeEnum workStationMode);

    void offline(Long id);

    void pause(Long id);

    void resume(Long id);

    WorkStationDTO getById(Long id);

    List<WorkStationDTO> getByWarehouseCode(String warehouseCode);

}
