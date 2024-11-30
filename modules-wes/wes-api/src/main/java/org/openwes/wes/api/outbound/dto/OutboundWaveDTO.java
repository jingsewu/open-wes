package org.openwes.wes.api.outbound.dto;

import lombok.Data;

import java.util.List;

@Data
public class OutboundWaveDTO {

    private String warehouseCode;
    private int priority;
    private boolean shortOutbound;
    private String waveNo;
    private List<OutboundPlanOrderDTO> outboundPlanOrders;
    private boolean hasRobotPickingOrder;
}
