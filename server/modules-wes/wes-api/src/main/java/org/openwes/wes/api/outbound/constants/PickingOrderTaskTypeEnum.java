package org.openwes.wes.api.outbound.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PickingOrderTaskTypeEnum {

    ROBOT("ROBOT", "机区拣选"),
    RELAY("RELAY", "接力拣选"),
    MANUAL("MANUAL", "人工区拣选任务");

    private final String value;
    private final String label;
}
