package org.openwes.station.application.business.handler.common.extension;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;

public interface IExtension {

    WorkStationModeEnum getWorkStationMode();

    ApiCodeEnum getApiCode();
}
