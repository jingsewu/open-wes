package org.openwes.wes.ems.proxy.domain.entity;

import org.openwes.wes.api.ems.proxy.dto.EmsLocationConfigDTO;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class EmsLocationConfig {

    private Long id;
    private String locationCode;
    private Long warehouseAreaId;
    private String warehouseCode;

    private EmsLocationConfigDTO.LocationType locationType;


}
