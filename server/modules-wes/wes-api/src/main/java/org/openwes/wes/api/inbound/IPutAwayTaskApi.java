package org.openwes.wes.api.inbound;

import org.openwes.wes.api.inbound.dto.PutAwayTaskDTO;

import java.util.List;

public interface IPutAwayTaskApi {

    void create(List<PutAwayTaskDTO> putAwayTasks);

    void complete(List<Long> putAwayTaskIds, String locationCode);
}
