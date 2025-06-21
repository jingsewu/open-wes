package org.openwes.plugin.extension.config;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

/**
 * every config class should implement this interface
 * <p>
 * Attention:
 * 1. Every plugin must only has one config class that implement this interface
 * 2. Every plugin config should container annotation @Extension and @NoArgsConstructor
 * 3. The @NoArgsConstructor is for deserialization to the config bean
 */
@Extension
public interface IExtensionConfig extends ExtensionPoint {
}
