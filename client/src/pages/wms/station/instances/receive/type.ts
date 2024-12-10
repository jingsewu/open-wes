import type { DefaultProps } from "./operations/defaultPage"

import type { CustomActionResponse } from "../types"
import type { RobotHandlerProps } from "./operations/RobotHandler"
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

// 箱找货CONTAINER_MATCH_SKU 货找箱SKU_MATCH_CONTAINER
export enum ShelfMatchType {
    CONTAINER_MATCH_SKU = "CONTAINER_MATCH_SKU",
    SKU_MATCH_CONTAINER = "SKU_MATCH_CONTAINER"
}

export enum MultipleBatchesMap {
    MANUAL = "INBOUND_RECEIVE_MANUAL_SHELF_CHOOSE_DETAIL_OPERATION_CONFIRM",
    RECOMMEND = "INBOUND_RECEIVE_RECOMMEND_SHELF_CHOOSE_DETAIL_OPERATION_CONFIRM"
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

export interface ContainerArea {
    conveyorArea: ContainerAreaOne
    operationId: string
    operationType: string
    rotationAngle?: number
}
export interface ContainerAreaOne {
    conveyorInfoList: ConveyorInfoList[]
}
export interface ConveyorInfoList {
    containerCode: any
    /** 扫描容器号 */
    onChange?: (code: string | undefined) => void
    changeSpecification?: (code: string | undefined) => void
    onCustomActionDispatch: (value: any) => Promise<CustomActionResponse>
    handleSubContainerChange?: (param: string) => void
    address: string
    containerInfo: ContainerInfo
    targetSelected: boolean
    /** 格口点击事件 */
}
export interface ContainerInfo {
    /** 料箱编码 */
    containerCode: string
    /** 料箱规格 */
    containerType: string
    containerLayoutCode: string
    containerLayoutName: string
    face: string
    modifySubContainerFlag: boolean // 是否允许选子容器 true:是 false 否
    modifyContainerSpecFlag: boolean
    subContainerInfoList: SubContainerInfoList[]
    subContainers?: SubContainer[]
    containerLayoutInfoList: ContainerLayoutInfoList[]
    onClick?: (param: string, code?: string) => void
}
export interface SubContainer {
    /** 是否正在操作的格口 */
    active?: boolean
    /** 第几行 */
    level?: number
    /** 第几列 */
    bay?: number
    /** 格口是否可用 */
    enable?: boolean
    /** 格口编码 */
    subContainerCode?: string
    /** 格口名称 */
    subContainerName?: string
    containerCode?: string
    stationSlot?: string
    subContainerBay: string
    subContainerLevel: string
    stationSlotStatus?: string
    subContainerStatus?: string
}
export interface ContainerLayoutInfoList {
    containerLayoutCode: string
    containerLayoutName: string
    face: string
    containerType: string
    active: boolean
}
interface SubContainerInfoList {
    subContainerBay: string
    subContainerLevel: string
    /** 格口编码 */
    subContainerCode: string
    /** 格口名称 */
    subContainerName?: string
    targetSelected: boolean
}
