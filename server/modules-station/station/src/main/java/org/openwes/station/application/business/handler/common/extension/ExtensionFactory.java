package org.openwes.station.application.business.handler.common.extension;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExtensionFactory {

    private final Map<WorkStationModeEnum, Map<ApiCodeEnum, IExtension>> handlerMap;

    @Autowired
    public ExtensionFactory(List<IExtension> extensions) {
        this.handlerMap = extensions.stream()
                .collect(Collectors.groupingBy(
                        // Group by WorkStationModeEnum
                        IExtension::getWorkStationMode,
                        // Collect into nested maps: ApiCodeEnum -> IExtension
                        Collectors.toMap(
                                IExtension::getApiCode,
                                Function.identity()
                        )
                ));
    }

    public <T extends IExtension> T getExtension(WorkStationModeEnum workStationMode, ApiCodeEnum apiCode) {
        if (handlerMap == null) {
            return null;
        }

        Map<ApiCodeEnum, IExtension> extensions = handlerMap.get(workStationMode);
        if (extensions == null) {
            return null;
        }

        return (T) extensions.get(apiCode);
    }
}
