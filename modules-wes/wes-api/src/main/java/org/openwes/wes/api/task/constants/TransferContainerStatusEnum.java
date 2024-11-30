package org.openwes.wes.api.task.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum TransferContainerStatusEnum implements IEnum {

    /**
     * when transfer container is created or unlocked
     */
    IDLE("IDLE", "空闲"),
    /**
     * when transfer container is bound on put wall
     */
    OCCUPANCY("OCCUPANCY", "占用"),
    /**
     * when transfer container is sealed, but not unlocked yet
     */
    LOCKED("LOCKED", "锁定"),
    ;


    private final String value;
    private final String label;

    private final String name = "周转容器状态";


}
