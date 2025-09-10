import { Divider } from "antd"
import type { InputRef } from "antd"
import classNames from "classnames/bind"
import React, { useEffect, useImperativeHandle, useState, useRef } from "react"
import { Translation, useTranslation } from "react-i18next"

import Count from "@/pages/wms/station/widgets/common/Count"
import PutWall from "@/pages/wms/station/widgets/PutWall"
import { BreathingLampClassName } from "@/pages/wms/station/widgets/PutWall/types"
import {
    ChooseArea,
    PutWallSlotStatus,
    PutWallSlotsItem
} from "@/pages/wms/station/event-loop/types"
import { useWorkStation } from "@/pages/wms/station/state"
import style from "./split.module.scss"

const cx = classNames.bind(style)

// 类型定义
interface SplitContentProps {
    refs: React.RefObject<any> | undefined
}

interface SkuInfo {
    operationTaskDTOS: Array<{
        toBeOperatedQty: number
    }>
    skuMainDataDTO?: {
        skuName?: string
        skuBarcode?: {
            barcodes?: string
        }
    }
}

interface PutWallSlot {
    putWallSlotCode: string
    putWallSlotStatus: PutWallSlotStatus
    putWallSlotDesc?: Array<{
        propertyName: string
        propertyValue: any
        putWallSlotStatus?: string
        breathingLamp?: string
    }>
    // 其他 PutWallSlotsItem 属性
    enable?: boolean
    bay?: string
    level?: string
    face?: string
    locBay?: number
    locLevel?: number
    transferContainerCode?: string
}

interface PutWallView {
    location: string
    active?: boolean
    putWallSlots: PutWallSlot[]
}

interface PutWallArea {
    putWallDisplayStyle: any
    putWallViews: PutWallView[]
    putWallTagConfigDTO: any
}

interface SkuArea {
    pickingViews: SkuInfo[]
}

const PICKING_STATUS_PROPERTY = "pickingStatus"

export const putWallStatusTextMap = {
    selected: <Translation>{(t) => t("putWallArea.selected")}</Translation>,
    optional: <Translation>{(t) => t("putWallArea.optional")}</Translation>,
    disabled: <Translation>{(t) => t("putWallArea.disabled")}</Translation>
}

const calculateToBePickedQty = (skuInfo: SkuInfo | undefined): number => {
    if (!skuInfo?.operationTaskDTOS) return 0
    return skuInfo.operationTaskDTOS.reduce(
        (acc, cur) => acc + cur.toBeOperatedQty,
        0
    )
}

const getDispatchSlots = (
    putWallArea: PutWallArea | undefined
): PutWallSlot[] => {
    if (!putWallArea?.putWallViews) return []

    return putWallArea.putWallViews
        .map((item) => item.putWallSlots)
        .reduce((acc, slots) => [...acc, ...slots], [] as PutWallSlot[])
        .filter(
            (slot: PutWallSlot) =>
                slot.putWallSlotStatus === PutWallSlotStatus.DISPATCH
        )
}

const isSlotSelectable = (slot: PutWallSlot): boolean => {
    return [PutWallSlotStatus.OPTIONAL, PutWallSlotStatus.SELECTED].includes(
        slot.putWallSlotStatus as PutWallSlotStatus
    )
}

