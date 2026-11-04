package org.openwes.plugin.sdk.spi;

import java.util.List;

/**
 * SPI implemented by script-plugin-runtime so dynamically materialized script extensions merge
 * with PF4J jar plugins at every extraction point. Injecting List&lt;ScriptExtensionProvider&gt;
 * yields an empty list when the runtime module is absent, so plugin-sdk stays usable standalone.
 */
public interface ScriptExtensionProvider {

    <T> List<T> getExtensions(Class<T> extensionType);
}
