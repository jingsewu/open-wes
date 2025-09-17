package org.openwes.plugin.sdk.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


@Component
@RequiredArgsConstructor
public class PluginSdkUtils {

    private final PluginManager pluginManager;

    public <T> List<T> getExtractObject(Class<T> tClass) {

        List<T> extractObjects = pluginManager.getExtensions(tClass);

        if (CollectionUtils.isNotEmpty(extractObjects)) {
            return extractObjects;
        }
        return Collections.emptyList();
    }
}
