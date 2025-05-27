package org.openwes.plugin.sdk.utils;

import org.openwes.plugin.sdk.domain.service.TenantPluginService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Component
@RequiredArgsConstructor
public class PluginUtils {

    private final TenantPluginService tenantPluginService;
    private final PluginManager pluginManager;

    public <T> List<T> getExtractObject(Class<T> tClass) {

        List<String> pluginIds = tenantPluginService.getStartedTenantPluginIds();

        if (CollectionUtils.isEmpty(pluginIds)) {
            return Collections.emptyList();
        }

        List<T> extractObjects = pluginIds.stream()
                .map(pluginId -> pluginManager.getExtensions(tClass, pluginId))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull).toList();

        if (CollectionUtils.isNotEmpty(extractObjects)) {
            return extractObjects;
        }
        return Collections.emptyList();
    }
}
