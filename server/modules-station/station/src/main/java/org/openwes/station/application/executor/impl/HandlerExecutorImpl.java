package org.openwes.station.application.executor.impl;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.CommonErrorDescEnum;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.common.utils.utils.ValidatorUtils;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.BusinessHandlerFactory;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.executor.HandlerExecutor;
import org.openwes.station.controller.websocket.cluster.WebsocketMessageListenerUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service("stationHandlerExecutor")
@RequiredArgsConstructor
@Validated
public class HandlerExecutorImpl implements HandlerExecutor {

    private final DistributeLock distributeLock;
    private final WebsocketMessageListenerUtils websocketMessageListenerUtils;

    @Override
    public void execute(ApiCodeEnum apiCode, String body, Long workStationId) {

        log.info("work station: {} received eventType: {} body: {}", workStationId, apiCode, body);

        distributeLock.acquireLockIfThrows(RedisConstants.WORK_STATION_OPERATE_SYNC_LOCK + workStationId, 3000L);
        try {
            IBusinessHandler<Object> businessHandler = BusinessHandlerFactory.getHandler(apiCode);

            Object parameter;

            Class<?> parameterClass = businessHandler.getParameterClass();
            if (parameterClass.isAssignableFrom(String.class)) {
                parameter = body;
            } else {
                Preconditions.checkState(StringUtils.isNotEmpty(body));
                parameter = parserParameter(body, parameterClass);

                ValidatorUtils.validate(parameter);
            }
            businessHandler.execute(parameter, workStationId);

            websocketMessageListenerUtils.noticeWorkStationVOChanged(workStationId);
        } finally {
            distributeLock.releaseLock(RedisConstants.WORK_STATION_OPERATE_SYNC_LOCK + workStationId);
        }

    }

    private Object parserParameter(String body, Class<?> parameterClass) {
        try {
            return JsonUtils.string2Object(body, parameterClass);
        } catch (Exception e) {
            log.error("parser parameter: {} error: ", body, e);
            throw WmsException.throwWmsException(CommonErrorDescEnum.PARAMETER_ERROR);
        }
    }
}
