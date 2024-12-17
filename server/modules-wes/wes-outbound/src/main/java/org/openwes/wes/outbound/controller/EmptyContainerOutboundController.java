package org.openwes.wes.outbound.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.outbound.IEmptyContainerOutboundOrderApi;
import org.openwes.wes.api.outbound.dto.EmptyContainerOutboundOrderCreateDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("outbound/empty/container")
@RequiredArgsConstructor
@Schema(description = "空箱出库相关接口")
@Tag(name = "Wms Module Api")
public class EmptyContainerOutboundController {

    private final IEmptyContainerOutboundOrderApi emptyContainerOutboundOrderApi;

    @PostMapping("create")
    public void create(EmptyContainerOutboundOrderCreateDTO createDTO) {
        emptyContainerOutboundOrderApi.createEmptyContainerOutboundOrder(createDTO);
    }
}
