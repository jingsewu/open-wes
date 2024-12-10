package org.openwes.wes.basic.warehouse.controller.parameter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AisleLocationQueryVO {
    @NotNull
    private Long warehouseAreaId;

    @NotEmpty
    private String aisleCode;
}
