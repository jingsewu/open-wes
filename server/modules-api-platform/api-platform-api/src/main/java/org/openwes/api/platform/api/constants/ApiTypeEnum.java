package org.openwes.api.platform.api.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum ApiTypeEnum implements IEnum {

    /**
     * WMS API
     */
    SKU_CREATE("SKU_CREATE", "创建商品"),
    SKU_BATCH_ATTRIBUTE_UPDATE("SKU_BATCH_ATTRIBUTE_UPDATE", "修改商品批次属性"),

    ORDER_INBOUND_CREATE("ORDER_INBOUND_CREATE", "创建入库订单"),
    ORDER_INBOUND_CANCEL("ORDER_INBOUND_CANCEL", "取消入库订单"),

    ORDER_OUTBOUND_CREATE("ORDER_OUTBOUND_CREATE", "创建出库订单"),
    ORDER_OUTBOUND_CANCEL("ORDER_OUTBOUND_CANCEL", "取消出库订单"),

    ORDER_STOCKTAKE_CREATE("ORDER_STOCKTAKE_CREATE", "创建盘点订单"),

    CONTAINER_COUNT_SYNCHRONIZE("CONTAINER_COUNT_SYNCHRONIZE", "箱子数量同步"),
    TRANSFER_CONTAINER_RELEASE("TRANSFER_CONTAINER_RELEASE", "释放周转箱"),

    ORDER_PRIORITY_ADJUST("ORDER_PRIORITY_ADJUST", "订单优先级调整"),

    SKU_STOCK_SEARCH("SKU_STOCK_SEARCH", "SKU库存查询"),

    /**
     * WCS API
     */
    CONTAINER_ARRIVE("CONTAINER_ARRIVE", "容器到达"),

    CONTAINER_LOCATION_REPORT("CONTAINER_LOCATION_REPORT", "容器位置上报"),

    CONTAINER_TASK_STATUS_REPORT("CONTAINER_TASK_STATUS_REPORT", "容器任务状态上报"),

    PTL_REPORT("PTL_REPORT", "电子标签上报");

    private final String value;
    private final String label;

    private final String name = "API类型";

}
