package org.openwes.plugin.core.controller;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openwes.common.utils.http.Response;
import org.openwes.plugin.api.constants.ApplicationPluginStatusEnum;
import org.openwes.plugin.api.constants.PluginStatusEnum;
import org.openwes.plugin.api.dto.PluginDTO;
import org.openwes.plugin.core.domain.entity.Plugin;
import org.openwes.plugin.core.domain.repository.PluginRepository;
import org.openwes.plugin.core.domain.service.PluginManagementService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/pluginManage")
public class PluginManageController {

    private final PluginManagementService pluginService;
    private final PluginRepository pluginRepository;

    @GetMapping(value = "/listAll")
    public Response<List<Plugin>> listAll() {
        List<Plugin> plugins = pluginRepository.findAll();
        return Response.success(plugins);
    }

    @GetMapping(value = "/storeQuery")
    public Object storeQuery() {
        HashMap<@Nullable String, @Nullable Object> result = Maps.newHashMap();
        result.put("pluginStore", pluginRepository.findAllByPluginStatus(PluginStatusEnum.PUBLISHED));
        return result;
    }

    @PostMapping(value = "/uploadPlugin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadPlugin(
            @RequestPart("pluginDTO") PluginDTO pluginDTO,
            @RequestPart("file") MultipartFile file) throws IOException {

        // Save the uploaded file to local storage
        String filePath = pluginService.saveFileToLocalStorage(file);

        // Parse JAR file and populate pluginDTO (assuming this is needed)
        pluginService.parseJarFile(filePath, pluginDTO);

        // Update DTO with the local file path
        pluginDTO.setJarFilePath(filePath);

        // Save the plugin data
        pluginService.addPlugin(pluginDTO);
    }

    @GetMapping("/approve/{pluginId}")
    public void approve(@PathVariable("pluginId") Long pluginId) {
        pluginService.approve(pluginId);
    }

    @GetMapping("/reject/{pluginId}")
    public void reject(@PathVariable("pluginId") Long pluginId) {
        pluginService.reject(pluginId);
    }

    @GetMapping("/publish/{pluginId}")
    public void publish(@PathVariable("pluginId") Long pluginId) {
        pluginService.publish(pluginId);
    }

    @GetMapping("/unpublish/{pluginId}")
    public void unpublish(@PathVariable("pluginId") Long pluginId) {
        pluginService.unpublish(pluginId);
    }

    @GetMapping("/delete/{pluginId}")
    public Response<String> delete(@PathVariable("pluginId") Long pluginId) {
        pluginService.delete(pluginId);
        return Response.success();
    }
}
