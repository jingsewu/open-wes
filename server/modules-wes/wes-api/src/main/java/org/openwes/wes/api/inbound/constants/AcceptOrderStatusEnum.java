package org.openwes.wes.api.inbound.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum AcceptOrderStatusEnum implements IEnum {

    NEW("NEW", "新单据"),

    COMPLETE("COMPLETE", "已完成"),
    ;

    private final String value;
    private final String label;

    private final String name = "收货单状态";
}
