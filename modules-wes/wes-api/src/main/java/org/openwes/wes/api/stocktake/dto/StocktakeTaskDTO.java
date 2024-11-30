package org.openwes.wes.api.stocktake.dto;

import org.openwes.wes.api.stocktake.constants.StocktakeTaskStatusEnum;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "盘点任务")
public class StocktakeTaskDTO implements Serializable {
    private Long id;

    private Long stocktakeOrderId;

    private String taskNo;

    private String warehouseCode;

    private StocktakeTaskStatusEnum stocktakeTaskStatus;

    private Long workStationId;

    private Long receivedUserId;

    @Hidden
    private Long version;

    private List<StocktakeTaskDetailDTO> details;
}
