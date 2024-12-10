package org.openwes.wes.config.domain.entity;

import org.openwes.common.utils.base.UpdateUserDTO;
import org.openwes.wes.api.config.dto.SystemConfigDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SystemConfig extends UpdateUserDTO {

    private Long id;
    private SystemConfigDTO.BasicConfigDTO basicConfig;
    private SystemConfigDTO.EmsConfigDTO emsConfig;
    private SystemConfigDTO.InboundConfigDTO inboundConfig;
    private SystemConfigDTO.OutboundConfigDTO outboundConfig;
    private SystemConfigDTO.OutboundAlgoConfigDTO outboundAlgoConfig;

    private Long version;
}
