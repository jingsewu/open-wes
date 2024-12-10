package org.openwes.wes.api.basic.dto;

import org.openwes.wes.api.config.dto.WarehouseMainDataConfigDTO;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WarehouseConfigDTO extends WarehouseMainDataConfigDTO {

    private Long id;

    @NotEmpty
    private String warehouseCode;

    private long version;
}
