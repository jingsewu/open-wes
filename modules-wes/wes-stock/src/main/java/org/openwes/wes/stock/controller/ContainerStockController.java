package org.openwes.wes.stock.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.stock.controller.parameter.FreezeStockVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("stock/container/stock")
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class ContainerStockController {

    private final IStockApi iStockApi;

    @PostMapping("freezeContainerStock")
    public Object freezeContainerStock(@RequestBody @Valid FreezeStockVO freezeStockVO) {
        iStockApi.freezeContainerStock(freezeStockVO.getId(), freezeStockVO.getQty());
        return Response.success();
    }

    @PostMapping("unfreezeContainerStock")
    public Object unfreezeContainerStock(@RequestBody @Valid FreezeStockVO freezeStockVO) {
        iStockApi.unfreezeContainerStock(freezeStockVO.getId(), freezeStockVO.getQty());
        return Response.success();
    }
}
