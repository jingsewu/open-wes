package org.openwes.station.application.business.handler.common;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.infrastructure.remote.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CallRobotHandler implements IBusinessHandler<String> {

    private final EquipmentService equipmentService;

    @Override
    public void execute(String body, Long workStationId) {
        equipmentService.callRobot(workStationId);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.CALL_ROBOT;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }
}
