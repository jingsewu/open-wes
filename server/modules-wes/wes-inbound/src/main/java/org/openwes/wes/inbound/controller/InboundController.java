package org.openwes.wes.inbound.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.inbound.IInboundPlanOrderApi;
import org.openwes.wes.api.inbound.dto.AcceptRecordDTO;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("inbound/plan")
@RequiredArgsConstructor
@Schema(description = "入库相关接口")
@Tag(name = "Wms Module Api")
@Validated
public class InboundController {

    private final IInboundPlanOrderApi inboundPlanOrderApi;

    @PostMapping("query/{identifyNo}/{warehouseCode}")
    @Operation(summary = "查询入库计划单", description = "根据传入的 LpnCode 或客户订单号查询入库计划单")
    public Response<InboundPlanOrderDTO> queryByLpnCodeOrCustomerOrderNo(@PathVariable String identifyNo, @PathVariable String warehouseCode) {
        InboundPlanOrderDTO inboundPlanOrderDTO = inboundPlanOrderApi.queryByLpnCodeOrCustomerOrderNoAndThrowException(identifyNo, warehouseCode);
        return Response.success(inboundPlanOrderDTO);
    }

    @PostMapping("accept")
    public void accept(@Valid @RequestBody AcceptRecordDTO acceptRecordDTO) {
        inboundPlanOrderApi.accept(acceptRecordDTO);
    }

    @PostMapping("forceCompleteAccept")
    public void forceCompleteAccept(@RequestParam("inboundPlanOrderId") Long inboundPlanOrderId) {
        inboundPlanOrderApi.forceCompleteAccept(inboundPlanOrderId);
    }

    @PostMapping("/close")
    public void close(@RequestBody Set<Long> inboundPlanOrderIds) {
        inboundPlanOrderApi.close(inboundPlanOrderIds);
    }

}
