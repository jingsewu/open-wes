package org.openwes.wes.api.stocktake.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "盘点单取消请求")
public class StocktakeOrderCancelDTO implements Serializable {
    @NotEmpty
    @Size(max = 64)
    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @NotEmpty
    @Schema(title = "盘点单编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> orderNos;
}
