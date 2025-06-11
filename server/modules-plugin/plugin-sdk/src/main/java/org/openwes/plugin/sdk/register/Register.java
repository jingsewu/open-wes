package org.openwes.plugin.sdk.register;

import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;

public interface Register {

    void beforeRegister(ApplicationContext pluginContext, PluginWrapper wrapper);

    void unRegister(ApplicationContext pluginContext, PluginWrapper wrapper);

    void afterRegister(ApplicationContext applicationContext, PluginWrapper wrapper);
}
