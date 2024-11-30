package org.openwes.wes.api.stocktake.dto;

import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.api.stocktake.constants.StocktakeAbnormalReasonEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeRecordStatusEnum;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "盘点记录")
public class StocktakeRecordDTO implements Serializable {
    private Long id;

    private Long stocktakeTaskDetailId;

    private Long stocktakeTaskId;

    private Long stocktakeOrderId;

    private String containerCode;

    private String containerFace;

    private String containerSlotCode;

    private Long skuId;

    private Long stockId;

    private Long skuBatchStockId;

    private Long skuBatchAttributeId;

    private Integer qtyOriginal;

    private Integer qtyStocktake;

    private StocktakeAbnormalReasonEnum abnormalReason;

    private Long workStationId;

    @Hidden
    private Long version;

    private StocktakeRecordStatusEnum stocktakeRecordStatus;

    private SkuMainDataDTO skuMainDataDTO;

    private SkuBatchAttributeDTO skuBatchAttributeDTO;
}
