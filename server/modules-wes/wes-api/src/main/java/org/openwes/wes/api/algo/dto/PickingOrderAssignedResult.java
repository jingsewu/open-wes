package org.openwes.wes.api.algo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class PickingOrderAssignedResult {

    private Long pickingOrderId;

    private Map<Long, String> assignedStationSlot;
}
