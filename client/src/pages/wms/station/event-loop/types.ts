import type { ReactChild, ReactChildren } from "react"

import type {
    DebuggerConfig,
    OperationProps
} from "@/pages/wms/station/instances/types"

export interface InfoModel {
    id: number
}

export interface ActionModel {
    id: number
}

export enum ChooseArea {
    workLocationArea = "CONTAINER_AREA",
    skuArea = "SKU_AREA",
    putWallArea = "PUT_WALL_AREA",
    tips = "TIPS"
}

export enum OperationType {
    RECEIVE = "RECEIVE" /** 收货 */,
    PUT_AWAY = "PUT_AWAY" /** 上架 */,
    PICKING = "PICKING" /** 拣货 */,
    SORTING = "SORTING",
    ONE_STEP_RELOCATION = "ONE_STEP_RELOCATION" /** 一步式理库 */,
    RECOMMEND_REPLENISH = "RECOMMEND_REPLENISH" /** 推荐容器补货 */,
    SELECT_CONTAINER_PUT_AWAY = "SELECT_CONTAINER_PUT_AWAY" /** 选择容器上架 */,
    WITHOUT_ORDER_PUT_AWAY = "WITHOUT_ORDER_PUT_AWAY" /** 无单上架 */,
    RELOCATION = "RELOCATION" /** 理库 */,
    COUNTING = "COUNTING" /** 盘点 */,
    RECHECK = "RECHECK" /** 复核 */
}

export interface ToolBar {
    enableReleaseSlot: boolean
    enableReportAbnormal: boolean
    enableSplitContainer: boolean
}

export enum ProcessStatusEnum {
    /** 进行中的任务 */
    PROCESSING,
    /** 未开始的任务 */
    UNDO
}

export interface OperationTaskDTOS {
    sourceContainerId: any
    processStatus: ProcessStatusEnum
    taskStatus: ProcessStatusEnum
    receivedQty: number
    detailId: string
    toBeOperatedQty: number
    requiredQty: number
    skuBatchStockId: number
    extendInfo: ExtendInfo
    id?: string
    targetContainerCode?: string
    targetContainerFace?: string
    targetContainerSlotCode?: string
    targetContainerId?: string
}

interface ExtendInfo {
    stocktakeOrderNo: string
    stocktakeMethod: string
    stocktakeType: string
}

export interface SkuAttributes {
    [key: string]: string
}

export interface SkuBatchAttributeDTO {
    skuAttributes: SkuAttributes
}

export interface SkuMainDataDTO {
    skuCode: string
    skuName: string
    skuAttribute?: { imageUrl: string }
    skuBarcode: { barcodes: string[] }
}

export interface pickingViewItem {
    operationTaskDTOS: OperationTaskDTOS[]
    skuBatchAttributeDTO: SkuBatchAttributeDTO
    skuMainDataDTO: SkuMainDataDTO
}

export interface SkuArea {
    pickingViews: pickingViewItem[]
    withoutOrderSkuInfos?: any[]
}

export enum PutWallSlotStatus {
    /** 空闲 */
    IDLE = "IDLE",
    /** 已绑定 */
    BOUND = "BOUND",
    /** 待绑定 */
    WAITING_BINDING = "WAITING_BINDING",
    /** 待封箱 */
    WAITING_SEAL = "WAITING_SEAL",
    /** 待分拨 */
    DISPATCH = "DISPATCH",
    /** 待挂起 拆箱用 */
    // WAITING_HANG = "WAITING_HANG",
    /** 禁用 处理enable: ture => DISABLE */
    OPTIONAL = "OPTIONAL",
    SELECTED = "SELECTED",
    DISABLE = "DISABLE"

    /** 拣货挂单拆箱选中状态（不在标准状态之中） */
    // _SELECTED = "_SELECTED"
}

export enum PutWallSlotStatusEnum {
    IDLE = "idle",
    WAITING_BINDING = "waitingBinding",
    BOUND = "bound",
    DISPATCH = "dispatch",
    WAITING_SEAL = "waitingSeal",
    DISABLE = "disabled",
    OPTIONAL = "optional",
    SELECTED = "selected"
}

export interface putWallSlotsItem {
    /** 是否禁用 */
    enable: boolean
    /** 列编码 */
    bay: string
    /** 行编码 */
    level: string
    /** 槽口编码 */
    putWallSlotCode: string
    /** 面 */
    face: string
    /** 第几列 */
    locBay: number
    /** 第几行 */
    locLevel: number
    /** 槽口状态 */
    putWallSlotStatus: PutWallSlotStatus
    /** 周转箱编码 */
    transferContainerCode?: string
    /** 周转箱编码 */
    putWallSlotDesc?: PutWallSlotDesc[]
}

