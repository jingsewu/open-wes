package org.openwes.station.application.business.handler;

import org.openwes.station.api.constants.ApiCodeEnum;

public interface IBusinessHandler<T> {


    /**
     * 执行业务
     *
     * @param body          parameters
     * @param workStationId work station ID
     *
     * @return
     */
    void execute(T body, Long workStationId);

    ApiCodeEnum getApiCode();

    Class<T> getParameterClass();

}
