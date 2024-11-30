package org.openwes.wes.basic.warehouse.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("basic/warehouse/area")
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class WarehouseAreaController {

    private final IWarehouseAreaApi warehouseAreaApi;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid WarehouseAreaDTO warehouseAreaDTO) {
        if (warehouseAreaDTO.getId() != null && warehouseAreaDTO.getId() > 0) {
            warehouseAreaApi.update(warehouseAreaDTO);
            return Response.success();
        }
        warehouseAreaApi.save(warehouseAreaDTO);
        return Response.success();
    }

    @PostMapping("enable/{id}")
    public Object enable(@PathVariable Long id) {
        warehouseAreaApi.enable(id);
        return Response.success();
    }

    @PostMapping("disable/{id}")
    public Object disable(@PathVariable Long id) {
        warehouseAreaApi.disable(id);
        return Response.success();
    }

    @PostMapping("delete/{id}")
    public Object delete(@PathVariable Long id) {
        warehouseAreaApi.delete(id);
        return Response.success();
    }
}
