package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作台状态
 *
 * @author sws
 */
@AllArgsConstructor
@Getter
public enum WorkStationStatusEnum implements IEnum {

    ONLINE("ONLINE", "在线"),

    PAUSED("PAUSED", "暂停"),

    OFFLINE("OFFLINE", "离线");

    private final String value;
    private final String label;
}
