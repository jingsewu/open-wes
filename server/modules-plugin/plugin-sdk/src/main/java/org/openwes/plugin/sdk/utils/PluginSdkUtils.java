package org.openwes.plugin.sdk.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.plugin.sdk.domain.entity.ApplicationPlugin;
import org.openwes.plugin.sdk.domain.service.PluginService;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Component
@RequiredArgsConstructor
public class PluginSdkUtils {

    private final PluginManager pluginManager;
    private final PluginService pluginService;

    public <T> List<T> getExtractObject(Class<T> tClass) {

        List<ApplicationPlugin> plugins = pluginService.getAllStartedPlugins();

        if (CollectionUtils.isEmpty(plugins)) {
            return Collections.emptyList();
        }

        List<T> extractObjects = plugins.stream()
                .map(plugin -> pluginManager.getExtensions(tClass, plugin.getPluginUniqueKey()))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull).toList();

        if (CollectionUtils.isNotEmpty(extractObjects)) {
            return extractObjects;
        }
        return Collections.emptyList();
    }
}
