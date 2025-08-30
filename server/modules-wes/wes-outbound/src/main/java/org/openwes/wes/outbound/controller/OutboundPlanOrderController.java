package org.openwes.wes.outbound.controller;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/outbound")
@RequiredArgsConstructor
@Validated
public class OutboundPlanOrderController {

    private final IOutboundPlanOrderApi outboundPlanOrderApi;

    @PostMapping("/order/create")
    @Operation(summary = "创建出库单")
    public void createOutboundPlanOrder(@Valid @RequestBody OutboundPlanOrderDTO outboundPlanOrderDTO) {
        outboundPlanOrderApi.createOutboundPlanOrder(Lists.newArrayList(outboundPlanOrderDTO));
    }

}
