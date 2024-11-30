package org.openwes.wes.api.basic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AisleDTO {

    private Long id;
    private String aisleCode;
    private Long warehouseAreaId;
    private boolean singleEntrance;
}
