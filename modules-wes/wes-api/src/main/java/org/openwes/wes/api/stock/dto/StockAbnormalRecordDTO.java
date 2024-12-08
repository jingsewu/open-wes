package org.openwes.wes.api.stock.dto;

import org.openwes.wes.api.stock.constants.StockAbnormalStatusEnum;
import org.openwes.wes.api.stock.constants.StockAbnormalTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class StockAbnormalRecordDTO implements Serializable {

    private Long id;

    @NotEmpty
    private String warehouseCode;

    @NotEmpty
    private String ownerCode;

    private String orderNo;

    private StockAbnormalTypeEnum stockAbnormalType;

    private String replayNo;

    @NotNull
    private Long containerStockId;
    @NotNull
    private Long skuBatchStockId;
    @NotNull
    private Long skuBatchAttributeId;
    @NotNull
    private Long skuId;
    private String skuCode;

    @NotEmpty
    private String containerCode;
    @NotEmpty
    private String containerSlotCode;
    private String locationCode;

    @NotNull
    private Integer qtyAbnormal;

    private String abnormalReason;
    private String reasonDesc;
    private String abnormalOrderNo;
    private StockAbnormalStatusEnum stockAbnormalStatus = StockAbnormalStatusEnum.NEW;
}
