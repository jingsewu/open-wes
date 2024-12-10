package org.openwes.api.platform.document;


import org.openwes.api.platform.api.dto.callback.wms.ContainerSealedDetailDTO;
import org.openwes.api.platform.api.dto.callback.wms.OutboundPickedDetailDTO;
import org.openwes.api.platform.document.dto.CreateOutboundPlanOrderDTO;
import org.openwes.common.utils.http.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("api/outbound")
@RestController
@Tag(name = "Upstream Api")
public class OutboundApiController {

    @PostMapping("create")
    @Operation(summary = "创建出库计划单", description = "根据传入的数据创建一个或多个出库计划单")
    public Response create(@RequestBody List<CreateOutboundPlanOrderDTO> body) {
        return Response.success();
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消出库计划单", description = "根据传入的客户单号取消出库计划单")
    public Response cancel(@RequestBody List<String> customerOrderNos) {
        return Response.success();
    }

    @PostMapping("/sealCallback")
    @Operation(summary = "封箱回传", description = "根据传入的客户单号取消入库计划单")
    public Response sealCallback(@RequestBody List<ContainerSealedDetailDTO> body) {
        return Response.success();
    }

    @PostMapping("/pickedCallback")
    @Operation(summary = "拣货完成回传", description = "当整个订单完成拣选的时候，会通过这个接口通知 wms")
    public Response pickedCallback(@RequestBody List<OutboundPickedDetailDTO> body) {
        return Response.success();
    }
}
