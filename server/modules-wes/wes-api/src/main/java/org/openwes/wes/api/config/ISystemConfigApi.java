package org.openwes.wes.api.config;

import org.openwes.wes.api.config.dto.SystemConfigDTO;

public interface ISystemConfigApi {

    void save(SystemConfigDTO systemConfigDTO);

    SystemConfigDTO.InboundConfigDTO getInboundConfig();

    SystemConfigDTO.OutboundConfigDTO getOutboundConfig();

    SystemConfigDTO.EmsConfigDTO getEmsConfig();

    SystemConfigDTO.OutboundAlgoConfigDTO getOutboundAlgoConfig();

    SystemConfigDTO.BasicConfigDTO getBasicConfig();

    SystemConfigDTO getSystemConfig();
}
