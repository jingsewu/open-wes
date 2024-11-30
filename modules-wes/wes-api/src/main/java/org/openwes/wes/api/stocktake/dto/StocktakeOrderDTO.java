package org.openwes.wes.api.stocktake.dto;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.openwes.wes.api.stocktake.constants.*;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "盘点单")
public class StocktakeOrderDTO implements Serializable {

    @Schema(title = "盘点单 id")
    private Long id;

    @Schema(title = "仓库编码")
    private String warehouseCode;

    @Schema(title = "订单号")
    private String orderNo;

    @Schema(title = "客户订单编号")
    private String customerOrderNo;

    @Schema(title = "盘点单状态")
    private StocktakeOrderStatusEnum stocktakeOrderStatus;

    @Schema(title = "盘点类型")
    private StocktakeTypeEnum stocktakeType;

    @Schema(title = "盘点方式")
    private StocktakeMethodEnum stocktakeMethod;

    @Schema(title = "创建方式")
    private StocktakeCreateMethodEnum stocktakeCreateMethod;

    @Schema(title = "创建类型")
    private StocktakeUnitTypeEnum stocktakeUnitType;

    @Schema(title = "是否盘点零库存")
    private Boolean includeZeroStock;

    @Schema(title = "是否盘点空格口")
    private Boolean includeEmptySlot;

    @Schema(title = "挂单标识")
    private boolean suspend;

    @Schema(title = "异常标识")
    private boolean abnormal;

    @Schema(title = "库区ID")
    private Long warehouseAreaId;

    @Schema(title = "逻辑区ID")
    private Long warehouseLogicId;

    @Hidden
    private Long version;

    @Schema(title = "盘点单明细")
    private List<StocktakeOrderDetailDTO> details;

    @Schema(title = "盘点记录")
    private List<StocktakeRecordDTO> records;
}
