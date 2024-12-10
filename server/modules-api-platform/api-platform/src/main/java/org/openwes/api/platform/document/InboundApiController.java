package org.openwes.api.platform.document;

import org.openwes.api.platform.api.dto.callback.wms.InboundPuttedDetailDTO;
import org.openwes.api.platform.document.dto.CreateInboundPlanOrderDTO;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.inbound.IInboundPlanOrderApi;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderCancelDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("api/inbound")
@RestController
@Tag(name = "Upstream Api")
@RequiredArgsConstructor
public class InboundApiController {

    private final IInboundPlanOrderApi iInboundPlanOrderApi;

    @PostMapping("create")
    @Operation(summary = "创建入库计划单", description = "根据传入的数据创建一个或多个入库计划单")
    public Response create(@RequestBody List<CreateInboundPlanOrderDTO> body) {
        return Response.success();
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消入库计划单", description = "根据传入的客户单号取消入库计划单")
    public Response<Object> cancel(@RequestBody InboundPlanOrderCancelDTO inboundPlanOrderCancelDTO) {
        return Response.success(iInboundPlanOrderApi.cancel(inboundPlanOrderCancelDTO.getIdentifyNos(), inboundPlanOrderCancelDTO.getWarehouseCode()));
    }

    @PostMapping("/callback")
    @Operation(summary = "上架完成回传", description = "入库计划单中所有的商品完成入库和上架操作后，调用此接口回传上游")
    public Response puttedAwayCallback(@RequestBody List<InboundPuttedDetailDTO> body) {
        return Response.success();
    }
}
