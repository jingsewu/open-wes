package org.openwes.plugin.sdk.register;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.plugin.sdk.utils.PluginUtils;
import org.pf4j.Plugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * register plugin components into spring application context
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PluginRegister {

    private final List<Register> registers;
    private final ApplicationContext applicationContext;
    public static final Map<String, ApplicationContext> pluginApplicationContexts = Maps.newConcurrentMap();

    public void applyRegister(Plugin plugin) {
        AnnotationConfigApplicationContext pluginApplication = createPluginApplication(plugin);
        pluginApplicationContexts.put(plugin.getWrapper().getPluginId(), pluginApplication);

        registers.forEach(register ->
                register.beforeRegister(pluginApplication, plugin.getWrapper())
        );

        pluginApplication.refresh();

        registers.forEach(register ->
                register.afterRegister(pluginApplication, plugin.getWrapper())
        );
    }

    private AnnotationConfigApplicationContext createPluginApplication(Plugin plugin) {
        Path path = plugin.getWrapper().getPluginPath();

        String uniqueContextId = plugin.getClass() + "_" + System.currentTimeMillis();

        String scanPackages = PluginUtils.getPluginScanPackages(path.toFile(), PluginUtils.PLUGIN_PROPERTIES);
        AnnotationConfigApplicationContext pluginApplicationContext = new AnnotationConfigApplicationContext();
        pluginApplicationContext.setId(uniqueContextId);
        pluginApplicationContext.setClassLoader(plugin.getWrapper().getPluginClassLoader());
        pluginApplicationContext.setParent(applicationContext);
        pluginApplicationContext.scan(scanPackages);

        return pluginApplicationContext;
    }

    public void applyUnRegister(Plugin plugin) {

        ApplicationContext pluginApplicationContext = pluginApplicationContexts.get(plugin.getWrapper().getPluginId());

        if (pluginApplicationContext == null) {
            return;
        }

        registers.forEach(register ->
                register.unRegister(pluginApplicationContext, plugin.getWrapper())
        );

        try {
            if (pluginApplicationContext instanceof AnnotationConfigApplicationContext acac) {
                acac.setClassLoader(null);
                acac.close();
            }

            pluginApplicationContexts.remove(plugin.getWrapper().getPluginId());
            System.gc();
        } catch (Exception e) {
            log.error("applyUnRegister error", e);
        }
    }


    public static ApplicationContext getApplicationContext(String pluginId) {
        return pluginApplicationContexts.get(pluginId);
    }
}
