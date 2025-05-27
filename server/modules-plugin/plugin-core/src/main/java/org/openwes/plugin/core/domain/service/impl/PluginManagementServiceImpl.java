package org.openwes.plugin.core.domain.service.impl;

import org.openwes.common.utils.exception.CommonException;
import org.openwes.plugin.api.dto.PluginDTO;
import org.openwes.plugin.core.domain.entity.Plugin;
import org.openwes.plugin.core.domain.repository.PluginRepository;
import org.openwes.plugin.core.domain.service.PluginManagementService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
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
    public void delete(Long pluginId) {
        Plugin plugin = pluginRepository.findById(pluginId).orElseThrow();
        plugin.delete();
        pluginRepository.save(plugin);
    }
}
