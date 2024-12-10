export enum TipType {
    /** 空箱处理提示 */
    EMPTY_CONTAINER_HANDLE_TIP = "EMPTY_CONTAINER_HANDLE_TIP",
    /** 选择拣选任务提示 */
    CHOOSE_PICKING_TASK_TIP = "CHOOSE_PICKING_TASK_TIP",
    /** 封箱提示 */
    SEAL_CONTAINER_TIP = "SEAL_CONTAINER_TIP",
    /** 异常登记 */
    REPORT_ABNORMAL_TIP = "REPORT_ABNORMAL_TIP",
    /** 扫描错误提示 */
    SCAN_ERROR_REMIND_TIP = "ERROR_REMIND_TIP",

    /** 整箱出库不拣选提示 */
    FULL_CONTAINER_AUTO_OUTBOUND_TIP = "FULL_CONTAINER_AUTO_OUTBOUND_TIP",

    /** 语音提醒 */
    PICKING_VOICE_TIP = "PICKING_VOICE_TIP"
}

/** 弹框类型 */
export enum ModalType {
    /** 弱提示 */
    TIP = "tip",
    /** 强提示 */
    CONFIRM = "confirm",
    /** 语音提示 */
    VOICE = "voice"
}
