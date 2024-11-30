package org.openwes.wes.api.stocktake.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "盘点单领用")
public class StocktakeOrderReceiveDTO implements Serializable {

    @NotEmpty
    @Schema(title = "盘点任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> stocktakeTaskIds;

    @Schema(title = "工作站ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @NotNull
    private Long workStationId;

}
