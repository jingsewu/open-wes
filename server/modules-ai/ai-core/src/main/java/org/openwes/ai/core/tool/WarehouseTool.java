package org.openwes.ai.core.tool;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseTool implements ITool {

    private final IWarehouseAreaApi warehouseAreaApi;

    @Tool(
            name = "getWarehouseArea",
            description = "This tool retrieves the details of a specific warehouse area based on the warehouse code and the warehouse area code. It first fetches all warehouse areas for the given warehouse code, then filters the list to find the matching warehouse area by its code. If no match is found, an exception is thrown."
    )
    public WarehouseAreaDTO getWarehouseArea(String warehouseCode, String warehouseAreaCode) {
        List<WarehouseAreaDTO> warehouseAreaDTOS = warehouseAreaApi.getByWarehouseCode(warehouseCode);
        return warehouseAreaDTOS.stream().filter(warehouseAreaDTO -> warehouseAreaDTO.getWarehouseAreaCode()
                        .equals(warehouseAreaCode))
                .findFirst().orElseThrow();
    }

}
