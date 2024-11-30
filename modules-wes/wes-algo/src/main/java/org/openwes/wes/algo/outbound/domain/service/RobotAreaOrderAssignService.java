package org.openwes.wes.algo.outbound.domain.service;

import org.openwes.wes.algo.outbound.domain.entity.OrderAssignStationModel;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;

import java.util.List;

public interface RobotAreaOrderAssignService {

    void matchingAvailablePutWallSlots(PickingOrderDTO pickingOrderDTO, List<PutWallSlotDTO> putWallSlots);

    long calculateStationScore(Long workStationId, OrderAssignStationModel orderAssignStationModel,
                               List<PickingOrderDTO.PickingOrderDetailDTO> details);

    void assign(PickingOrderDTO pickingOrderDTO, Long workStationId, List<PutWallSlotDTO> availablePutWallSlots);
}
