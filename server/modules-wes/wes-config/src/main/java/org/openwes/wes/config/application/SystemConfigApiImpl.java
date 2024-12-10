package org.openwes.wes.config.application;

import org.openwes.wes.api.config.ISystemConfigApi;
import org.openwes.wes.api.config.dto.SystemConfigDTO;
import org.openwes.wes.config.domain.entity.SystemConfig;
import org.openwes.wes.config.domain.repository.SystemConfigRepository;
import org.openwes.wes.config.domain.transfer.SystemConfigTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


@Validated
@RequiredArgsConstructor
@Service
public class SystemConfigApiImpl implements ISystemConfigApi {

    private final SystemConfigRepository systemConfigRepository;
    private final SystemConfigTransfer systemConfigTransfer;

    @Override
    public void save(SystemConfigDTO systemConfigDTO) {
        systemConfigRepository.save(systemConfigTransfer.toDO(systemConfigDTO));
    }

    @Override
    public SystemConfigDTO.InboundConfigDTO getInboundConfig() {

        SystemConfig systemConfig = systemConfigRepository.findSystemConfig();
        if (systemConfig == null || systemConfig.getInboundConfig() == null) {
            return new SystemConfigDTO.InboundConfigDTO();
        }

        return systemConfig.getInboundConfig();
    }

    @Override
    public SystemConfigDTO.OutboundConfigDTO getOutboundConfig() {
        SystemConfig systemConfig = systemConfigRepository.findSystemConfig();
        if (systemConfig == null || systemConfig.getOutboundConfig() == null) {
            return new SystemConfigDTO.OutboundConfigDTO();
        }

        return systemConfig.getOutboundConfig();
    }

    @Override
    public SystemConfigDTO.EmsConfigDTO getEmsConfig() {
        SystemConfig systemConfig = systemConfigRepository.findSystemConfig();
        if (systemConfig == null || systemConfig.getEmsConfig() == null) {
            return new SystemConfigDTO.EmsConfigDTO();
        }

        return systemConfig.getEmsConfig();
    }

    @Override
    public SystemConfigDTO.OutboundAlgoConfigDTO getOutboundAlgoConfig() {
        SystemConfig systemConfig = systemConfigRepository.findSystemConfig();
        if (systemConfig == null || systemConfig.getOutboundAlgoConfig() == null) {
            return new SystemConfigDTO.OutboundAlgoConfigDTO();
        }

        return systemConfig.getOutboundAlgoConfig();
    }

    @Override
    public SystemConfigDTO.BasicConfigDTO getBasicConfig() {
        SystemConfig systemConfig = systemConfigRepository.findSystemConfig();
        if (systemConfig == null || systemConfig.getBasicConfig() == null) {
            return new SystemConfigDTO.BasicConfigDTO();
        }

        return systemConfig.getBasicConfig();
    }

    @Override
    public SystemConfigDTO getSystemConfig() {
        SystemConfig systemConfig = systemConfigRepository.findSystemConfig();
        return systemConfigTransfer.toDTO(systemConfig);
    }
}