export interface PutWallSlotDesc {
    textSize: number
    bold: string
    color: any
    fieldName: string
    fieldValue: string
}

export type Location = "LEFT" | "RIGHT"

// "LEFT_TO_RIGHT" | "RIGHT_TO_LEFT"  播种墙列排序 从左到右/从右到左
export enum DisplayOrder {
    RIGHT_TO_LEFT = "RIGHT_TO_LEFT",
    LEFT_TO_RIGHT = "LEFT_TO_RIGHT"
}

export interface putWallViewsItem {
    /** 播种墙位置 */
    location: Location
    putWallSlots: putWallSlotsItem[]
    active?: boolean
    showHasTask?: boolean
    displayOrder?: DisplayOrder
    containerSpec?: {
        width: number
    }
}

export enum PutWallDisplayStyle {
    /** 播种墙合并显示 */
    merge = "merge",
    /** 播种墙tab显示 */
    split = "split"
}

export interface putWallArea {
    /** 播种墙展示样式  合并|分开 */
    putWallDisplayStyle: PutWallDisplayStyle
    putWallViews: putWallViewsItem[]
    putWallTagConfigDTO: PutWallTagConfigDTO
}

export interface PutWallTagConfigDTO {
    [key: string]: putWallSlotColor
}

export interface putWallSlotColor {
    /** 播种墙槽位背景色 */
    color: string
    /** 播种墙颜色闪烁/常亮 */
    mode: "FLASH" | "ON"
    /** 是否可拍灯 */
    updown: "TAPABLE" | "UNTAPABLE"
}

export type StationProcessingStatus =
    | "NO_TASK"
    | "WAIT_ROBOT"
    | "WAIT_CALL_CONTAINER"

export interface OrderList {
    active: boolean
    customerOrderNo: string
    lpnCode: string
    orderId: string
    orderNo: string
    qtyNeed: number
    qtyReceived: number
    receivedSkuTypes: number
}

export interface OrderArea {
    currentStocktakeOrder: StocktakeOrder
}

export interface StocktakeOrder {
    taskNo: string
    stocktakeCreateMethod: string
    stocktakeMethod: string
    stocktakeType: string
}

export interface WorkStationEvent<T> {
    // workStationEvent?: SworkStationInfoInterface<T>
    /** 操作台类型 */
    operationType: OperationType
    /** 工作站编码 */
    stationCode: string
    /** 工作站是否上线 */
    workStationStatus: WorkStationStatus
    /** 选择的操作区域 */
    chooseArea: ChooseArea
    /** 商品区域信息 */
    skuArea: SkuArea
    /** 播种墙区域信息 */
    putWallArea: putWallArea
    /** 客户订单区域信息 */
    orderArea: OrderList[]
    operationOrderArea: OrderArea
    /** 工作位容器信息 */
    workLocationArea: WorkLocationArea
    tips: any
    /** 操作按钮 */
    toolbar: ToolBar
    /** 选择容器上架，工作站任务状态 */
    stationProcessingStatus?: StationProcessingStatus
    /** 已扫描的barcode */
    scanCode?: string
    /** 仓库编码 */
    warehouseCode?: string
    processingType?: "WHOLE_BOX" | "NO_ORDER" | "NORMAL"
    /** 一品多批选中的明细id */
    processingInboundOrderDetailId: string
    /** 已呼叫容器数 */
    callContainerCount?: number
    warehouseAreaId?: string
    workStationId: string
}

export interface ContainerSlotSpecsItem {
    /** 列编码 */
    bay: string
    /** 行编码 */
    level: string
    /** 容器格口编码 */
    containerSlotSpecCode: string
    face: string
    /** 容器第几行 */
    locBay: number
    /** 容器第几列 */
    locLevel: number
}

export interface ContainerSpec {
    containerSlotNum: number
    /** 容器类型编码 */
    containerSpecCode: string
    containerSlotSpecs: ContainerSlotSpecsItem[]
}

export interface ArrivedContainer {
    /** 容器编码 */
    containerCode: string
    /** 容器面 */
    face: string
    locationCode: string
    processStatus: ProcessStatusEnum
    containerSpec: ContainerSpec
    containerAttributes?: ContainerAttributes
}

interface ContainerAttributes {
    skuMaxQty?: number
    skuTotalQty?: number
    subSpecCode?: string
    locationCode?: string
    stockIds?: string
}

