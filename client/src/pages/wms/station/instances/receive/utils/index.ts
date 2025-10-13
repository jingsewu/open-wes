import type { OrderDetail } from '../types'

// 简化的工具函数 - 只保留真正有用的
export const utils = {
    // 解析容器槽位规格
    parseContainerSlotSpecs: (specs: string): any[] => {
        try {
            return JSON.parse(specs || "[]")
        } catch {
            return []
        }
    },

    // 在订单详情中查找SKU
    findSkuInOrderDetails: (details: OrderDetail[], skuCode: string): OrderDetail | undefined => {
        return details.find(item => item.skuCode === skuCode)
    },

    // 处理API错误
    handleApiError: (error: any, defaultMessage: string): string => {
        if (error?.response?.data?.message) {
            return error.response.data.message
        }
        if (error?.message) {
            return error.message
        }
        return defaultMessage
    }
}
