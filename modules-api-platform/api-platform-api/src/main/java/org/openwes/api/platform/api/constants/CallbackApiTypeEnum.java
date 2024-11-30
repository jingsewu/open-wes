package org.openwes.api.platform.api.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum CallbackApiTypeEnum implements IEnum {

    COMMON_CALLBACK("COMMON_CALLBACK", "通用回调"),

    /**
     * WMS API: openWes -> WMS
     */
    INBOUND_PLAN_ORDER_COMPLETED("INBOUND_PLAN_ORDER_COMPLETED", "入库计划订单完成"),
    PUT_AWAY_TASK_COMPLETED("PUT_AWAY_TASK_COMPLETED", "上架任务完成"),

    OUTBOUND_PLAN_ORDER_DISPATCHED("OUTBOUND_PLAN_ORDER_DISPATCHED", "出库计划订单派发"),
    OUTBOUND_PLAN_ORDER_COMPLETE("OUTBOUND_PLAN_ORDER_COMPLETE", "出库计划订单完成"),
    OUTBOUND_SEAL_CONTAINER("OUTBOUND_SEAL_CONTAINER", "出库封箱"),
    STOCK_ABNORMAL_CALLBACK("STOCK_ABNORMAL_CALLBACK", "库存异常回调"),
    STOCK_ADJUSTMENT_CALLBACK("STOCK_ADJUSTMENT_CALLBACK", "库存调整回调"),
    STOCK_SYNCHRONIZATION_CALLBACK("STOCK_SYNCHRONIZATION_CALLBACK", "库存同步回调"),
    TRANSFER_CONTAINER_DESTINATION("TRANSFER_CONTAINER_DESTINATION", "转移容器目的地"),


    /**
     * PTL API: openWes -> PTL
     */
    PTL_CONTROL("PTL_CONTROL", "PTL控制"),


    /**
     * RCS API: openWes -> RCS
     */
    EMPTY_CONTAINER_INBOUND_TASK_CREATE("EMPTY_CONTAINER_INBOUND_TASK_CREATE", "空容器入库任务创建"),
    CONTAINER_TASK_CREATE("CONTAINER_TASK_CREATE", "容器任务创建"),


    /**
     * 任务取消
     */
    CONTAINER_TASK_CANCEL("CONTAINER_TASK_CANCEL", "容器任务取消"),


    /**
     * 任务放行
     */
    CONTAINER_TASK_RELEASE("CONTAINER_TASK_RELEASE", "容器任务放行"),


    CONTAINER_LEAVE("CONTAINER_LEAVE", "容器离开"),

    CALL_ROBOT("CALL_ROBOT", "呼叫机器人");

    private final String value;
    private final String label;

    private final String name = "回调类型";

}
