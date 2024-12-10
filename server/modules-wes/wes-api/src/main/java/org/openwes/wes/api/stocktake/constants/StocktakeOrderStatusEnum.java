package org.openwes.wes.api.stocktake.constants;

import com.google.common.collect.Lists;
import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum StocktakeOrderStatusEnum implements IEnum {

    NEW("NEW", "新单据"),
    STARTED("STARTED", "盘点中"),
    PARTIAL_DONE("PARTIAL_DONE", "部分完成"),
    DONE("DONE", "完成"),
    CANCELED("CANCELED", "取消"),
    ;

    private final String value;
    private final String label;

    public static final List<StocktakeOrderStatusEnum> CANCELABLE_LIST = Lists.newArrayList(NEW);
    public static final List<StocktakeOrderStatusEnum> EXECUTABLE_LIST = Lists.newArrayList(NEW);
    public static final List<StocktakeOrderStatusEnum> FINAL_LIST = Lists.newArrayList(PARTIAL_DONE, DONE);

    public static boolean isCancelable(StocktakeOrderStatusEnum status) {
        return CANCELABLE_LIST.contains(status);
    }

    public static boolean isExecutable(StocktakeOrderStatusEnum status) {
        return EXECUTABLE_LIST.contains(status);
    }

    public static boolean isFinal(StocktakeOrderStatusEnum status) {
        return FINAL_LIST.contains(status);
    }
}
