package org.openwes.station.api.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChooseAreaEnum {
    SKU_AREA("skuArea"),
    CONTAINER_AREA("containerArea"),
    PUT_WALL_AREA("putWallArea"),
    SCAN_AREA("scanArea"),
    ORDER_AREA("orderArea"),
    TIPS("tips");
    private final String value;
}
