package com.open.wes.extension;

import org.pf4j.ExtensionPoint;

public interface IPlugin extends ExtensionPoint {

    default void initialize() {
    }

    default void destory() {
    }

}
