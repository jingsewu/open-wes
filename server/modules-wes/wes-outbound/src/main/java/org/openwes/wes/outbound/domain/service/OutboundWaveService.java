package org.openwes.wes.outbound.domain.service;

import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;

import java.util.Collection;
import java.util.List;

public interface OutboundWaveService {

    Collection<List<OutboundPlanOrder>> wavePickings(List<OutboundPlanOrder> outboundPlanOrders);
}
