package org.openwes.wes.api.outbound.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum OutboundWaveStatusEnum implements IEnum {

    NEW("NEW", "新单据"),
    PROCESSING("PROCESSING", "处理中"),
    DONE("DONE", "完成"),
    CANCELED("CANCELED", "取消");

    private final String value;
    private final String label;

    private final String name = "出库波次状态";

}
