package org.openwes.plugin.extension.business.wes.outbound.action;

import org.openwes.plugin.extension.business.wes.outbound.IOutboundWavePlugin;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;

import java.util.List;

public interface IOutboundWaveWaveAction extends IOutboundWavePlugin {

    /**
     * group outbound plan orders to outbound waves
     *
     * @param outboundPlanOrderDTOs
     * @return
     */
    default List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> outboundPlanOrderDTOs) {
        return null;
    }

}
