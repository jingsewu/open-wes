package org.openwes.wes.stock.controller;

import org.openwes.wes.api.stock.IStockAdjustmentApi;
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
@RequestMapping("stock/adjustment")
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class StockAdjustmentController {

    private final IStockAdjustmentApi stockAdjustmentApi;

    @PostMapping("adjust")
    public void adjust(@RequestBody @Valid List<Long> ids) {
        stockAdjustmentApi.adjust(ids);
    }

    @PostMapping("close")
    public void close(@RequestBody @Valid List<Long> ids) {
        stockAdjustmentApi.close(ids);
    }
}
