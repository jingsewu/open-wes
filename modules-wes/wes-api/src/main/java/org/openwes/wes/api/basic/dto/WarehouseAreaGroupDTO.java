package org.openwes.wes.api.basic.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class WarehouseAreaGroupDTO {
    private Long id;

    // unique identifier
    @NotEmpty
    private String warehouseAreaGroupCode;
    @NotEmpty
    private String warehouseAreaGroupName;
    private String remark;

    @NotEmpty
    private String warehouseCode;

    private boolean deleted;
    private boolean enable;

    private long version;
}
