package org.openwes.wes.api.stock.dto;

import org.openwes.wes.api.stock.constants.StockAbnormalReasonEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class StockAdjustmentDetailDTO implements Serializable {
    private Long id;
    private Long stockAdjustmentOrderId;
    private Long stockAbnormalRecordId;
    private String warehouseCode;
    private String ownerCode;
    private String containerCode;
    private String containerSlotCode;
    private Long containerStockId;
    private Long skuBatchStockId;
    private Long skuBatchAttributeId;

    private Long skuId;
    private String skuCode;
    private Integer qtyAdjustment;
    private StockAbnormalReasonEnum abnormalReason;
}
