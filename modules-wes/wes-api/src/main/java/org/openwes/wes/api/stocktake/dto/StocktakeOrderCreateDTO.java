package org.openwes.wes.api.stocktake.dto;


import com.google.common.collect.Lists;
import org.openwes.common.utils.validate.IValidate;
import org.openwes.wes.api.stocktake.constants.StocktakeCreateMethodEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeMethodEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeTypeEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeUnitTypeEnum;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "盘点单创建请求")
public class StocktakeOrderCreateDTO implements Serializable, IValidate {

    @Schema(title = "仓库编码")
    @NotEmpty
    private String warehouseCode;

    @Schema(title = "盘点类型")
    @NotNull
    private StocktakeTypeEnum stocktakeType;

    @Schema(title = "盘点方式")
    @NotNull
    private StocktakeMethodEnum stocktakeMethod;

    @Schema(title = "盘点创建方式")
    @NotNull
    private StocktakeCreateMethodEnum stocktakeCreateMethod;

    @Schema(title = "盘点基本单位")
    @NotNull
    private StocktakeUnitTypeEnum stocktakeUnitType;

    @Schema(title = "是否盘点零库存")
    private Boolean includeZeroStock = Boolean.FALSE;

    @Schema(title = "是否盘点空格口")
    private Boolean includeEmptySlot = Boolean.FALSE;

    @Schema(title = "货架列表", description = "要盘点的货架列表")
    private List<String> shelfCodes;

    @Schema(title = "货架面列表", description = "要盘点的货架面列表")
    private List<String> shelfCodeFacePairs;

    @Schema(title = "容器列表", description = "要盘点的容器列表")
    private List<String> containerCodes;

    @Schema(title = "商品id列表", description = "要盘点的商品id列表")
    private List<Long> skuIds;

    @Schema(title = "库存id列表", description = "要盘点的库存id列表")
    private List<Long> stockIds;

    @Hidden
    @Schema(title = "盘点基本单位编码列表", description = "盘点基本单位编码列表：按货架盘点时为货架编码列表、按料箱盘点时为料箱编码列表")
    private List<String> unitCodes;

    @Hidden
    @Schema(title = "盘点基本单位Id", description = "盘点基本单位Id：按商品盘点时为SkuId列表、按库存盘点时为StockId列表")
    private List<Long> unitIds;

    @Hidden
    @Schema(title = "复盘的库存异常id列表")
    private List<Long> stockAbnormalRecordIds;

    @Schema(title = "库区ID")
    private Long warehouseAreaId;

    @Schema(title = "逻辑区ID")
    private Long warehouseLogicId;

    @Schema(title = "工作站ID")
    private Long workStationId;

    public void transfer() {
        unitCodes = Lists.newArrayList();
        unitIds = Lists.newArrayList();
        switch (stocktakeUnitType) {
            case CONTAINER -> unitCodes.addAll(containerCodes);
            case SHELF -> unitCodes.addAll(shelfCodes);
            case SKU -> unitIds.addAll(skuIds);
            case STOCK -> unitIds.addAll(stockIds);
        }
    }

    @Override
    public boolean validate() {

        if (this.stocktakeUnitType == StocktakeUnitTypeEnum.CONTAINER && ObjectUtils.isEmpty(containerCodes)) {
            throw new IllegalArgumentException("containerCodes is empty");
        }
        if (this.stocktakeUnitType == StocktakeUnitTypeEnum.SHELF && ObjectUtils.isEmpty(shelfCodes)) {
            throw new IllegalArgumentException("shelfCodes is empty");
        }
        if (this.stocktakeUnitType == StocktakeUnitTypeEnum.SKU && ObjectUtils.isEmpty(skuIds)) {
            throw new IllegalArgumentException("skuIds is empty");
        }
        if (this.stocktakeUnitType == StocktakeUnitTypeEnum.STOCK && ObjectUtils.isEmpty(stockIds)) {
            throw new IllegalArgumentException("stockIds is empty");
        }
        return true;
    }
}
