package org.openwes.wes.basic.warehouse.domain.entity;

import lombok.Data;

@Data
public class WarehouseLogic {

    private Long id;

    private String warehouseCode;
    private Long warehouseAreaId;
    private String warehouseLogicCode;
    private String warehouseLogicName;

    private String remark;

    private boolean deleted;
    private Long deleteTime;
    private boolean enable;

    private long version;

    public void enable() {
        this.enable = true;
    }

    public void disable() {
        this.enable = false;
    }

    public void delete() {
        this.deleted = true;
        this.deleteTime = System.currentTimeMillis();
    }
}
