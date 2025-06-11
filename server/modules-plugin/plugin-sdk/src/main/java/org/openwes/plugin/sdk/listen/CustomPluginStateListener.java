package org.openwes.plugin.sdk.listen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.plugin.sdk.register.PluginRegister;
import org.pf4j.Plugin;
import org.pf4j.PluginState;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginStateListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPluginStateListener implements PluginStateListener {

    private final PluginRegister pluginRegister;

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        if (event.getPluginState() == PluginState.STARTED) {
            log.debug("Plugin {} started", event.getPlugin().getPluginId());

            Plugin plugin = event.getPlugin().getPlugin();
            pluginRegister.applyRegister(plugin);
        } else if (event.getPluginState() == PluginState.STOPPED) {
            log.debug("Plugin {} stopped", event.getPlugin().getPluginId());

            pluginRegister.applyUnRegister(event.getPlugin().getPlugin());
        } else if (event.getPluginState() == PluginState.UNLOADED) {
            log.debug("Plugin {} unloaded", event.getPlugin().getPluginId());

            pluginRegister.applyUnRegister(event.getPlugin().getPlugin());
        } else if (event.getPluginState() == PluginState.DISABLED) {
            log.debug("Plugin {} disabled", event.getPlugin().getPluginId());

            pluginRegister.applyUnRegister(event.getPlugin().getPlugin());
        }
    }
}
