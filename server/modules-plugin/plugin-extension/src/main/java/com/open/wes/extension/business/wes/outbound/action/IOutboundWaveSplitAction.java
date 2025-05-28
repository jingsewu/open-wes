package com.open.wes.extension.business.wes.outbound.action;

import com.open.wes.extension.business.wes.outbound.IOutboundWavePlugin;
import org.openwes.wes.api.outbound.dto.OutboundWaveDTO;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;

import java.util.List;

public interface IOutboundWaveSplitAction extends IOutboundWavePlugin {

    default List<PickingOrderDTO> split(OutboundWaveDTO outboundWaveDTO) {
        return null;
    }

}
