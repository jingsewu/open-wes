package org.openwes.wes.outbound.domain.context;

import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundPreAllocatedRecord;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class OutboundPlanOrderCancelContext {
    private List<OutboundPlanOrder> outboundPlanOrders;
    private List<OutboundPreAllocatedRecord> outboundPreAllocatedRecords;
    private List<OutboundWave> outboundWaves;
    private List<PickingOrder> pickingOrders;
}
