package org.openwes.wes.basic.warehouse.domain.entity;

import lombok.Data;

@Data
public class WarehouseAreaGroup {

    private Long id;

    // unique identifier
    private String warehouseAreaGroupCode;
    private String warehouseAreaGroupName;

    private String remark;
    private String warehouseCode;

    private boolean enable;
    private boolean deleted;
    private Long deleteTime;

    private long version;

    public void disable() {
        this.enable = false;
    }

    public void enable() {
        this.enable = true;
    }

    public void delete() {
        this.deleted = true;
        this.deleteTime = System.currentTimeMillis();
    }
}
