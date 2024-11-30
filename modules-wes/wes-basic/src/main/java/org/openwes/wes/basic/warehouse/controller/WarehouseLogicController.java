package org.openwes.wes.basic.warehouse.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.basic.IWarehouseLogicApi;
import org.openwes.wes.api.basic.dto.WarehouseLogicDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("basic/warehouse/areaLogic")
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class WarehouseLogicController {

    private final IWarehouseLogicApi warehouseLogicApi;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid WarehouseLogicDTO warehouseLogicDTO) {
        if (warehouseLogicDTO.getId() != null && warehouseLogicDTO.getId() > 0) {
            warehouseLogicApi.update(warehouseLogicDTO);
            return Response.success();
        }
        warehouseLogicApi.save(warehouseLogicDTO);
        return Response.success();
    }

    @PostMapping("enable/{id}")
    public Object enable(@PathVariable Long id) {
        warehouseLogicApi.enable(id);
        return Response.success();
    }

    @PostMapping("disable/{id}")
    public Object disable(@PathVariable Long id) {
        warehouseLogicApi.disable(id);
        return Response.success();
    }

    @PostMapping("delete/{id}")
    public Object delete(@PathVariable Long id) {
        warehouseLogicApi.delete(id);
        return Response.success();
    }
}
