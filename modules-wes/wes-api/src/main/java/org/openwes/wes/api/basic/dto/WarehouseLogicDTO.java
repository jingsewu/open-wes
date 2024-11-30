package org.openwes.wes.api.basic.dto;

import lombok.Data;

@Data
public class WarehouseLogicDTO {
    private Long id;

    // union unique identifier
    private String warehouseLogicCode;
    private String warehouseAreaId;

    private String warehouseLogicName;

    private String warehouseCode;

    private String remark;

    private boolean deleted;
    private Long deleteTime;
    private boolean enable;

    private long version;
}
