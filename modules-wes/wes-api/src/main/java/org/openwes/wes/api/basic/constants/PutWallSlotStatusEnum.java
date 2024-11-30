package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PutWallSlotStatusEnum implements IEnum {

    IDLE("IDLE", "空闲"),

    WAITING_BINDING("WAITING_BINDING", "待绑定"),

    BOUND("BOUND", "已绑定"),

    // 数据库没有待分拨状态， 这里当商品扫码后，商品需要投递的已绑定槽口修改状态为 待分拨
    DISPATCH("DISPATCH", "待分拨"),

    WAITING_SEAL("WAITING_SEAL", "待封箱");

    private final String value;
    private final String label;
}
