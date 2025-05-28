package com.open.wes.extension.business.wes.outbound.action;

import com.open.wes.extension.business.wes.outbound.IOutboundWavePlugin;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;

import java.util.List;

public interface IOutboundWaveWaveAction extends IOutboundWavePlugin {

    default List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> outboundPlanOrderDTOs) {
        return null;
    }

}
