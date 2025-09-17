package org.openwes.plugin.extension;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

@Extension
public interface IPlugin extends ExtensionPoint {

    default void initialize() {
    }

    default void destory() {
    }

}
