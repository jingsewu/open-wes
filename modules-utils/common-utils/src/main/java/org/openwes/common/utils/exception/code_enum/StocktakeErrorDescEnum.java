package org.openwes.common.utils.exception.code_enum;

import org.openwes.common.utils.constants.AppCodeEnum;
import lombok.Getter;

@Getter
public enum StocktakeErrorDescEnum implements IBaseError {

    STOCKTAKE_BASE_ERROR("ST001001", "stocktake base error"),

    /**
     * 单据
     */
    STOCKTAKE_CREATE_ORDER_DETAIL_ERROR("ST002000", "Failed to create stocktake order details, the stock cannot be found through the stocktake unit"),
    STOCKTAKE_ORDER_NOT_FOUND("ST002001", "Stocktake orders not found"),
    STOCKTAKE_NO_STOCKTAKE_UNITS("ST002003", "Please select the record"),
    STOCKTAKE_ORDER_NOT_FOUND_OR_EXECUTED("ST002004", "Stocktake orders not found"),

    /**
     * 任务
     */
    STOCKTAKE_TASK_NOT_FOUND("ST003001", "Stocktake task was not found or completed"),
    STOCKTAKE_CONTAINER_NO_TASK("ST003002", "This container is not a required container for the current task"),

    /**
     * 盘点记录
     */
    STOCKTAKE_CONTAINER_STOCK_NOT_FOUND("ST004001", "Container stock not found"),
    STOCKTAKE_CONTAINER_STOCK_QTY_HAS_CHANGED("ST004002", "The quantity of inventory has changed"),
    STOCKTAKE_RECORD_NOT_FOUND("ST002003", "Stocktake records not found"),
    STOCKTAKE_EXCEEDING_THE_MIN_STOCKTAKE_LOSS_QTY("ST004004", "It is not allowed to operate the inventory occupied by outbound(quantity: {0}) and the inventory occupied by inventory adjustment(quantity: {1}). Minimum inventory counting quantity: {2}"),
    STOCKTAKE_SURPLUS_SKU_BATCH_ATTRIBUTE_NOT_FOUNT("ST004005", "Sku batch attribute not found"),
    STOCKTAKE_SURPLUS_SKU_NOT_FOUNT("ST004006", "Please enter the product information into the system first"),
    STOCKTAKE_SURPLUS_STOCK_ALREADY_EXIST("ST004007", "The inventory has been recorded in the system"),
    STOCKTAKE_SURPLUS_MULTIPLE_SKU_NOT_ALLOWED("ST004008", "multiple skus not allowed"),

    /**
     * 工作站
     */
    STOCKTAKE_NO_BAR_CODE_SCANNED("ST005001", "No bar code scanned"),
    STOCKTAKE_BAR_CODE_CANNOT_BE_BLANK("ST005002", "Bar code cannot be blank"),
    STOCKTAKE_NO_OPERATION_TASK("ST005003", "No operation task"),
    STOCKTAKE_BAR_CODE_PARSING_ERROR("ST005004", "Please check barcode"),
    STOCKTAKE_EXIST_UNFINISHED_STOCKTAKE_TASKS("ST005005", "There are still inventory tasks not completed"),
    STOCKTAKE_STATION_NOT_FOUND("ST005006", "Stocktake workstation not found"),
    STOCKTAKE_STATION_NOT_ONLINE("ST005007", "Stocktake workstation not online"),
    STOCKTAKE_STATION_OPERATION_TYPE_ERROR("ST005008", "The workstation operation type is incorrect"),
    STOCKTAKE_OPERATION_TASK_NOT_RIGHT("ST005009", "Stocktake operation task is incorrect");

    private final String code;
    private final String desc;
    private final String appCode = AppCodeEnum.WMS.name();

    StocktakeErrorDescEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
