package org.openwes.plugin.sdk.listen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.plugin.sdk.register.PluginRegister;
import org.pf4j.Plugin;
import org.pf4j.PluginState;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginStateListener;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPluginStateListener implements PluginStateListener {

    private final PluginRegister pluginRegister;
    private final SpringPluginManager springPluginManager;

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        log.debug("Plugin {} state changed to {}", event.getPlugin().getPluginId(), event.getPluginState());
        // Extension instances cached by SpringExtensionFactory may belong to the changed plugin;
        // drop them so the next lookup rebuilds against the current plugin set.
        if (springPluginManager.getSpringExtensionFactory() != null) {
            springPluginManager.getSpringExtensionFactory().evictAll();
        }

        if (event.getPluginState() == PluginState.STARTED) {
            Plugin plugin = event.getPlugin().getPlugin();
            pluginRegister.applyRegister(plugin);
        } else if (event.getPluginState() == PluginState.STOPPED) {
            pluginRegister.applyUnRegister(event.getPlugin().getPlugin());
        } else if (event.getPluginState() == PluginState.UNLOADED) {
            pluginRegister.applyUnRegister(event.getPlugin().getPlugin());
        } else if (event.getPluginState() == PluginState.DISABLED) {
            pluginRegister.applyUnRegister(event.getPlugin().getPlugin());
        }
    }
}
