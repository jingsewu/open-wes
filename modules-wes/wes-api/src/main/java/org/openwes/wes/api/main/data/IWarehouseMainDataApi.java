package org.openwes.wes.api.main.data;


import org.openwes.wes.api.main.data.dto.WarehouseMainDataDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.Collection;

public interface IWarehouseMainDataApi {

    void createWarehouse(@Valid WarehouseMainDataDTO warehouseDTO);

    void updateWarehouse(@Valid WarehouseMainDataDTO warehouseDTO);

    WarehouseMainDataDTO getWarehouse(@NotEmpty String warehouseCode);

    Collection<WarehouseMainDataDTO> getWarehouses(@NotEmpty Collection<String> warehouseCodes);

}
