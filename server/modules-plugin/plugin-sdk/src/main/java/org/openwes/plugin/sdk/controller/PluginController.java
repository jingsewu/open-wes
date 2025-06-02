package org.openwes.plugin.sdk.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.file.FileUtils;
import org.openwes.common.utils.http.Response;
import org.openwes.plugin.api.dto.PluginConfigDTO;
import org.openwes.plugin.sdk.domain.entity.ApplicationPlugin;
import org.openwes.plugin.sdk.domain.service.PluginService;
import org.pf4j.AbstractPluginManager;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin")
@Tag(name = "Plugin Module Api")
public class PluginController {

    private final PluginService pluginService;

    @PostMapping(value = "install", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<Object> install(@RequestPart("file") MultipartFile file) throws IOException {

        String filePath = FileUtils.saveFile(file, AbstractPluginManager.DEFAULT_PLUGINS_DIR);

        try {
            pluginService.install(filePath);
        } catch (Exception e) {
            Files.deleteIfExists(Paths.get(filePath));
            throw new RuntimeException(e);
        }

        return Response.success();
    }

    @GetMapping(value = "/list")
    public List<ApplicationPlugin> list() {
        return pluginService.list();
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
