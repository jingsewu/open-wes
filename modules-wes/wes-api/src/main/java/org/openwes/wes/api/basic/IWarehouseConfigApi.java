package org.openwes.wes.api.basic;

import org.openwes.wes.api.basic.dto.WarehouseConfigDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public interface IWarehouseConfigApi {

    void save(@Valid WarehouseConfigDTO warehouseConfigDTO);

    void update(@Valid WarehouseConfigDTO warehouseConfigDTO);

    WarehouseConfigDTO getWarehouseConfig(@NotEmpty String warehouseCode);
}
