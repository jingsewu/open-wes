package org.openwes.wes.basic.warehouse.domain.entity;

import org.openwes.wes.api.config.dto.WarehouseMainDataConfigDTO;
import lombok.Data;

@Data
public class WarehouseConfig {

    private Long id;
    private String warehouseCode;
    private WarehouseMainDataConfigDTO warehouseMainDataConfig;

    private long version;
}
