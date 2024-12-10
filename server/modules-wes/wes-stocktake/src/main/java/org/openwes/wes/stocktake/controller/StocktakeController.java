package org.openwes.wes.stocktake.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.stocktake.IStocktakeApi;
import org.openwes.wes.api.stocktake.dto.StocktakeOrderCancelDTO;
import org.openwes.wes.api.stocktake.dto.StocktakeOrderCreateDTO;
import org.openwes.wes.api.stocktake.dto.StocktakeOrderExecuteDTO;
import org.openwes.wes.api.stocktake.dto.StocktakeOrderReceiveDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stocktake")
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class StocktakeController {

    private final IStocktakeApi stocktakeApi;

    @PostMapping("/order/create")
    public Response<Object> createStocktakeOrder(@RequestBody StocktakeOrderCreateDTO stocktakeOrderCreateDTO) {
        stocktakeApi.createStocktakeOrder(stocktakeOrderCreateDTO);
        return Response.success();
    }

    @PostMapping("/order/execute")
    public Response<Object> executeStocktakeOrder(@RequestBody StocktakeOrderExecuteDTO stocktakeOrderExecuteDTO) {
        stocktakeApi.executeStocktakeOrder(stocktakeOrderExecuteDTO);
        return Response.success();
    }

    @PostMapping("/order/cancel")
    public Response<List<String>> cancelStocktakeOrder(@RequestBody StocktakeOrderCancelDTO stocktakeOrderCancelDTO) {
        List<String> cancelledOrderNos = stocktakeApi.cancelStocktakeOrder(stocktakeOrderCancelDTO);
        return Response.success(cancelledOrderNos);
    }

    @PostMapping("/order/receive")
    public Response<Object> receiveStocktakeOrder(@Valid @RequestBody StocktakeOrderReceiveDTO stocktakeOrderReceiveDTO) {
        stocktakeApi.receiveStocktakeOrder(stocktakeOrderReceiveDTO.getStocktakeTaskIds(), stocktakeOrderReceiveDTO.getWorkStationId());
        return Response.success();
    }
}
