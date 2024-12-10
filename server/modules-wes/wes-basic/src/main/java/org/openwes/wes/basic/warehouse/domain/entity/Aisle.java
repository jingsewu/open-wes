package org.openwes.wes.basic.warehouse.domain.entity;

import lombok.Data;

@Data
public class Aisle {

    private Long id;
    private String aisleCode;
    private Long warehouseAreaId;
    private boolean singleEntrance;
}
