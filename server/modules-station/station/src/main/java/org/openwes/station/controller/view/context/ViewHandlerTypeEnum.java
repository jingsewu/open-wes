package org.openwes.station.controller.view.context;

import com.google.common.collect.Lists;

import java.util.List;

public enum ViewHandlerTypeEnum {

    BASE_AREA,
    SKU_AREA,
    ORDER_AREA,
    PUT_WALL_AREA,
    CONTAINER_AREA,
    TIPS,

    /**
     * OUTBOUND
     */
    OUTBOUND_BASE_AREA, OUTBOUND_CONTAINER_AREA, OUTBOUND_SKU_AREA,

    /**
     * STOCKTAKE
     */
    STOCKTAKE_BASE_AREA,
    STOCKTAKE_SKU_AREA,
    ;


    public static List<ViewHandlerTypeEnum> baseViewHandlerTypes() {
        return Lists.newArrayList(BASE_AREA);
    }

    public static List<ViewHandlerTypeEnum> pickingViewHandlerTypes() {
        return Lists.newArrayList(OUTBOUND_BASE_AREA, OUTBOUND_SKU_AREA, PUT_WALL_AREA, OUTBOUND_CONTAINER_AREA, TIPS);
    }

    public static List<ViewHandlerTypeEnum> replenishmentViewHandlerTypes() {
        return Lists.newArrayList(ORDER_AREA, TIPS);
    }

    public static List<ViewHandlerTypeEnum> stocktakeViewHandlerTypes() {
        return List.of(STOCKTAKE_BASE_AREA, STOCKTAKE_SKU_AREA, TIPS, CONTAINER_AREA);
    }

}
