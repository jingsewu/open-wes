package org.openwes.wes.api.basic.dto;

import org.openwes.wes.api.basic.constants.WarehouseAreaTypeEnum;
import org.openwes.wes.api.basic.constants.WarehouseAreaUseEnum;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class WarehouseAreaDTO implements Serializable {

    private Long id;

    @NotEmpty
    private String warehouseAreaCode;
    @NotEmpty
    private String warehouseCode;

    @NotEmpty
    private String warehouseAreaName;

    @NotEmpty
    private String warehouseGroupCode;

    @NotNull
    private WarehouseAreaTypeEnum warehouseAreaType;

    @NotNull
    private WarehouseAreaWorkTypeEnum warehouseAreaWorkType;

    @NotNull
    private WarehouseAreaUseEnum warehouseAreaUse;

    private int level;
    private int temperatureLimit;
    private int wetLimit;

    private boolean deleted;
    private boolean enable;

    private String remark;

    private long version;
}
