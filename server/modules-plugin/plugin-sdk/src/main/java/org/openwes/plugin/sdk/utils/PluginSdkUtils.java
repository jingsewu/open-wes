package org.openwes.plugin.sdk.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.plugin.extension.IPlugin;
import org.openwes.plugin.sdk.spi.ScriptExtensionProvider;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PluginSdkUtils {

    private final PluginManager pluginManager;
    private final List<ScriptExtensionProvider> scriptExtensionProviders;

    public <T> List<T> getExtractObject(Class<T> tClass) {

        List<T> extractObjects = new ArrayList<>(pluginManager.getExtensions(tClass));
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            extractObjects.addAll(provider.getExtensions(tClass));
        }

        if (CollectionUtils.isEmpty(extractObjects)) {
            return Collections.emptyList();
        }
        extractObjects.sort(Comparator.comparingInt(PluginSdkUtils::priorityOf).reversed());
        return extractObjects;
    }

    /** Extensions that do not extend IPlugin keep priority 0 and preserve their relative order (stable sort). */
    private static int priorityOf(Object extension) {
        return extension instanceof IPlugin plugin ? plugin.priority() : 0;
    }
}
