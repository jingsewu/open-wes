import { useState, useCallback, useMemo } from 'react'
import type { InputRef } from 'antd'
import { useEffect, useRef } from 'react'
import { message } from 'antd'
import { useTranslation } from 'react-i18next'

import { useWorkStation } from '@/pages/wms/station/state'
import { MessageType } from '@/pages/wms/station/widgets/message'

import type { SkuInfo, OrderDetail } from '../types'
import { receiveApiService, createApiHandler } from '../services/api'
import { utils } from '../utils'
import { WAREHOUSE_CODE } from '../constants'

// 接收工作流Hook — 拥有所有本地状态和业务 handler
export const useReceiveWorkflow = () => {
    const [orderNo, setOrderNo] = useState("")
    const [orderInfo, setOrderInfo] = useState<any>()
    const [currentSkuInfo, setCurrentSkuInfo] = useState<any>({})
    const [focusValue, setFocusValue] = useState("")

    const { store, message: showMessage, onActionDispatch } = useWorkStation()

    const apiHandler = useMemo(
        () => createApiHandler((error: any) => {
            showMessage?.({ type: MessageType.ERROR, content: error.message })
        }),
        [showMessage]
    )

    const onScanSubmit = useCallback(async () => {
        await apiHandler(async () => {
            const orderData = await receiveApiService.queryPlan(orderNo, WAREHOUSE_CODE!)
            setOrderInfo(orderData)
            setFocusValue("sku")
        })
    }, [apiHandler, orderNo])

    const onSkuChange = useCallback((detail: any) => {
        setCurrentSkuInfo(detail)
        setFocusValue("container")
    }, [])

    const onConfirm = useCallback(async ({
        containerCode,
        containerSpecCode,
        containerId,
        activeSlot,
        inputValue
    }: any) => {
        const workStationEvent = store.workStationEvent
        await apiHandler(async () => {
            const res = await receiveApiService.acceptPlan({
                inboundPlanOrderId: orderInfo.id,
                inboundPlanOrderDetailId: currentSkuInfo.id,
                warehouseCode: WAREHOUSE_CODE!,
                qtyAccepted: inputValue,
                skuId: currentSkuInfo.skuId,
                targetContainerCode: containerCode,
                targetContainerSpecCode: containerSpecCode,
                targetContainerSlotCode: activeSlot[0],
                batchAttributes: {},
                targetContainerId: containerId,
                workStationId: workStationEvent!.workStationId
            })
            if (res.status === 200) {
                if (workStationEvent?.hasOrder) await onScanSubmit()
                setCurrentSkuInfo({})
                setFocusValue("sku")
            }
        })
    }, [apiHandler, orderInfo, currentSkuInfo, store, onScanSubmit])

    return {
        orderNo, setOrderNo,
        orderInfo,
        currentSkuInfo,
        focusValue,
        setFocusValue,
        onScanSubmit,
        onSkuChange,
        onConfirm,
        store,
        onActionDispatch
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
                const detail = utils.findSkuInOrderDetails(details, skuCode)
                if (!detail) {
                    setSkuCode("")
                    message.warning(t("receive.station.warning.skuNotInOrder"))
                    return
                }
                onSkuChange(detail)
                return
            }

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

    return { skuCode, setSkuCode, scanSku, resetSkuCode }
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

    return { inputValue, setInputValue, handleQuantityChange, resetQuantity }
}
