package org.openwes.plugin.sdk;

import lombok.RequiredArgsConstructor;
import org.openwes.plugin.api.constants.ApplicationPluginStatusEnum;
import org.openwes.plugin.sdk.domain.entity.ApplicationPlugin;
import org.openwes.plugin.sdk.domain.repository.ApplicationPluginRepository;
import org.pf4j.PluginManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class PluginInitialize implements CommandLineRunner {

    private final ApplicationPluginRepository pluginRepository;
    private final PluginManager pluginManager;

    @Override
    public void run(String... args) {

        List<ApplicationPlugin> plugins = pluginRepository.findAllByStatus(ApplicationPluginStatusEnum.STARTED);

        plugins.forEach(plugin -> {
            pluginManager.startPlugin(plugin.getPluginUniqueKey());
        });
    }
}
