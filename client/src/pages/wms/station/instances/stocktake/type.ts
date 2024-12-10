// import type { Rule } from "@/config/JS/Project/project.types"

// import type { ContainerHandlerProps } from "./operations/ConveyorHandler"
// import type { RobotHandlerProps } from "./operations/RobotHandler"
// import type { ShelfHandlerProps } from "./operations/ShelfHandler"
import type { StationProcessingStatus } from "@/pages/wms/station/event-loop/types"

export enum StationOperationType {
    conveyorArea = "conveyorArea",
    robotArea = "robotArea",
    shelfArea = "shelfArea",
    stocktakeArea = "stocktakeArea",
    collectingDoc = "collectingDoc",
    selectBatchTipArea = "selectBatchTipArea",
    selectEmptyTipArea = "selectEmptyTipArea",
    defaultArea = "defaultArea",
    skuArea = "skuArea",
    orderArea = "orderArea"
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

export interface StocktakeProps {
    // conveyorArea: ContainerHandlerProps
    // robotArea: RobotHandlerProps
    // shelfArea: ShelfHandlerProps
    stocktakeArea: StocktakeHanlerProps
    stocktakeConfig: StocktakeConfig
    currentDevicePhysicalType: StationPhysicalType
    rotationAngle: number
    stationProcessingStatus: StationProcessingStatus
}

export enum StocktakeEventCode {
    /** 领取任务 */
    receiveTask = "STOCK_TAKE_RECEIVE_TASK_EVENT",
    /** 单行扫码 */
    singleLineScanCode = "STOCK_TAKE_SINGLE_LINE_SCAN_CODE_EVENT",
    /** 多行扫码 */
    multiLineScanCode = "STOCK_TAKE_MULTI_LINE_SCAN_CODE_EVENT",
    /** 完成盘点事件 */
    doneStocktake = "STOCK_TAKE_DONE_TASK_EVENT",
    /** 盘点数量统计事件 */
    countStocktake = "STOCK_TAKE_SCAN_CODE_TAKE_COUNT_EVENT",
    /** 切换任务 */
    changeStocktakeTask = "STOCK_TAKE_MULTI_LINE_CHANGE_DATA_EVENT",
    /** 一键完成盘点任务 */
    doneAllStocktake = "STOCK_TAKE_DONE_ALL_TASK_EVENT",
    /** 扫码料箱 */
    sanContainer = "STOCK_TAKE_SCAN_CONTAINER_EVENT",
    /** 盘点盈品事件 */
    additionalProduct = "STOCK_TAKE_SURPLUS_EVENT",
    /** 切换到输送线扫码事件 */
    switchConveyorScan = "DOUBLE_STOCKTAKE_POSITION_EVENT"
}

export interface StocktakeHanlerProps {
    processingTaskDetails: CurrentTaskDetailDto[]
    currentTaskDetailDTO?: CurrentTaskDetailDto
    resolveVo?: { code: string }
    containerLayoutInfoDTO: { subContainerList: any[] }
}
export interface CurrentTaskDetailDto {
    stocktakeMethod: string
    stocktakeType: string
    ksStocktakeTaskNo: string
    ksStocktakeTaskDetailNo: string
    ksStocktakeOrderDetailNo: string
    skuCode: string
    snCode: string
    skuName: string
    containerCode: string
    qtyOriginal: number
    qtyStocktake: number
    batchNumber: string
    batchAttributesDetails: any[]
    stationCode: string
    orderNo: string
    numberOfTotes: number
    ksStockIdList: any[]
    imageAddress: string
    snStockIdList: any[]
    noBarcode: boolean
    // amountDisplayRule?: Rule
}

interface StocktakeConfig {
    pickType: PickTypeEnum
    multipleTaskExecutable: "TRUE" | "FALSE"
}

export enum PickTypeEnum {
    /** 单行拣选（逐件） */
    PIECE = "PIECE",
    /** 多行拣选（手工输入） */
    ORDER_LINE = "ORDER_LINE"
}

export enum StocktakeMethodEnum {
    /** 明盘 */
    INFORMED = "INFORMED",
    /** 盲盘(数量) */
    BLIND_QTY = "BLIND_QTY"
}
