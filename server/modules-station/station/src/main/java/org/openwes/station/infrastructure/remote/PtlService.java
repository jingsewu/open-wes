package org.openwes.station.infrastructure.remote;

import org.openwes.api.platform.api.ICallbackApi;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.api.dto.callback.CallbackMessage;
import org.openwes.station.api.dto.PtlMessageDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class PtlService {

    @DubboReference
    private ICallbackApi callbackApi;

    public void sendMessage(PtlMessageDTO ptlMessageDTO, Long workStationId) {
        callbackApi.callback(CallbackApiTypeEnum.PTL_CONTROL, String.valueOf(workStationId), new CallbackMessage<>().setData(ptlMessageDTO));
    }
}
