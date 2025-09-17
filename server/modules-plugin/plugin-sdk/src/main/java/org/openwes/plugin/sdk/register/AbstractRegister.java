package org.openwes.plugin.sdk.register;

import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;

public abstract class AbstractRegister implements Register {

    @Override
    public void beforeRegister(ApplicationContext pluginContext, PluginWrapper plugin) {

    }

    @Override
    public void unRegister(ApplicationContext pluginContext, PluginWrapper plugin) {

    }

    @Override
    public void afterRegister(ApplicationContext pluginContext, PluginWrapper plugin) {

    }
}
