package org.openwes.plugin.extension;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

@Extension
public interface IPlugin extends ExtensionPoint {

    default void initialize() {
    }

    default void destory() {
    }

    /**
     * Higher value wins when several plugins can serve the same extension point. Ties are broken
     * by the registry order (script plugins are merged after jar plugins, sorted by plugin key).
     */
    default int priority() {
        return 0;
    }

}
