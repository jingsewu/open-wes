package org.openwes.api.platform.infrastructure;

import org.openwes.api.platform.application.service.handler.request.station.TagPtlRequestHandler;
import org.openwes.station.api.IPtlApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class WorkStationClientServiceImpl implements WorkStationClientService {

    @DubboReference
    private IPtlApi ptlApi;

    @Override
    public void tapPtl(TagPtlRequestHandler.TapPtlEvent tapPtlEvent) {
        ptlApi.off(tapPtlEvent.getWorkStationId(), tapPtlEvent.getPtlTag());
    }
}