const SplitContent: React.FC<SplitContentProps> = ({ refs }) => {
    const { t } = useTranslation()

    const { store, onActionDispatch } = useWorkStation()
    const { operationsMap } = store

    // 数据提取
    const putWallArea = operationsMap.get(ChooseArea.putWallArea) as
        | PutWallArea
        | undefined
    const skuArea = operationsMap.get(ChooseArea.skuArea) as SkuArea | undefined
    const pickingViews = skuArea?.pickingViews || []
    const currentSkuInfo = pickingViews[0] as SkuInfo | undefined

    // 如果没有SKU信息
    if (!currentSkuInfo) {
        return (
            <div className="max-h-150 overflow-auto pt-1">
                <div className="text-center text-gray-500 py-8">
                    {t("common.noData")}
                </div>
            </div>
        )
    }

    const {
        putWallDisplayStyle,
        putWallViews = [],
        putWallTagConfigDTO
    } = putWallArea || {}

    const [actualPickingNum, setActualPickingNum] = useState<number>(0)
    const [selectedSlot, setSelectedSlot] = useState<PutWallSlot | null>(null)
    const [inputStatus, setInputStatus] = useState<
        "" | "warning" | "error" | undefined
    >(undefined)
    const [location, setLocation] = useState("")

    const countRef = useRef<InputRef>(null)

    // 计算属性
    const toBePickedQty = calculateToBePickedQty(currentSkuInfo)

    // 自动选中单个可用的槽位
    useEffect(() => {
        const dispatchSlots = getDispatchSlots(putWallArea)

        if (dispatchSlots.length === 1) {
            setSelectedSlot(dispatchSlots[0])
        }

        countRef?.current?.focus()
    }, [putWallArea])

    // 同步实际拣货数量
    useEffect(() => {
        setActualPickingNum(toBePickedQty)
    }, [toBePickedQty])

    // 更新槽位描述信息
    const updatePutWallSlotDesc = (
        slot: PutWallSlot,
        selectedSlotCode?: string
    ) => {
        return slot.putWallSlotDesc?.map((val) => {
            if (val.propertyName === PICKING_STATUS_PROPERTY) {
                if (selectedSlotCode === slot.putWallSlotCode) {
                    return {
                        ...val,
                        putWallSlotStatus: "SELECTED",
                        propertyValue: putWallStatusTextMap.selected,
                        breathingLamp: BreathingLampClassName.WAITING_SEAL
                    }
                } else if (
                    slot.putWallSlotStatus === PutWallSlotStatus.DISPATCH
                ) {
                    return {
                        ...val,
                        putWallSlotStatus: "OPTIONAL",
                        propertyValue: putWallStatusTextMap.optional
                    }
                }
                return {
                    ...val,
                    putWallSlotStatus: "SELECTED",
                    propertyValue: ""
                }
            }
            return val
        })
    }

    // 获取槽位状态
    const getPickingSlotStatus = (
        slot: PutWallSlot,
        selectedSlotCode?: string
    ) => {
        if (selectedSlotCode === slot.putWallSlotCode) {
            return {
                putWallSlotStatus: "SELECTED",
                breathingLamp: BreathingLampClassName.WAITING_SEAL
            }
        } else if (slot.putWallSlotStatus === PutWallSlotStatus.DISPATCH) {
            return {
                putWallSlotStatus: "OPTIONAL"
            }
        }
        return {
            putWallSlotStatus: "DISABLE"
        }
    }

    // 处理后的墙位视图数据
    const processedPutWallViews = putWallViews.map((item: PutWallView) => {
        const isActive = item.location === location && location
        const updatedItem = {
            ...item,
            active: isActive || (!location && item.active)
        }

        const slotView = item.putWallSlots.map((slot: PutWallSlot) => {
            const slotStatus = getPickingSlotStatus(
                slot,
                selectedSlot?.putWallSlotCode
            )
            return {
                ...slot,
                putWallSlotDesc: updatePutWallSlotDesc(
                    slot,
                    selectedSlot?.putWallSlotCode
                ),
                ...slotStatus
            }
        })

        return { ...updatedItem, putWallSlots: slotView }
    })

    const handleCountChange = (value: number) => {
        setActualPickingNum(value)
    }

    const handleSlotChange = (slot: PutWallSlotsItem) => {
        const putWallSlot = slot as PutWallSlot
        if (!isSlotSelectable(putWallSlot)) return
        setSelectedSlot(putWallSlot)
    }

    const handleInputStatusChange = (
        status: "" | "warning" | "error" | undefined,
        currentInputValue: number
    ) => {
        setInputStatus(status)
    }

    // 暴露给父组件的方法和属性
    useImperativeHandle(
        refs,
        () => ({
            onActionDispatch,
            pickedNumber: actualPickingNum,
            putWallSlotCode: selectedSlot?.putWallSlotCode,
            inputStatus
        }),
        [
            onActionDispatch,
            actualPickingNum,
            selectedSlot?.putWallSlotCode,
            inputStatus
        ]
    )

    return (
        <div className="max-h-150 overflow-auto pt-1">
            <div>
                <PutWall
                    onSlotClick={handleSlotChange}
                    putWallDisplayStyle={putWallDisplayStyle}
                    putWallViews={processedPutWallViews as any}
                    putWallStatusTextMap={putWallStatusTextMap}
                    putWallTagConfigDTO={putWallTagConfigDTO}
                />
            </div>
            <Divider />
            <div className="d-flex">
                <div
                    className="d-flex flex-col items-center justify-center mr-6"
                    style={{
                        width: 60,
                        height: 48,
                        background: "#fafafa"
                    }}
                >
                    <div className="text-3xl font-semibold">
                        {selectedSlot?.putWallSlotCode || ""}
                    </div>
                    <div className="text-md">{t("skuArea.currentSlot")}</div>
                </div>
                <div className={cx("sku-info")}>
                    <div>
                        <span>{t("skuArea.productName")}：</span>
                        {currentSkuInfo?.skuMainDataDTO?.skuName}
                    </div>
                    <div>
                        <span>{t("skuArea.barcode")}：</span>
                        {currentSkuInfo?.skuMainDataDTO?.skuBarcode?.barcodes}
                    </div>
                    <div>
                        <span>{t("skuArea.numberToPick")}：</span>
                        {toBePickedQty}
                    </div>
                    <div className={cx("actual-qty")}>
                        <span>{t("skuArea.numberPicked")}：</span>
                        <Count
                            width={292}
                            height={48}
                            onChange={handleCountChange}
                            value={actualPickingNum}
                            max={toBePickedQty}
                            handleStatusChange={handleInputStatusChange}
                            precision={0}
                            ref={countRef}
                        />
                    </div>
                </div>
            </div>
        </div>
    )
}

export default SplitContent
