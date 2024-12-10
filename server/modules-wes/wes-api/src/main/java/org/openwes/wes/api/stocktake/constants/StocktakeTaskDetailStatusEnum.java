package org.openwes.wes.api.stocktake.constants;

import com.google.common.collect.Lists;
import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum StocktakeTaskDetailStatusEnum implements IEnum {

    NEW("NEW", "新单据"),
    STARTED("STARTED", "盘点中"),
    CLOSE("CLOSE", "关闭"),
    PARTIAL_DONE("PARTIAL_DONE", "部分完成"),
    DONE("DONE", "完成"),
    ;

    private final String value;
    private final String label;

    public static final List<StocktakeTaskDetailStatusEnum> PROCESSING_LIST = Lists.newArrayList(STARTED);
    public static final List<StocktakeTaskDetailStatusEnum> CLOSEABLE_LIST = Lists.newArrayList(NEW, STARTED);
    public static final List<StocktakeTaskDetailStatusEnum> FINAL_LIST = Lists.newArrayList(CLOSE, PARTIAL_DONE, DONE);

    public static boolean isProcessing(StocktakeTaskDetailStatusEnum status) {
        return PROCESSING_LIST.contains(status);
    }

    public static boolean isCloseable(StocktakeTaskDetailStatusEnum status) {
        return CLOSEABLE_LIST.contains(status);
    }

    public static boolean isFinal(StocktakeTaskDetailStatusEnum status) {
        return FINAL_LIST.contains(status);
    }
}
