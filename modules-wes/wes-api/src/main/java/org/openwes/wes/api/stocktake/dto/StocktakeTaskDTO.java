package org.openwes.wes.api.stocktake.dto;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.openwes.wes.api.stocktake.constants.*;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "盘点任务")
public class StocktakeTaskDTO implements Serializable {

    private Long id;

    private Long stocktakeOrderId;

    private String taskNo;

    private String warehouseCode;

    private StocktakeTypeEnum stocktakeType;

    private StocktakeCreateMethodEnum stocktakeCreateMethod;

    private StocktakeMethodEnum stocktakeMethod;

    private StocktakeUnitTypeEnum stocktakeUnitType;

    private StocktakeTaskStatusEnum stocktakeTaskStatus;

    private Long workStationId;

    private Long receivedUserId;

    @Hidden
    private Long version;

    private List<StocktakeTaskDetailDTO> details;
}
