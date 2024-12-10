package org.openwes.wes.api.stock.dto;

import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SkuBatchStockDTO implements Comparable<SkuBatchStockDTO> {

    private Long id;

    private Long skuId;
    //unique key union skuBatchAttributeId and warehouseAreaCode and warehouseCode
    private String warehouseCode;
    private Long skuBatchAttributeId;
    private Long warehouseAreaId;

    private Integer totalQty;
    private Integer availableQty;
    // outbound locked qty
    private Integer outboundLockedQty;
    // other operation locked qty in the warehouse
    private Integer noOutboundLockedQty;
    private Long version;


    //compare need these two fields
    private WarehouseAreaDTO warehouseArea;
    private SkuBatchAttributeDTO skuBatchAttribute;

    @Override
    public int compareTo(SkuBatchStockDTO skuBatchStockDTO) {

        int result = warehouseArea.getWarehouseAreaWorkType().compareTo(skuBatchStockDTO.getWarehouseArea().getWarehouseAreaWorkType());

        // 库区类型相同，再根据批次属性中的入库日期进行排序
        if (result == 0) {
            return skuBatchAttribute.compareTo(skuBatchStockDTO.getSkuBatchAttribute());
        }
        return result;
    }

    public void initCompareData(WarehouseAreaDTO warehouseAreaDTO, SkuBatchAttributeDTO skuBatchAttributeDTO) {
        this.warehouseArea = warehouseAreaDTO;
        this.skuBatchAttribute = skuBatchAttributeDTO;
    }
}
