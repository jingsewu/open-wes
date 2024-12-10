package org.openwes.wes.api.stock.constants;

import com.google.common.collect.Lists;
import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockAbnormalStatusEnum implements IEnum {

    NEW("NEW", "新单据"),
    CREATE_RE_CHECK_ORDER("CREATE_RE_CHECK_ORDER", "复盘单创建"),
    CREATE_ADJUSTMENT_ORDER("CREATE_ADJUSTMENT_ORDER", "调整单创建"),

    /*=========结束状态============*/
    FINISH_RE_CHECK_ORDER("FINISH_RE_CHECK_ORDER", "差异复盘"),
    FINISH_ADJUSTMENT_ORDER("FINISH_ADJUSTMENT_ORDER", "调整完成"),
    CLOSED("CLOSED", "已关闭");

    private final String value;
    private final String label;

    public static boolean isFinished(StockAbnormalStatusEnum status) {
        return Lists.newArrayList(FINISH_RE_CHECK_ORDER, FINISH_ADJUSTMENT_ORDER, CLOSED).contains(status);
    }
}
