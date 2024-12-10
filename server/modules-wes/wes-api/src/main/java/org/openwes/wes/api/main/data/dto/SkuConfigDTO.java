package org.openwes.wes.api.main.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商品配置信息")
public class SkuConfigDTO implements Serializable {

    @Schema(title = "是否启用唯一码管理", description = "true 启用，false 禁用")
    private boolean enableSn;

    @Schema(title = "是否启用效期管理", description = "true 启用，false 禁用")
    private boolean enableEffective;

    @Schema(title = "保质期")
    private Integer shelfLife;

    @Schema(title = "效期限制")
    private Integer effectiveDays;

    @Schema(title = "条码规则")
    private String barcodeRuleCode;

    @Schema(title = "热度")
    private String heat;

    @Schema(title = "库存上限")
    private Integer maxStock;

    @Schema(title = "库存下限")
    private Integer minStock;

    @Schema(title = "是否计算热度", description = "true 是，false 否")
    private boolean calculateHeat;

    @Schema(title = "是否无码商品", description = "true 是，false 否")
    private boolean noBarcode;

}
