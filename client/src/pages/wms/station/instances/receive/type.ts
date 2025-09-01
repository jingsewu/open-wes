import type {DefaultProps} from "./operations/defaultPage"
import type {RobotHandlerProps} from "./operations/containerHandler"

export enum StationOperationType {
    conveyorArea = "conveyorArea",
    robotArea = "robotArea",
    orderArea = "orderArea",
    selectDetailArea = "selectDetailArea",
    shelfArea = "shelfArea",
    collectingDoc = "collectingDoc",
    callBin = "callBin",
    infoInput = "infoInput",
    scanInfoListArea = "scanInfoListArea",
    defaultArea = "defaultArea",
    tips = "TIPS"
}

/**
 * 工作站物理设备类型
 */
export enum StationPhysicalType {
    /** 缓存货架 */
    SHELF = "SHELF",
    /** 机器人 */
    ROBOT = "ROBOT",
    /** 输送线 */
    CONVEYOR = "CONVEYOR",
    /** 卸料机 */
    UNLOADER = "UNLOADER",
    /** 推荐容器 */
    RECOMMEND = "RECOMMEND"
}

const ROBOT = {
    sanCode: "INBOUND_RECEIVE_ROBOT_SCAN_CODE_OPERATION_CONFIRM", // 扫码
    confirm: "INBOUND_ROBOT_SELECT_CONTAINER_PUTAWAY_CONFIRM_OPERATION_CONFIRM", // 确定
    switchConfirm:
        "INBOUND_ROBOT_EXCHANGE_SELECT_CONTAINER_PUTAWAY_RECEIVE_CONFIRM_EVENT", // 切换确定动作
    switchSanCode:
        "INBOUND_ROBOT_EXCHANGE_SELECT_CONTAINER_PUTAWAY_SCAN_CODE_EVENT", // 切换扫码动作
    full: "INBOUND_ROBOT_SELECT_CONTAINER_PUTAWAY_DONE_RECEIVED_EVENT" // 满箱
}
export const ReplenishTypeUrlMap = {
    MANUAL: {
        CONVEYOR: {
            sanCode:
                "INBOUND_RECEIVE_MANUAL_CONVEYOR_CHOOSE_SCAN_CODE_OPERATION_CONFIRM", // 扫码
            confirm:
                "INBOUND_RECEIVE_MANUAL_CONVEYOR_CONFIRM_OPERATION_CONFIRM", // 确定
            switchConfirm:
                "INBOUND_RECEIVE_MANUAL_EXCHANGE_CONVEYOR_CONFIRM_EVENT", // 切换确定动作
            switchSanCode:
                "INBOUND_RECEIVE_MANUAL_CONVEYOR_EXCHANGE_CHOOSE_SCAN_CODE_EVENT", // 切换扫码动作
            full: "INBOUND_RECEIVE_MANUAL_CONVEYOR_DONE_RECEIVED_EVENT", // 满箱
            skuContainerSwitch:
                "INBOUND_RECEIVE_MANUAL_SHELF_EXCHANGE_MATCH_MODE_EVENT" // 箱货切换
        },
        SHELF: {
            sanCode: "INBOUND_RECEIVE_MANUAL_SHELF_SCAN_CODE_OPERATION_CONFIRM", // 扫码
            confirm: "INBOUND_RECEIVE_MANUAL_SHELF_CONFIRM_OPERATION_CONFIRM", // 确定
            switchConfirm:
                "INBOUND_RECEIVE_MANUAL_SHELF_EXCHANGE_CONFIRM_EVENT", // 切换确定动作
            switchSanCode:
                "INBOUND_RECEIVE_MANUAL_SHELF_EXCHANGE_CHOOSE_SCAN_CODE_EVENT", // 切换扫码动作
            full: "INBOUND_RECEIVE_MANUAL_SHELF_DONE_RECEIVED_EVENT", // 满箱
            skuContainerSwitch:
                "INBOUND_RECEIVE_MANUAL_SHELF_EXCHANGE_MATCH_MODE_EVENT" // 箱货切换
        },
        ROBOT: ROBOT
    },
    RECOMMEND: {
        CONVEYOR: {
            sanCode:
                "INBOUND_RECEIVE_RECOMMEND_CONVEYOR_CHOOSE_SCAN_CODE_OPERATION_CONFIRM", // 扫码
            confirm:
                "INBOUND_RECEIVE_RECOMMEND_CONVEYOR_CONFIRM_OPERATION_CONFIRM", // 确定
            switchConfirm:
                "INBOUND_RECEIVE_RECOMMEND_EXCHANGE_CONVEYOR_CONFIRM_EVENT", // 切换确定动作
            switchSanCode:
                "INBOUND_RECEIVE_RECOMMEND_CONVEYOR_EXCHANGE_CHOOSE_SCAN_CODE_EVENT", // 切换扫码动作
            full: "INBOUND_RECEIVE_RECOMMEND_CONVEYOR_DONE_RECEIVED_EVENT", // 满箱
            skuContainerSwitch:
                "INBOUND_RECEIVE_RECOMMEND_SHELF_EXCHANGE_MATCH_MODE_EVENT" // 箱货切换
        },
        SHELF: {
            sanCode:
                "INBOUND_RECEIVE_RECOMMEND_SHELF_SCAN_CODE_OPERATION_CONFIRM", // 扫码
            confirm:
                "INBOUND_RECEIVE_RECOMMEND_SHELF_CONFIRM_OPERATION_CONFIRM", // 确定
            switchConfirm:
                "INBOUND_RECEIVE_RECOMMEND_SHELF_EXCHANGE_CONFIRM_EVENT", // 切换确定动作
            switchSanCode:
                "INBOUND_RECEIVE_RECOMMEND_SHELF_EXCHANGE_CHOOSE_SCAN_CODE_EVENT", // 切换扫码动作
            full: "INBOUND_RECEIVE_RECOMMEND_SHELF_DONE_RECEIVED_EVENT", // 满箱
            skuContainerSwitch:
                "INBOUND_RECEIVE_RECOMMEND_SHELF_EXCHANGE_MATCH_MODE_EVENT" // 箱货切换
        },
        ROBOT: ROBOT
    },
    NONE: {
        CONVEYOR: {
            sanCode:
                "INBOUND_RECEIVE_MANUAL_CONVEYOR_CHOOSE_SCAN_CODE_OPERATION_CONFIRM", // 扫码
            confirm:
                "INBOUND_RECEIVE_MANUAL_CONVEYOR_CONFIRM_OPERATION_CONFIRM", // 确定
            switchConfirm:
                "INBOUND_RECEIVE_MANUAL_EXCHANGE_CONVEYOR_CONFIRM_EVENT", // 切换确定动作
            switchSanCode:
                "INBOUND_RECEIVE_MANUAL_CONVEYOR_EXCHANGE_CHOOSE_SCAN_CODE_EVENT", // 切换扫码动作
            full: "INBOUND_RECEIVE_MANUAL_CONVEYOR_DONE_RECEIVED_EVENT" // 满箱
        },
        ROBOT: ROBOT
    }
}

export interface replenishProps {
    [StationOperationType.robotArea]: RobotHandlerProps
    [StationOperationType.defaultArea]: DefaultProps
}
