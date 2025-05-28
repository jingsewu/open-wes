package org.openwes.plugin.sdk.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.plugin.api.dto.PluginConfigDTO;
import org.openwes.plugin.sdk.domain.service.PluginService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin")
@Tag(name = "Plugin Module Api")
public class PluginController {

    private final PluginService pluginService;

    @GetMapping(value = "install")
    public Response<Object> install(@RequestPart("file") MultipartFile file) throws IOException {

        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".jar");
        file.transferTo(tempFile);
        try {
            pluginService.install(tempFile.getPath());
        } finally {
            tempFile.deleteOnExit();
        }

        return Response.success();
    }

    @GetMapping(value = "/start/{pluginUniqueKey}")
    public void start(@PathVariable("pluginUniqueKey") String pluginUniqueKey) {
        pluginService.start(pluginUniqueKey);
    }

    @GetMapping(value = "/stop/{pluginUniqueKey}")
    public void stop(@PathVariable("pluginUniqueKey") String pluginUniqueKey) {
        pluginService.stop(pluginUniqueKey);
    }

    @GetMapping(value = "/config/{pluginUniqueKey}")
    public Object getTenantPluginConfig(@PathVariable("pluginUniqueKey") String pluginUniqueKey) {
        return pluginService.get(pluginUniqueKey);
    }

    @PostMapping(value = "/config")
    public void config(@RequestBody PluginConfigDTO tenantPluginConfigDTO) {
        pluginService.config(tenantPluginConfigDTO);
    }
}
