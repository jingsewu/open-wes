package org.openwes.plugin.core.controller;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openwes.common.utils.http.Response;
import org.openwes.common.utils.user.AuthConstants;
import org.openwes.distribute.file.client.FastdfsClient;
import org.openwes.mq.MqClient;
import org.openwes.plugin.api.constants.PluginManageTypeEnum;
import org.openwes.plugin.api.constants.PluginStatusEnum;
import org.openwes.plugin.api.constants.TenantPluginStatusEnum;
import org.openwes.plugin.api.constants.TopicConstants;
import org.openwes.plugin.api.dto.PluginDTO;
import org.openwes.plugin.api.dto.PluginManageDTO;
import org.openwes.plugin.core.domain.entity.Plugin;
import org.openwes.plugin.core.domain.entity.TenantPlugin;
import org.openwes.plugin.core.domain.repository.PluginRepository;
import org.openwes.plugin.core.domain.repository.TenantPluginRepository;
import org.openwes.plugin.core.domain.service.PluginManagementService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/pluginManage")
public class PluginManageController {

    private final PluginManagementService pluginService;
    private final PluginRepository pluginRepository;
    private final TenantPluginRepository tenantInstallPluginRepository;
    private final MqClient mqClient;
    private final FastdfsClient fastdfsClient;

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

    @PostMapping(value = "uploadFile", consumes = {"multipart/form-data", "application/java-archive"})
    public String uploadFile(@RequestPart("file") MultipartFile file) throws IOException {
        return fastdfsClient.updateFile(file.getInputStream(), file.getSize(), file.getName(), null);
    }

    @PostMapping(value = "addPlugin", consumes = {"multipart/form-data", "application/java-archive"})
    public void addPlugin(PluginDTO pluginDTO,
                          @RequestPart(value = "jarFile") MultipartFile multipartFile) throws IOException {

        String jarFilePath = fastdfsClient.updateFile(multipartFile.getInputStream(), multipartFile.getSize(), multipartFile.getName(), null);

        parseJarFile(multipartFile, pluginDTO);

        pluginDTO.setJarFilePath(jarFilePath);

        pluginService.addPlugin(pluginDTO);
    }

    @GetMapping(value = "/install/{pluginId}")
    public Response<Object> install(@RequestHeader(AuthConstants.HEADER_TENANT_ID) String tenant,
                                    @PathVariable Long pluginId) {

        Plugin plugin = pluginRepository.findById(pluginId).orElseThrow();

        //save tenant install plugin record
        TenantPlugin tenantInstallPlugin = TenantPlugin.builder()
                .tenantName(tenant)
                .pluginUniqueKey(plugin.getPluginUniqueKey())
                .pluginId(plugin.getId())
                .status(TenantPluginStatusEnum.STARTED)
                .build();
        tenantInstallPluginRepository.save(tenantInstallPlugin);

        PluginManageDTO pluginManageDTO = PluginManageDTO.builder().pluginId(plugin.getId())
                .pluginUniqueKey(plugin.getPluginUniqueKey())
                .pluginManageType(PluginManageTypeEnum.INSTALL)
                .pluginJarPath(plugin.getJarFilePath())
                .version(plugin.getPluginVersion())
                .build();
        mqClient.sendMessage(TopicConstants.TOPIC_PLUGIN_LISTEN_PLUGIN_MODIFY, pluginManageDTO);
        return Response.success();
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

    private void parseJarFile(MultipartFile multipartFile, PluginDTO pluginDTO) throws IOException {
        File tempFile = File.createTempFile("temp", ".jar");
        multipartFile.transferTo(tempFile);
        try (JarFile jarFile = new JarFile(tempFile)) {
            JarEntry jarEntry = jarFile.getJarEntry("plugin.properties");
            if (jarEntry == null) {
                throw new IllegalArgumentException("plugin.properties is not exits.");
            }

            List<String> lines = IOUtils.readLines(jarFile.getInputStream(jarEntry), Charset.defaultCharset());
            for (String line : lines) {
                String[] splits = line.split("=");
                if (StringUtils.isEmpty(line) || splits.length < 2) {
                    continue;
                }
                String key = splits[0];
                String value = splits[1];
                if (Objects.equals(key, "plugin.id")) {
                    pluginDTO.setPluginUniqueKey(value);
                } else if (Objects.equals(key, "plugin.code")) {
                    pluginDTO.setCode(value);
                } else if (Objects.equals(key, "plugin.name")) {
                    pluginDTO.setName(value);
                } else if (Objects.equals(key, "plugin.provider")) {
                    pluginDTO.setDeveloper(value);
                } else if (Objects.equals(key, "plugin.version")) {
                    pluginDTO.setPluginVersion(value);
                } else if (Objects.equals(key, "plugin.description")) {
                    pluginDTO.setDescription(value);
                } else if (Objects.equals(key, "plugin.dependencies")) {
                    pluginDTO.setDependencies(value);
                }
            }

        } catch (IOException e) {
            log.error("parse jar file error:", e);
            throw e;
        } finally {
            Files.delete(Path.of(tempFile.getPath()));
        }
    }
}
