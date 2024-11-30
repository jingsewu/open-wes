package org.openwes.wes.api.main.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商品包装信息")
public class SkuPackageDTO implements Serializable {

    @Schema(description = "商品包装明细信息")
    private List<SkuPackageDetail> skuPackageDetails;

    @Data
    @Schema(description = "商品包装明细信息")
    public static class SkuPackageDetail implements Serializable {

        @NotNull
        @Schema(title = "包装级别")
        private Integer level;

        @NotEmpty
        @Schema(title = "包装编码")
        private String packageCode;

        @NotEmpty
        @Schema(title = "包装单位")
        private String unit;

        @Schema(title = "是否可拆包", description = "true 是，false 否")
        private boolean enableSplit;

        @Min(value = 0, message = "height must be greater than or equal to 0")
        @NotNull
        @Schema(title = "包装高度", description = "毫米(mm)")
        private Long height;

        @Min(value = 0, message = "width must be greater than or equal to 0")
        @NotNull
        @Schema(title = "包装宽度", description = "毫米(mm)")
        private Long width;

        @Min(value = 0, message = "length must be greater than or equal to 0")
        @NotNull
        @Schema(title = "包装高度", description = "毫米(mm)")
        private Long length;

        @Min(value = 0, message = "weight must be greater than or equal to 0")
        @NotNull
        @Schema(title = "包装重量", description = "毫克(mg)")
        private Integer weight;
    }
}
