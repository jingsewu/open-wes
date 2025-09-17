package org.openwes.plugin.core.domain.service;

import jakarta.validation.Valid;
import org.openwes.plugin.api.dto.PluginDTO;
import org.openwes.plugin.core.domain.entity.Plugin;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Validated
public interface PluginManagementService {

    void addPlugin(@Valid PluginDTO pluginDTO) throws IOException;

    List<Plugin> getByCode(String code);

    void approve(Long pluginId);

    void delete(Long pluginId);

    void reject(Long pluginId);

    void publish(Long pluginId);

    void unpublish(Long pluginId);

    PluginDTO parseJarFile(String filePath) throws IOException;

    String downloadPlugin(Long pluginId);
}
