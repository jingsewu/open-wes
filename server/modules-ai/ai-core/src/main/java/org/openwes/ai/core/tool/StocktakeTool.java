package org.openwes.ai.core.tool;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.stocktake.IStocktakeApi;
import org.openwes.wes.api.stocktake.dto.StocktakeOrderCreateDTO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StocktakeTool implements ITool {

    private final IStocktakeApi stocktakeApi;

    @Tool(
            name = "createStocktake",
            description = "创建仓库盘点单，需提供仓库编码、盘点类型、方式等核心参数。当选择容器/货架/SKU/库存作为盘点单位时，必须提供对应的编码列表。包含零库存和空格口等可选配置项[3,4](@ref)"
    )
    public void create(StocktakeOrderCreateDTO stocktakeOrderCreateDTO) {
        stocktakeApi.createStocktakeOrder(stocktakeOrderCreateDTO);
    }
}