export interface WorkLocationSlotsItem {
    enable: boolean
    workLocationCode: string
    arrivedContainer: ArrivedContainer
}

export interface WorkLocationViews {
    enable: boolean
    /** 操作台编码 */
    stationCode: string
    terminalType: string
    /** 工作位编码 */
    workLocationCode: string
    workLocationSlots: WorkLocationSlotsItem[]
    /** 操作台类型 */
    workLocationType: DevicePhysicalType
}

export interface WorkLocationArea {
    workLocationViews: WorkLocationViews[]
}
interface SworkStationInfoInterface<T> {
    stationStatus: string
    extendsInfo: {
        chooseArea: string
    } & T
}

export interface WorkStationEventLoopConfig {
    /** 轮询事件url */
    queryURL: string
    /** 操作确认url */
    confirmURL: string
    /** 轮询间隔时间 */
    pollingInterval: number
    /** 工作站编码 */
    stationCode: string
    /** 发送事件url */
    sendEventURL: string
    /** 获取工作站信息url */
    getWorkStationInfoURL: string
}

type GetCurrentOperationResponse<BusinessData> = BusinessData & {
    operationId: string // 操作ID
    operationType: string // 操作类型
}

export type GetCurrentOperation<T> = (
    stationCode: string
) => Promise<GetCurrentOperationResponse<T>>

type OperationConfirmPayload<BusinessData> = BusinessData & {
    stationCode: string
    operationId: string
    operationType: string
}

interface OperationConfirmResponse {
    code: number
    msg: string
    data?: Record<string, any>
}

export type OperationConfirm<T> = (
    payload: OperationConfirmPayload<T>
) => Promise<OperationConfirmResponse>

export interface WorkStationProviderProps extends DebuggerConfig {
    stationCode: string
    type: string
    children: ReactChildren | ReactChild
}

export interface WorkStationContextProps {
    workStationEvent: WorkStationEvent<any> | undefined
    workStationInfo: WorkStationInfo
}

export type WorkStationAPIContextProps = Pick<
    OperationProps<any, any>,
    "onConfirm" | "onCustomActionDispatch" | "message"
>

export interface SendEventPayload {
    eventCode: string
    [key: string]: any
}

export enum WorkStationStatus {
    /** 离线 */
    OFFLINE = "OFFLINE",
    /** 在线 */
    ONLINE = "ONLINE",
    /** 暂停 */
    PAUSED = "PAUSED",
    /** 无任务 */
    NO_TASK = "NO_TASK",
    /** 等待机器人中 */
    WAIT_ROBOT = "WAIT_ROBOT",
    /** 等待呼叫容器 */
    WAIT_CALL_CONTAINER = "WAIT_CALL_CONTAINER",
    /** 等待容器中 */
    WAIT_CONTAINER = "WAIT_CONTAINER",
    /** 作业中 */
    DO_OPERATION = "DO_OPERATION"
}

export enum CurrentOperationType {
    /** 未设置 */
    NONE = "NONE",
    /** 推荐 */
    RECOMMEND = "RECOMMEND",
    /** 手动 */
    MANUAL = "MANUAL"
}

export enum DevicePhysicalType {
    /** 人机 */
    ROBOT = "ROBOT",
    /** 默认空图片 */
    DEFAULT = "DEFAULT"
}

const enum emptyContainerOutboundEnum {
    // 当前没有任务
    NO_TASK = "NO_TASK",
    // 等待机器人到来
    WAIT_ROBOT = "WAIT_ROBOT",
    // 当前正在操作
    DO_OPERATION = "DO_OPERATION"
}
interface ExtendsRunningInfoFace {
    emptyContainerOutboundWorkStatus?: emptyContainerOutboundEnum
    outboundCommonConfig?: Record<string, any>
    runningConfig?: Record<string, any>
}
export interface WorkStationInfo {
    /** 工作站编码 */
    stationCode: string
    /** 工作站状态uuid 以识别工作站信息变化 */
    runningStatusUUID: string
    /** 工作站状态 */
    stationStatus: WorkStationStatus
    /** 执行中的任务编码集合 */
    executingTaskCodes: number[]
    /** 呼叫机器人数量 */
    callRobotNum: number
    /** 所有容器编码集合 */
    allContainerCodeList: string[]
    /** 在途容器编码集合 */
    inTransitContainerCodeList: string[]
    /** 当前操作类型 */
    currentOperationType: CurrentOperationType
    /** 拥有的物理设备类型集合 */
    devicePhysicalTypeList: DevicePhysicalType[]
    // 空箱出庫扩展需要
    extendsRunningInfo?: ExtendsRunningInfoFace
}
