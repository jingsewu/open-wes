package org.openwes.station.application.business.handler;

import org.openwes.station.api.constants.ApiCodeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BusinessHandlerFactory implements InitializingBean {

    private static final Map<ApiCodeEnum, IBusinessHandler> map = new EnumMap<>(ApiCodeEnum.class);

    private final List<IBusinessHandler> handlerList;

    @Override
    public void afterPropertiesSet() {
        for (IBusinessHandler handler : handlerList) {
            map.put(handler.getApiCode(), handler);
        }
    }

    public static IBusinessHandler getHandler(ApiCodeEnum apiType) {
        return map.get(apiType);
    }
}
