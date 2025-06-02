package org.openwes.station.application.business.handler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.plugin.extension.business.station.CustomApiParameter;
import org.openwes.plugin.extension.business.station.ICustomApiPlugin;
import org.openwes.plugin.sdk.utils.PluginSdkUtils;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.event.CustomApiEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomApiHandler implements IBusinessHandler<CustomApiEvent> {

    private final PluginSdkUtils pluginUtils;

    @Override
    public void execute(CustomApiEvent body, Long workStationId) {
        List<ICustomApiPlugin> customApiPlugins = pluginUtils.getExtractObject(ICustomApiPlugin.class);
        if (ObjectUtils.isEmpty(customApiPlugins)) {
            log.warn("can't find any custom api plugins");
            return;
        }

        customApiPlugins.stream()
                .filter(v -> StringUtils.equals(v.customApiCode(), body.getCustomApiCode()))
                .forEach(v -> v.execute(new CustomApiParameter(workStationId, body.getEvent())));
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.CUSTOM_API;
    }

    @Override
    public Class<CustomApiEvent> getParameterClass() {
        return CustomApiEvent.class;
    }
}
