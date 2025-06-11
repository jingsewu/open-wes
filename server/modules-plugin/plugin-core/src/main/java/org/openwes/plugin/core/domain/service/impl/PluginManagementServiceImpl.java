package org.openwes.plugin.core.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.utils.ValidatorUtils;
import org.openwes.plugin.api.constants.PluginStatusEnum;
import org.openwes.plugin.api.dto.PluginDTO;
import org.openwes.plugin.core.domain.entity.Plugin;
import org.openwes.plugin.core.domain.repository.PluginRepository;
import org.openwes.plugin.core.domain.service.PluginManagementService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PluginManagementServiceImpl implements PluginManagementService {

    private final PluginRepository pluginRepository;

    @Override
    public void addPlugin(PluginDTO pluginDTO) {

        List<Plugin> list = getByCode(pluginDTO.getCode());
        if (CollectionUtils.isNotEmpty(list)
                && list.stream().anyMatch(u -> StringUtils.equals(u.getPluginVersion(), pluginDTO.getPluginVersion()))) {
            throw new CommonException("plugin code: " + pluginDTO.getCode() + ", and version: " + pluginDTO.getPluginVersion() + " exist.");
        }

        Plugin plugin = new Plugin();
        BeanUtils.copyProperties(pluginDTO, plugin);
        pluginRepository.save(plugin);
    }

    @Override
    public List<Plugin> getByCode(String code) {
        return pluginRepository.findByCode(code);
    }

    @Override
    public void approve(Long pluginId) {
        Plugin plugin = pluginRepository.findById(pluginId).orElseThrow();
        plugin.approve();
        pluginRepository.save(plugin);
    }

    @Override
    public void reject(Long pluginId) {
        Plugin plugin = pluginRepository.findById(pluginId).orElseThrow();
        plugin.reject();
        pluginRepository.save(plugin);
    }

    @Override
    public void publish(Long pluginId) {
        Plugin plugin = pluginRepository.findById(pluginId).orElseThrow();
        plugin.publish();
        pluginRepository.save(plugin);
    }

    @Override
    public void unpublish(Long pluginId) {
        Plugin plugin = pluginRepository.findById(pluginId).orElseThrow();
        plugin.unpublish();
        pluginRepository.save(plugin);
    }

    @Override
    public PluginDTO parseJarFile(String filePath) throws IOException {

        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setPluginStatus(PluginStatusEnum.NEW);

        try (JarFile jarFile = new JarFile(new File(filePath))) {
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
                }else if (Objects.equals(key, "plugin.scan-packages")) {
                    pluginDTO.setScanPackages(value);
                }
            }

            ValidatorUtils.validate(pluginDTO);

        } catch (IOException e) {
            log.error("parse jar file error and remove the file: {}", filePath, e);
            Files.delete(Path.of(filePath));
            throw e;
        }

        return pluginDTO;
    }

    @Override
    public String downloadPlugin(Long pluginId) {
        Plugin plugin = pluginRepository.findById(pluginId).orElseThrow();
        plugin.download();
        pluginRepository.save(plugin);
        return plugin.getJarFilePath();
    }

    @Override
    public void delete(Long pluginId) {
        Plugin plugin = pluginRepository.findById(pluginId).orElseThrow();
        plugin.delete();
        pluginRepository.save(plugin);
    }
}
