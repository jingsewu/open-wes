package org.openwes.wes.stock.controller;

import org.openwes.wes.api.stock.IStockAbnormalRecordApi;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("stock/abnormal/record")
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class StockAbnormalRecordController {

    private final IStockAbnormalRecordApi stockAbnormalRecordApi;

    @PostMapping("createAdjustmentOrder")
    public void createAdjustmentOrder(@RequestBody @Valid List<Long> ids) {
        stockAbnormalRecordApi.createAdjustmentOrder(ids);
    }

    @PostMapping("manualClose")
    public void manualClose(@RequestBody @Valid List<Long> ids) {
        stockAbnormalRecordApi.manualClose(ids);
    }

    @PostMapping("createRecheckOrder")
    public void createRecheckOrder(@RequestBody @Valid List<Long> ids) {
        stockAbnormalRecordApi.createRecheckOrder(ids);
    }
}
