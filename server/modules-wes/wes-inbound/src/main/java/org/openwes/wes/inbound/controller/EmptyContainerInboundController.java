package org.openwes.wes.inbound.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.inbound.IEmptyContainerInboundOrderApi;
import org.openwes.wes.api.inbound.constants.EmptyContainerInboundWayEnum;
import org.openwes.wes.api.inbound.dto.EmptyContainerInboundRecordDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("inbound/empty/container")
@RequiredArgsConstructor
@Schema(description = "空箱入库相关接口")
@Tag(name = "Wms Module Api")
public class EmptyContainerInboundController {

    private final IEmptyContainerInboundOrderApi emptyContainerInboundOrderApi;

    @PostMapping("create")
    public Response create(@RequestParam String warehouseCode,
                           @RequestBody EmptyContainerInboundRecordDTO emptyContainerInboundRecordDTO) {
        emptyContainerInboundOrderApi.create(warehouseCode, EmptyContainerInboundWayEnum.SCAN,
                Collections.singletonList(emptyContainerInboundRecordDTO));
        return Response.success();
    }
}
