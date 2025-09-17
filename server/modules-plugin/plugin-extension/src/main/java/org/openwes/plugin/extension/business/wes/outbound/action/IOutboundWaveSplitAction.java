package org.openwes.plugin.extension.business.wes.outbound.action;

import org.openwes.plugin.extension.business.wes.outbound.IOutboundWavePlugin;
import org.openwes.wes.api.outbound.dto.OutboundWaveDTO;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;

import java.util.List;

public interface IOutboundWaveSplitAction extends IOutboundWavePlugin {

    /**
     * split outbound wave to picking orders
     *
     * @param outboundWaveDTO
     * @return
     */
    default List<PickingOrderDTO> split(OutboundWaveDTO outboundWaveDTO) {
        return null;
    }

}
