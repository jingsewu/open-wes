package org.openwes.wes.api.ems.proxy.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public enum BusinessTaskTypeEnum {

    EMPTY_CONTAINER_INBOUND,

    RECEIVING,
    PUT_AWAY,
    PICKING,
    SORTING,
    ONE_STEP_RELOCATION,

    REPLENISH,
    SELECT_CONTAINER_PUT_AWAY,
    WITHOUT_ORDER_PUT_AWAY,

    // pick sku from a container in the storage area to relocation area.
    RELOCATION,
    COUNTING,
    RECHECK,

    STOCKTAKE,
    ;

    public static final Collection<BusinessTaskTypeEnum> replenishmentType = List.of(REPLENISH, SELECT_CONTAINER_PUT_AWAY, WITHOUT_ORDER_PUT_AWAY);

}
