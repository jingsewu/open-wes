import { useState, useEffect, useRef } from 'react'
import type { InputRef } from 'antd'
import { message } from 'antd'
import { useTranslation } from 'react-i18next'
import type { SkuInfo, OrderDetail } from '../types'
import { receiveApiService } from '../services/api'
import { utils } from '../utils'

// 接收状态管理Hook
export const useReceiveState = () => {
    const [orderNo, setOrderNo] = useState("")
    const [orderInfo, setOrderInfo] = useState<any>()
    const [currentSkuInfo, setCurrentSkuInfo] = useState<any>({})
    const [focusValue, setFocusValue] = useState("")

    return {
        orderNo, setOrderNo,
        orderInfo, setOrderInfo,
        currentSkuInfo, setCurrentSkuInfo,
        focusValue, setFocusValue
    }
}

// 容器规格管理Hook
export const useContainerSpecs = () => {
    const [specOptions, setSpecOptions] = useState<any[]>([])
    const [containerSpec, setContainerSpec] = useState<any>({})
    const [containerSlotSpec, setContainerSlotSpec] = useState<any>("")
    const [activeSlot, setActiveSlot] = useState<string[]>([])

    const initializeSpecs = async (warehouseCode: string, containerType: string) => {
        try {
            const options = await receiveApiService.getContainerSpecs(warehouseCode, containerType)
            setSpecOptions(options)
            
            if (options.length > 0) {
                setContainerSpec({ containerSpecCode: options[0].value })
                setContainerSlotSpec(JSON.parse(options[0].containerSlotSpecs || "[]"))
            }
            
            return options
        } catch (error) {
            console.error("Failed to initialize container specs:", error)
            return []
        }
    }

    const updateContainerSpec = (specCode: string, specOptions: any[]) => {
        setContainerSpec((prev: any) => ({ ...prev, containerSpecCode: specCode }))
        const slotSpec = specOptions.find(item => item.value === specCode)?.containerSlotSpecs
        setContainerSlotSpec(JSON.parse(slotSpec || "[]"))
    }

    const resetSpecs = () => {
        setContainerSpec({})
        setContainerSlotSpec("")
        setActiveSlot([])
    }

    return {
        specOptions,
        containerSpec,
        containerSlotSpec,
        activeSlot,
        setActiveSlot,
        setContainerSpec,
        setContainerSlotSpec,
        initializeSpecs,
        updateContainerSpec,
        resetSpecs
    }
}

// 焦点管理Hook
export const useFocusManagement = (focusValue: string) => {
    const containerRef = useRef<InputRef>(null)
    const countRef = useRef<any>(null)

    useEffect(() => {
        if (focusValue === "container") {
            containerRef.current?.focus()
        } else if (focusValue === "count") {
            countRef.current?.focus()
        }
    }, [focusValue])

    return { containerRef, countRef }
}

// SKU扫描Hook
export const useSkuScanner = (onSkuChange: (detail: any) => void) => {
    const [skuCode, setSkuCode] = useState<string>("")
    const { t } = useTranslation()

    const scanSku = async (details?: any[]) => {
        try {
            if (details && details.length > 0) {
                // 从订单详情中查找SKU
                const detail = utils.findSkuInOrderDetails(details, skuCode)
                if (!detail) {
                    setSkuCode("")
                    message.warning(t("receive.station.warning.skuNotInOrder"))
                    return
                }
                onSkuChange(detail)
                return
            }

            // 从基础数据中查找SKU
            const sku = await receiveApiService.getSkuByCode(skuCode)
            onSkuChange({
                skuId: sku.id,
                skuCode: sku.skuCode,
                skuName: sku.skuName
            })
        } catch (error) {
            console.error("SKU lookup error:", error)
            setSkuCode("")
            message.warning(t("receive.station.warning.skuNotExist"))
        }
    }

    const resetSkuCode = () => setSkuCode("")

    return {
        skuCode,
        setSkuCode,
        scanSku,
        resetSkuCode
    }
}

// 数量控制Hook
export const useQuantityControl = () => {
    const [inputValue, setInputValue] = useState<number | string>("")

    const handleQuantityChange = {
        onChange: (value: number) => setInputValue(value),
        minus: () => {
            if (inputValue) {
                setInputValue((prev: number | string) => Math.max(0, Number(prev) - 1))
            }
        },
        plus: () => setInputValue((prev: number | string) => (Number(prev) || 0) + 1)
    }

    const resetQuantity = () => setInputValue("")

    return {
        inputValue,
        setInputValue,
        handleQuantityChange,
        resetQuantity
    }
}

