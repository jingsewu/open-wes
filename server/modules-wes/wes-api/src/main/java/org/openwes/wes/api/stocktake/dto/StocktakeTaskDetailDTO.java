package org.openwes.wes.api.stocktake.dto;

import org.openwes.wes.api.stocktake.constants.StocktakeTaskDetailStatusEnum;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "盘点任务明细")
public class StocktakeTaskDetailDTO implements Serializable {
    private Long id;

    private Long stocktakeOrderId;

    private Long stocktakeTaskId;

    private String warehouseCode;

    private String containerCode;

    private String containerFace;

    private StocktakeTaskDetailStatusEnum stocktakeTaskDetailStatus;

    private Long workStationId;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private String updateUser;

    @Hidden
    private Long version;

    private List<StocktakeRecordDTO> records;
}
