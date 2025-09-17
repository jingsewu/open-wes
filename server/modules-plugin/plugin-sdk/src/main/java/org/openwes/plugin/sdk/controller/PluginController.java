package org.openwes.plugin.sdk.controller;

import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.common.utils.file.FileUtils;
import org.openwes.common.utils.http.Response;
import org.openwes.plugin.api.dto.PluginConfigDTO;
import org.openwes.plugin.extension.config.IExtensionConfig;
import org.openwes.plugin.sdk.service.PluginService;
import org.openwes.plugin.sdk.utils.ConfigMetadataExtractor;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin")
@Tag(name = "Plugin Module Api")
public class PluginController {

    private final PluginService pluginService;
    private final PluginManager pluginManager;

    @Value("${pf4j.plugin.jar.dir:plugins/jar}")
    private String pluginDir;

    @PostMapping(value = "install", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<Object> install(@RequestPart("file") MultipartFile file) throws IOException {

        String filePath = FileUtils.saveFile(file, pluginDir);

        try {
            pluginService.install(filePath);
        } catch (Exception e) {
            Files.deleteIfExists(Paths.get(filePath));
            throw new RuntimeException(e);
        }

        return Response.success();
    }

    @GetMapping(value = "/list")
    public List<Map<String, Object>> list() {
        Map<String, Object> result = Maps.newHashMap();
        List<PluginWrapper> plugins = pluginManager.getPlugins();
        return plugins.stream().map(v -> {
            result.put("pluginId", v.getPluginId());
            result.put("pluginPath", v.getPluginPath().toString());
            result.put("pluginStatus", v.getPluginState());
            return result;
        }).toList();
    }

    @GetMapping(value = "/start/{pluginUniqueKey}")
    public void start(@PathVariable("pluginUniqueKey") String pluginUniqueKey) {
        pluginService.start(pluginUniqueKey);
    }

    @GetMapping(value = "/stop/{pluginUniqueKey}")
    public void stop(@PathVariable("pluginUniqueKey") String pluginUniqueKey) {
        pluginService.stop(pluginUniqueKey);
    }

    @GetMapping(value = "/uninstall/{pluginUniqueKey}")
    public void uninstall(@PathVariable("pluginUniqueKey") String pluginUniqueKey) throws IOException {
        pluginService.uninstall(pluginUniqueKey);
    }

    @GetMapping(value = "/config/{pluginUniqueKey}")
    public Object getTenantPluginConfig(@PathVariable("pluginUniqueKey") String pluginUniqueKey) {
        return pluginService.get(pluginUniqueKey);
    }

    @PostMapping(value = "/config")
    public void config(@RequestBody PluginConfigDTO tenantPluginConfigDTO) {
        pluginService.config(tenantPluginConfigDTO);
    }

    @GetMapping(value = "getConfigPageMetaData/{pluginUniqueKey}")
    public List<ConfigMetadataExtractor.ConfigFieldMetadata> getConfigPageMetaData(@PathVariable("pluginUniqueKey") String pluginUniqueKey) {

        List<Class<? extends IExtensionConfig>> extensionClasses = pluginManager.getExtensionClasses(IExtensionConfig.class, pluginUniqueKey);

        if (ObjectUtils.isEmpty(extensionClasses)) {
            return null;
        }

        return ConfigMetadataExtractor.extractMetadata(extensionClasses.iterator().next());
    }
}
