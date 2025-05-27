package org.openwes.plugin.sdk.controller;

import lombok.RequiredArgsConstructor;
import org.openwes.plugin.api.dto.TenantPluginConfigDTO;
import org.openwes.plugin.sdk.domain.service.TenantPluginService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin")
public class PluginController {

    private final TenantPluginService tenantPluginService;

    @GetMapping(value = "/start/{pluginUniqueKey}")
    public void start(@PathVariable("pluginUniqueKey") String pluginUniqueKey) {
        tenantPluginService.start(pluginUniqueKey);
    }

    @GetMapping(value = "/stop/{pluginUniqueKey}")
    public void stop(@PathVariable("pluginUniqueKey") String pluginUniqueKey) {
        tenantPluginService.stop(pluginUniqueKey);
    }

    @GetMapping(value = "/config/{pluginUniqueKey}")
    public Object getTenantPluginConfig(@PathVariable("pluginUniqueKey") String pluginUniqueKey) {
        return tenantPluginService.get(pluginUniqueKey);
    }

    @PostMapping(value = "/config")
    public void config(@RequestBody TenantPluginConfigDTO tenantPluginConfigDTO) {
        tenantPluginService.config(tenantPluginConfigDTO);
    }
}
