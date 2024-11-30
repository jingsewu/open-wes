package org.openwes.wes.api.ems.proxy.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

// 也就是INBOUND/PUT AWAY, OUTBOUND, TRANSFER
@Getter
@AllArgsConstructor
public enum ContainerTaskTypeEnum implements IEnum {
    INBOUND("INBOUND", "入库"),

    OUTBOUND("OUTBOUND", "出库"),
    TRANSFER("TRANSFER", "搬运"),
    ;

    private final String value;
    private final String label;

    private final String name = "容器任务类型";
}
