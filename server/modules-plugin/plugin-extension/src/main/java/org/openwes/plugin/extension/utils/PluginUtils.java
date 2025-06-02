package org.openwes.plugin.extension.utils;

import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.plugin.api.IPluginApi;
import org.openwes.plugin.api.dto.PluginConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PluginUtils {

    private static IPluginApi pluginApi;

    @Autowired
    public void setPluginApi(IPluginApi pluginApi) {
        PluginUtils.pluginApi = pluginApi;
    }

    public static <T> T getPluginConfig(String pluginId, Class<T> clazz) {

        PluginConfigDTO tenantPluginConfigDTO = pluginApi.getPluginConfig(pluginId);

        if (tenantPluginConfigDTO != null) {
            return JsonUtils.string2Object(tenantPluginConfigDTO.getConfigInfo(), clazz);
        } else {
            return newClazzInstance(clazz);
        }
    }

    private static <T> T newClazzInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            log.error("no such method exception: ", e);
        } catch (ReflectiveOperationException e) {
            log.error("reflective operation error: ", e);
        }
        throw new WmsException("new clazz: {} instance error,", clazz.getName());
    }

}
