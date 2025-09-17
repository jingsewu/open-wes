package org.openwes.plugin.sdk.configuration;

import lombok.extern.slf4j.Slf4j;
import org.openwes.plugin.sdk.listen.CustomPluginStateListener;
import org.openwes.plugin.sdk.register.PluginRegister;
import org.pf4j.DefaultPluginManager;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Slf4j
@Configuration
public class PluginConfiguration {

    @Value("${pf4j.plugin.jar.dir:plugins/jar}")
    private String pluginDir;

    @Bean
    public DefaultPluginManager pluginManager(PluginRegister pluginClassRegister) {
        SpringPluginManager springPluginManager = new SpringPluginManager(Paths.get(pluginDir));
        springPluginManager.addPluginStateListener(new CustomPluginStateListener(pluginClassRegister));
        return springPluginManager;
    }

}
