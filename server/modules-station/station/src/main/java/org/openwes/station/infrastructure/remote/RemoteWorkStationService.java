package org.openwes.station.infrastructure.remote;

import org.openwes.wes.api.basic.IPutWallApi;
import org.openwes.wes.api.basic.IWorkStationApi;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import lombok.Setter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Setter
@Service
public class RemoteWorkStationService {

    @DubboReference
    private IWorkStationApi workStationApi;
    @DubboReference
    private IPutWallApi putWallApi;

    public void online(Long workStationId, WorkStationModeEnum workStationMode) {
        workStationApi.online(workStationId, workStationMode);
    }

    public void offline(Long workStationId) {
        workStationApi.offline(workStationId);
    }

    public void pause(Long workStationId) {
        workStationApi.pause(workStationId);
    }

    public void resume(Long workStationId) {
        workStationApi.resume(workStationId);
    }

    public WorkStationDTO queryWorkStation(Long workStationId) {
        return workStationApi.getById(workStationId);
    }

    public PutWallSlotDTO queryPutWallSlot(Long workStationId, String putWallSlotCode) {
        return putWallApi.getPutWallSlot(putWallSlotCode, workStationId);
    }

    public List<PutWallSlotDTO> queryPutWallSlots(Long workStationId, Collection<String> putWallSlotCodes) {
        return putWallApi.getPutWallSlots(putWallSlotCodes, workStationId);
    }


}
