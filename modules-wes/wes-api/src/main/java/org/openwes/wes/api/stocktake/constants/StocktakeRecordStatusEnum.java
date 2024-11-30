package org.openwes.wes.api.stocktake.constants;

import com.google.common.collect.Lists;
import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@SuppressWarnings("unused")
public enum StocktakeRecordStatusEnum implements IEnum {
    NEW("NEW", "新单据"),
    DONE("DONE", "完成"),
    CLOSE("CLOSE", "关闭"),
    ;

    private final String value;
    private final String label;

    public static final List<StocktakeRecordStatusEnum> CLOSEABLE_LIST = Lists.newArrayList(NEW);
    public static final List<StocktakeRecordStatusEnum> FINAL_LIST = Lists.newArrayList(DONE, CLOSE);

    public static boolean isFinal(StocktakeRecordStatusEnum status) {
        return FINAL_LIST.contains(status);
    }

    public static boolean isCloseable(StocktakeRecordStatusEnum status) {
        return CLOSEABLE_LIST.contains(status);
    }
}
