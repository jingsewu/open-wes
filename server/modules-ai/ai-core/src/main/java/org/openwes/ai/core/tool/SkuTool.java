package org.openwes.ai.core.tool;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkuTool implements ITool {

    private final ISkuMainDataApi skuMainDataApi;

    @Tool(
            name = "getSkuMainDataDTOs",
            description = "根据商品编码(skuCode)获取指定仓库(warehouseCode)的SKU主数据列表，自动过滤匹配仓库编码的数据"
    )
    public List<SkuMainDataDTO> getSkuMainDataDTOs(String skuCode, String warehouseCode) {
        return skuMainDataApi.findSkuMainDataBySkuCode(skuCode)
                .stream().filter(skuMainDataDTO -> skuMainDataDTO.getWarehouseCode().equals(warehouseCode))
                .toList();
    }
}
