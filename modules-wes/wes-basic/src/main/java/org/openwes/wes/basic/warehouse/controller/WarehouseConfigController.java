package org.openwes.wes.basic.warehouse.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.basic.IWarehouseConfigApi;
import org.openwes.wes.api.basic.dto.WarehouseConfigDTO;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseConfig;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseConfigRepository;
import org.openwes.wes.basic.warehouse.domain.transfer.WarehouseConfigTransfer;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("basic/warehouse/config")
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class WarehouseConfigController {

    private final IWarehouseConfigApi warehouseConfigApi;
    private final WarehouseConfigRepository warehouseConfigRepository;
    private final WarehouseConfigTransfer warehouseConfigTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid WarehouseConfigDTO warehouseConfigDTO) {
        if (warehouseConfigDTO.getId() != null && warehouseConfigDTO.getId() > 0) {
            warehouseConfigApi.update(warehouseConfigDTO);
            return Response.success();
        }
        warehouseConfigApi.save(warehouseConfigDTO);
        return Response.success();
    }

    @PostMapping("get/{warehouseCode}")
    public Object get(@PathVariable("warehouseCode") String warehouseCode) {
        WarehouseConfig warehouseConfig = warehouseConfigRepository.findByWarehouseCode(warehouseCode);
        return Response.builder().data(warehouseConfigTransfer.toDTO(warehouseConfig)).build();
    }
}
