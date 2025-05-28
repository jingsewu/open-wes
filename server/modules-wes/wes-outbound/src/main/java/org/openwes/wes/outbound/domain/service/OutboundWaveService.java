package org.openwes.wes.outbound.domain.service;

import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;

import java.util.Collection;
import java.util.List;

public interface OutboundWaveService {

    /**
     * union outbound plan orders into a wave to improve picking performance
     *
     * @param outboundPlanOrders
     * @return
     */
    Collection<List<OutboundPlanOrder>> wavePickings(List<OutboundPlanOrder> outboundPlanOrders);

    /**
     * split a wave into a list of picking orders
     *
     * @param outboundWave outboundWave
     * @return
     */
    List<PickingOrder> spiltWave(OutboundWave outboundWave);
}
