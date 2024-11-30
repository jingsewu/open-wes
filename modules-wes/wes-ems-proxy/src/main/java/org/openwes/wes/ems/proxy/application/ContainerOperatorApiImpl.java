package org.openwes.wes.ems.proxy.application;

import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.api.dto.callback.CallbackMessage;
import org.openwes.wes.ems.proxy.domain.entity.EmsLocationConfig;
import org.openwes.wes.ems.proxy.domain.repository.EmsLocationConfigRepository;
import org.openwes.wes.ems.proxy.infrastructure.remote.WmsTaskCallbackFacade;
import org.openwes.wes.api.ems.proxy.IContainerOperatorApi;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.ems.proxy.dto.ContainerOperation;
import org.openwes.wes.api.ems.proxy.dto.EmsLocationConfigDTO;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.common.facade.StationCallbackFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@DubboService
@RequiredArgsConstructor
public class ContainerOperatorApiImpl implements IContainerOperatorApi {

    private final StationCallbackFacade stationCallbackFacade;
    private final WmsTaskCallbackFacade wmsTaskCallbackFacade;
    private final CallbackApiFacade callbackApiFacade;
    private final EmsLocationConfigRepository workLocationRepository;

    @Override
    public void containerArrive(ContainerArrivedEvent containerArrivedEvent) {
        EmsLocationConfig emsLocationConfig = workLocationRepository.findByLocationCode(containerArrivedEvent.getWorkLocationCode());

        // 在一些简单的场景，如果没有处理容器位置上报的特殊逻辑。例如只有普通工作站，或者输送线工作站但是不需要对控制输送线上箱子的流动
        // 这时不需要配置EmsLocation。所有的容器到达通知，直接转发给work station
        if (emsLocationConfig == null) {
            stationCallbackFacade.containerArrive(containerArrivedEvent);
            return;
        }

        containerArrivedEvent.setWarehouseAreaId(emsLocationConfig.getWarehouseAreaId());

        EmsLocationConfigDTO.LocationType locationType = emsLocationConfig.getLocationType();
        if (locationType == null) {
            return;
        }

        if (locationType.isNotifyWorkStation()) {
            stationCallbackFacade.containerArrive(containerArrivedEvent);
        }

        if (locationType.isNotifyTransferContainer()) {
            wmsTaskCallbackFacade.transferContainerArrive(containerArrivedEvent);
        }

    }

    @Override
    public void containerLeave(ContainerOperation containerOperation) {
        callbackApiFacade.callback(CallbackApiTypeEnum.CONTAINER_LEAVE, new CallbackMessage<>().setData(containerOperation), null);
    }
}
