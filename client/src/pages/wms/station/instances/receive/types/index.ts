import type { DefaultProps } from "../operations/defaultPage"
import type { RobotHandlerProps } from "../operations/containerHandler"

// 工作站操作类型枚举
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

// 工作站物理设备类型
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

// 自定义动作类型
export enum CustomActionType {
    /** 上架选择容器格口事件 */
    CHOOSE_CONTAINER_SLOT_CODE = "CHOOSE_CONTAINER_SLOT_CODE",
    /** 点击异常登记按钮 */
    INBOUND_ABNORMAL_TIP = "INBOUND_ABNORMAL_TIP",
    /** 异常登记确认 */
    INBOUND_ABNORMAL_CONFIRM = "INBOUND_ABNORMAL_CONFIRM",
    /** 一码多品选择SKU确认 */
    INBOUND_SCAN_BARCODE_2_MANY_SKU_CODE = "INBOUND_SCAN_BARCODE_2_MANY_SKU_CODE",
    /** 容器离开事件 */
    CONTAINER_LEAVE = "CONTAINER_LEAVE",
}

// 操作URL配置类型
export interface OperationUrls {
    sanCode: string
    confirm: string
    switchConfirm?: string
    switchSanCode?: string
    full?: string
    skuContainerSwitch?: string
}

// 补货类型URL映射类型
export type ReplenishTypeUrlMapType = {
    [key in 'MANUAL' | 'RECOMMEND' | 'NONE']: {
        [key in StationPhysicalType]?: OperationUrls
    }
}

// 保留核心类型定义
export interface SkuInfo {
    id?: string
    skuId: string
    skuCode: string
    skuName: string
    batchAttributes?: Record<string, any>
    qtyAccepted?: number
    qtyRestocked?: number
}

export interface OrderDetail extends SkuInfo {
    qtyAccepted: number
    qtyRestocked: number
}

// 组件Props接口
export interface ReplenishProps {
    [StationOperationType.robotArea]: RobotHandlerProps
    [StationOperationType.defaultArea]: DefaultProps
}

// 容器处理器Props
export interface ContainerHandlerProps {
    focusValue: string
    onConfirm: (params: any) => void
    changeFocusValue: (value: string) => void
    onScanSubmit?: () => void
    containerCode?: string
    disable?: boolean
    isContainerLeave?: boolean
    hasOrder?: boolean
    onActionDispatch: (action: any) => void
}

// SKU处理器Props
export interface SkuHandlerProps {
    details?: any[]
    displayQty?: boolean
    currentSkuInfo: any
    focusValue: string
    onSkuChange: (detail: any) => void
}

// 订单处理器Props
export interface OrderHandlerProps {
    value: any
}

// 接收状态
export interface ReceiveState {
    orderNo: string
    orderInfo: any
    currentSkuInfo: any
    focusValue: string
}

// API请求参数
export interface AcceptPlanParams {
    inboundPlanOrderId: string
    inboundPlanOrderDetailId: string
    warehouseCode: string
    qtyAccepted: number
    skuId: string
    targetContainerCode: string
    targetContainerSpecCode: string
    targetContainerSlotCode: string
    batchAttributes: Record<string, any>
    targetContainerId: string
    workStationId: string
}

