package org.openwes.wes.api.stocktake.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@Schema(description = "盘点记录提交请求")
public class StocktakeRecordSubmitDTO implements Serializable {
    @NotNull
    @Schema(title = "盘点记录id", requiredMode = Schema.RequiredMode.REQUIRED)
    protected Long recordId;

    @NotNull
    @Min(0)
    @Schema(title = "实盘数量", requiredMode = Schema.RequiredMode.REQUIRED)
    protected Integer stocktakeQty;

    @NotNull
    protected Long workStationId;

}
