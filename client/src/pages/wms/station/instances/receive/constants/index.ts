import type { OperationUrls, ReplenishTypeUrlMapType } from '../types'

// 基础常量
export const WAREHOUSE_CODE = localStorage.getItem("warehouseCode")
export const CONTAINER_TYPE = "CONTAINER"

// 样式常量
export const BUTTON_STYLE = {
    borderRight: "1px solid #ccc",
    borderLeft: "1px solid #ccc"
} as const

// 生成操作URL的工厂函数
const createOperationUrls = (prefix: string, deviceType: string): OperationUrls => {
    if (deviceType === 'CONVEYOR') {
        return {
            sanCode: `${prefix}_${deviceType}_CHOOSE_SCAN_CODE_OPERATION_CONFIRM`,
            confirm: `${prefix}_${deviceType}_CONFIRM_OPERATION_CONFIRM`,
            switchConfirm: `${prefix}_EXCHANGE_${deviceType}_CONFIRM_EVENT`,
            switchSanCode: `${prefix}_${deviceType}_EXCHANGE_CHOOSE_SCAN_CODE_EVENT`,
            full: `${prefix}_${deviceType}_DONE_RECEIVED_EVENT`,
            skuContainerSwitch: `${prefix}_SHELF_EXCHANGE_MATCH_MODE_EVENT`
        }
    } else if (deviceType === 'SHELF') {
        return {
            sanCode: `${prefix}_${deviceType}_SCAN_CODE_OPERATION_CONFIRM`,
            confirm: `${prefix}_${deviceType}_CONFIRM_OPERATION_CONFIRM`,
            switchConfirm: `${prefix}_${deviceType}_EXCHANGE_CONFIRM_EVENT`,
            switchSanCode: `${prefix}_${deviceType}_EXCHANGE_CHOOSE_SCAN_CODE_EVENT`,
            full: `${prefix}_${deviceType}_DONE_RECEIVED_EVENT`,
            skuContainerSwitch: `${prefix}_${deviceType}_EXCHANGE_MATCH_MODE_EVENT`
        }
    }
    
    // 默认返回基础格式
    return {
        sanCode: `${prefix}_${deviceType}_SCAN_CODE_OPERATION_CONFIRM`,
        confirm: `${prefix}_${deviceType}_CONFIRM_OPERATION_CONFIRM`,
        full: `${prefix}_${deviceType}_DONE_RECEIVED_EVENT`
    }
}

// 机器人特殊配置
const ROBOT_CONFIG: OperationUrls = {
    sanCode: "INBOUND_RECEIVE_ROBOT_SCAN_CODE_OPERATION_CONFIRM",
    confirm: "INBOUND_ROBOT_SELECT_CONTAINER_PUTAWAY_CONFIRM_OPERATION_CONFIRM",
    switchConfirm: "INBOUND_ROBOT_EXCHANGE_SELECT_CONTAINER_PUTAWAY_RECEIVE_CONFIRM_EVENT",
    switchSanCode: "INBOUND_ROBOT_EXCHANGE_SELECT_CONTAINER_PUTAWAY_SCAN_CODE_EVENT",
    full: "INBOUND_ROBOT_SELECT_CONTAINER_PUTAWAY_DONE_RECEIVED_EVENT"
}

// 补货类型URL映射
export const ReplenishTypeUrlMap: ReplenishTypeUrlMapType = {
    MANUAL: {
        CONVEYOR: createOperationUrls("INBOUND_RECEIVE_MANUAL", "CONVEYOR"),
        SHELF: createOperationUrls("INBOUND_RECEIVE_MANUAL", "SHELF"),
        ROBOT: ROBOT_CONFIG
    },
    RECOMMEND: {
        CONVEYOR: createOperationUrls("INBOUND_RECEIVE_RECOMMEND", "CONVEYOR"),
        SHELF: createOperationUrls("INBOUND_RECEIVE_RECOMMEND", "SHELF"),
        ROBOT: ROBOT_CONFIG
    },
    NONE: {
        CONVEYOR: createOperationUrls("INBOUND_RECEIVE_MANUAL", "CONVEYOR"),
        ROBOT: ROBOT_CONFIG
    }
}

// API端点
export const API_ENDPOINTS = {
    QUERY_PLAN: (orderNo: string, warehouseCode: string) => 
        `/wms/inbound/plan/query/${orderNo}/${warehouseCode}`,
    ACCEPT_PLAN: "/wms/inbound/plan/accept",
    GET_CONTAINER: (containerCode: string, warehouseCode: string) => 
        `/wms/basic/container/get?containerCode=${containerCode}&warehouseCode=${warehouseCode}`,
    GET_SKU_BY_CODE: (skuCode: string) => 
        `/wms/basic/sku/getBySkuCode?skuCode=${skuCode}`,
    COMPLETE_BY_CONTAINER: (containerCode: string) => 
        `/wms/inbound/accept/completeByContainer?containerCode=${containerCode}`
} as const

