export enum CustomActionType {
    /** 扫描SKU */
    SCAN_BARCODE = "SCAN_BARCODE",
    /** 点击确定 */
    STOCKTAKE_SUBMIT = "STOCKTAKE_SUBMIT",
    /** 领用盘点任务 */
    STOCKTAKE_EXECUTE_TASK = "STOCKTAKE_EXECUTE_TASK",
    /** 创建盘点单 */
    STOCKTAKE_CREATE_ORDER = "STOCKTAKE_CREATE_ORDER",
    /** 清空扫码框 */
    SCAN_BARCODE_RESET = "SCAN_BARCODE_RESET",
    /** 盈品 */
    STOCKTAKE_SURPLUS_SKU = "STOCKTAKE_SURPLUS_SKU",
    /** 理库确定操作 */
    RELOCATION_CONFIRM = "RELOCATION_CONFIRM",
    /** 理库异常登记，目标箱放不下 */
    RELOCATION_ABNORMAL = "RELOCATION_ABNORMAL",
    /** 容器离开 */
    RELOCATION_LEAVE = "RELOCATION_LEAVE"
}
