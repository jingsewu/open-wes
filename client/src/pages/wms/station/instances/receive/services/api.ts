import request from "@/utils/requestInterceptor"
import { request_select_container_spec } from "@/pages/wms/constants/select_search_api_contant"
import { API_ENDPOINTS } from '../constants'
import type { AcceptPlanParams } from '../types'

// 接收模块API服务
export const receiveApiService = {
    // 查询入库计划
    async queryPlan(orderNo: string, warehouseCode: string) {
        const response = await request({
            method: "post",
            url: API_ENDPOINTS.QUERY_PLAN(orderNo, warehouseCode)
        }) as any
        return response.data.data
    },

    // 接受入库计划
    async acceptPlan(params: AcceptPlanParams) {
        const response = await request({
            method: "post",
            url: API_ENDPOINTS.ACCEPT_PLAN,
            data: params,
            headers: { "Content-Type": "application/json" }
        }) as any
        return response
    },

    // 获取容器信息
    async getContainer(containerCode: string, warehouseCode: string) {
        const response = await request({
            method: "post",
            url: API_ENDPOINTS.GET_CONTAINER(containerCode, warehouseCode)
        }) as any
        return response.data
    },

    // 根据SKU代码获取SKU信息
    async getSkuByCode(skuCode: string) {
        const response = await request({
            method: "post",
            url: API_ENDPOINTS.GET_SKU_BY_CODE(skuCode),
            headers: { "Content-Type": "application/json" }
        }) as any
        
        if (response.status === 200 && response.data?.length > 0) {
            return response.data[0]
        }
        throw new Error("SKU not found")
    },

    // 获取容器规格
    async getContainerSpecs(warehouseCode: string, containerType: string) {
        const response = await request_select_container_spec(
            warehouseCode,
            containerType
        ) as any
        return response?.data?.options || []
    },

    // 完成容器接收
    async completeByContainer(containerCode: string) {
        const response = await request({
            method: "post",
            url: API_ENDPOINTS.COMPLETE_BY_CONTAINER(containerCode)
        }) as any
        return response
    }
}

// 通用API请求处理器
export const createApiHandler = (onError?: (error: any) => void) => {
    return async (apiCall: () => Promise<any>) => {
        try {
            return await apiCall()
        } catch (error: any) {
            console.error("API request failed:", error)
            if (onError) {
                onError(error)
            } else {
                throw error
            }
        }
    }
}

