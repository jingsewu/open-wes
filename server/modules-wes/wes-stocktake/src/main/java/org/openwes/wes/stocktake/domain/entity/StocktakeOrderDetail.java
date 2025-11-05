package org.openwes.wes.stocktake.domain.entity;

import lombok.Data;
import org.openwes.wes.api.stocktake.constants.StocktakeUnitTypeEnum;

@Data
public class StocktakeOrderDetail {

    private Long id;

    private Long stocktakeOrderId;

    private String warehouseCode;

    /**
     * 盘点基本单位类型
     */
    private StocktakeUnitTypeEnum stocktakeUnitType;

    /**
     * 盘点基本单位编码
     */
    private String unitCode;

    /**
     * 盘点基本单位Id：按商品盘点时为SkuId
     */
    private Long unitId;

    private Long version;

    public void initialize(Long stocktakeOrderId) {
        this.stocktakeOrderId = stocktakeOrderId;
    }
}
