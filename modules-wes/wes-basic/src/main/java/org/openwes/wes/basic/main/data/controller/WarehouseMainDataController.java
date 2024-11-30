package org.openwes.wes.basic.main.data.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.main.data.IWarehouseMainDataApi;
import org.openwes.wes.api.main.data.dto.WarehouseMainDataDTO;
import org.openwes.wes.basic.main.data.domain.entity.WarehouseMainData;
import org.openwes.wes.basic.main.data.domain.repository.WarehouseMainDataRepository;
import org.openwes.wes.basic.main.data.domain.transfer.WarehouseMainDataTransfer;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("basic/warehouse")
@Tag(name = "Wms Module Api")
public class WarehouseMainDataController {

    private final IWarehouseMainDataApi warehouseMainDataApi;
    private final WarehouseMainDataRepository warehouseMainDataRepository;
    private final WarehouseMainDataTransfer warehouseMainDataTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid WarehouseMainDataDTO warehouseMainDataDTO) {
        if (warehouseMainDataDTO.getId() != null && warehouseMainDataDTO.getId() > 0) {
            warehouseMainDataApi.updateWarehouse(warehouseMainDataDTO);
            return Response.success();
        }
        warehouseMainDataApi.createWarehouse(warehouseMainDataDTO);
        return Response.success();
    }

    @PostMapping("get/{id}")
    public Object getById(@PathVariable Long id) {
        WarehouseMainData warehouseMainData = warehouseMainDataRepository.findById(id);
        return warehouseMainDataTransfer.toDTO(warehouseMainData);
    }
}
