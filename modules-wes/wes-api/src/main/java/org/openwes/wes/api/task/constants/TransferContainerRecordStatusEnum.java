package org.openwes.wes.api.task.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum TransferContainerRecordStatusEnum implements IEnum {

    BOUNDED("BOUNDED", "已绑定"),
    SEALED("SEALED", "已封箱");

    private final String value;
    private final String label;

    private final String name = "周转容器记录状态";

}
