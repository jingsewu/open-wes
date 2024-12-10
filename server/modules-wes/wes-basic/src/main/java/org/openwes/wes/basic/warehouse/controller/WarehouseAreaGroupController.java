package org.openwes.wes.basic.warehouse.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.basic.IWarehouseAreaGroupApi;
import org.openwes.wes.api.basic.dto.WarehouseAreaGroupDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("basic/warehouse/areaGroup")
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class WarehouseAreaGroupController {

    private final IWarehouseAreaGroupApi warehouseAreaGroupApi;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid WarehouseAreaGroupDTO warehouseAreaGroupDTO) {
        if (warehouseAreaGroupDTO.getId() != null && warehouseAreaGroupDTO.getId() > 0) {
            warehouseAreaGroupApi.update(warehouseAreaGroupDTO);
            return Response.success();
        }
        warehouseAreaGroupApi.save(warehouseAreaGroupDTO);
        return Response.success();
    }

    @PostMapping("enable/{id}")
    public Object enable(@PathVariable Long id) {
        warehouseAreaGroupApi.enable(id);
        return Response.success();
    }

    @PostMapping("disable/{id}")
    public Object disable(@PathVariable Long id) {
        warehouseAreaGroupApi.disable(id);
        return Response.success();
    }

    @PostMapping("delete/{id}")
    public Object delete(@PathVariable Long id) {
        warehouseAreaGroupApi.delete(id);
        return Response.success();
    }
}
