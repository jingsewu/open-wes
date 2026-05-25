package org.openwes.station.application;

import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.station.api.IStationApi;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.api.dto.TapPtlEvent;
import org.openwes.station.application.executor.HandlerExecutor;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Primary
@Validated
@DubboService
@RequiredArgsConstructor
public class StationApiImpl implements IStationApi {

    private final HandlerExecutor handlerExecutor;

    @Override
    public void containerArrive(ContainerArrivedEvent containerArrivedEvent) {
        handlerExecutor.execute(ApiCodeEnum.CONTAINER_ARRIVED, JsonUtils.obj2String(containerArrivedEvent),
                containerArrivedEvent.getWorkStationId());
    }

    @Override
    public void tapPtl(TapPtlEvent tapPtlEvent) {
        handlerExecutor.execute(ApiCodeEnum.TAP_PTL, JsonUtils.obj2String(tapPtlEvent),
                tapPtlEvent.getWorkStationId());
    }
}
