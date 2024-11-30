package org.openwes.wes.api.config.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WarehouseMainDataConfigDTO implements Serializable {

    private Long id;
    private Long workStationId;

    private boolean enablePutAwayRule = true;
    private boolean enableSkuWeighing = false;
    private boolean allowPutAwayOutside = false;

    private Boolean disableReceiving;
}
