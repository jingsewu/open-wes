package org.openwes.script.plugin.runtime.registry;

import lombok.RequiredArgsConstructor;
import org.openwes.plugin.sdk.spi.ScriptExtensionProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScriptExtensionProviderImpl implements ScriptExtensionProvider {

    private final ScriptPluginRegistry registry;

    @Override
    public <T> List<T> getExtensions(Class<T> extensionType) {
        return registry.getExtensions(extensionType);
    }
}
