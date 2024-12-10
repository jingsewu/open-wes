package org.openwes.wes.api.outbound;

import org.openwes.wes.api.outbound.dto.OutboundPlanOrderCancelDTO;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;

import java.util.Collection;
import java.util.List;

public interface IOutboundPlanOrderApi {

    void createOutboundPlanOrder(List<OutboundPlanOrderDTO> outboundPlanOrderDTOs);

    List<OutboundPlanOrderDTO> getOutboundPlanOrders(String warehouseCode, Collection<String> customerOrderNos);

    List<String> cancelOutboundPlanOrder(OutboundPlanOrderCancelDTO outboundPlanOrderCancelDTO);

    List<OutboundPlanOrderDTO> getByCustomerWaveNos(Collection<String> waveNos);

    List<OutboundPlanOrderDTO> getByIds(Collection<Long> outboundPlanOrderIds);
}
