import { Divider } from "antd"
import type { InputRef } from "antd"
import classNames from "classnames/bind"
import React, {
    useEffect,
    useImperativeHandle,
    useMemo,
    useState,
    useRef
} from "react"
import { Translation, useTranslation } from "react-i18next"

import Count from "@/pages/wms/station/widgets/common/Count"
import PutWall from "@/pages/wms/station/widgets/PutWall"
import { BreathingLampClassName } from "@/pages/wms/station/widgets/PutWall/types"
import {
    ChooseArea,
    PutWallSlotStatus,
    putWallSlotsItem
} from "@/pages/wms/station/event-loop/types"
import style from "./split.module.scss"
//
const cx = classNames.bind(style)

export const putWallStatusTextMap = {
    selected: <Translation>{(t) => t("putWallArea.selected")}</Translation>,
    optional: <Translation>{(t) => t("putWallArea.optional")}</Translation>,
    disabled: <Translation>{(t) => t("putWallArea.disabled")}</Translation>
}

const SplitContent = (props: any) => {
    const { t } = useTranslation()

    const { operationsMap, refs, onCustomActionDispatch } = props
    const putWallArea = operationsMap.get(ChooseArea.putWallArea)
    const skuArea = operationsMap.get(ChooseArea.skuArea)
    const pickingViews = skuArea?.pickingViews || []

    const { putWallDisplayStyle, putWallViews, putWallTagConfigDTO } =
        putWallArea
    const currentSkuInfo = pickingViews[0]
    const [actualPickingNum, setActualPickingNum] = useState<number>(0)
    const [selectedSLot, setSelectedSLot] = useState<Partial<putWallSlotsItem>>(
        {}
    )
    const [inputStatus, setInputStatus] = useState<
        putWallSlotsItem["putWallSlotStatus"] | null
    >()
    const [location, setLocation] = useState("")
    //
    const countRef = useRef<InputRef>(null)

    useEffect(() => {
        // 拆箱时，如果只有一个可选的槽位，自动选中
        const allPutWallSlots = putWallArea?.putWallViews
            ?.map((item: any) => item.putWallSlots)
            .flat()
        const dispatchSlots = allPutWallSlots.filter(
            (item: any) =>
                // item.allowSplit &&
                item.putWallSlotStatus === PutWallSlotStatus.DISPATCH
        )

        if (dispatchSlots.length === 1) {
            setSelectedSLot(dispatchSlots[0])
        }
        countRef?.current?.focus()
    }, [putWallArea])
    // 选中槽位的待拣数量
    const toBePickedQty = useMemo(() => {
        return selectedSLot.putWallSlotDesc?.find(
            (item) => item.fieldName === "tobeOperatedQty"
        )?.fieldValue
    }, [selectedSLot])

    const newPutWallSlotDesc = (slot: any) => {
        const newList = slot.putWallSlotDesc?.map((val: any) => {
            if (val.propertyName === "pickingStatus") {
                if (selectedSLot.putWallSlotCode === slot.putWallSlotCode) {
                    return {
                        ...val,
                        putWallSlotStatus: "SELECTED",
                        propertyValue: putWallStatusTextMap.selected,
                        breathingLamp: BreathingLampClassName.WAITING_SEAL
                    }
                } else if (
                    [PutWallSlotStatus.DISPATCH].includes(
                        slot.putWallSlotStatus as PutWallSlotStatus
                    )
                    // &&
                    // slot.allowSplit
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
        return newList
    }
    const getPickingSlotStatus = (slot: any) => {
        if (slot.putWallSlotCode === selectedSLot.putWallSlotCode) {
            return {
                putWallSlotStatus: "SELECTED",
                breathingLamp: BreathingLampClassName.WAITING_SEAL
            }
        } else if (
            slot.putWallSlotStatus === PutWallSlotStatus.DISPATCH
            // &&slot.allowSplit
        ) {
            return {
                putWallSlotStatus: "OPTIONAL"
            }
        }
        return {
            putWallSlotStatus: "DISABLE"
        }
    }

    const newValue = useMemo(() => {
        return putWallViews.map((item: any) => {
            if (item.location === location && location) {
                item.active = true
            } else {
                item.active = location ? false : item.active
            }
            const slotView = item.putWallSlots.map((slot: any) => {
                const slotStatus = getPickingSlotStatus(slot)

                return {
                    ...slot,
                    putWallSlotDesc: newPutWallSlotDesc(slot),
                    ...slotStatus
                }
            })
            return { ...item, putWallSlots: slotView }
        })
    }, [putWallViews, selectedSLot, location])

    useEffect(() => {
        setActualPickingNum(Number(toBePickedQty))
    }, [toBePickedQty])

    const handleCountChange = (value: number) => {
        setActualPickingNum(value)
    }

    const handleSlotChange = (value: any) => {
        if (
            !(
                [
                    PutWallSlotStatus.OPTIONAL,
                    PutWallSlotStatus.SELECTED
                ].includes(value.putWallSlotStatus as PutWallSlotStatus)
                // &&
                // value.allowSplit
            )
        )
            return
        setSelectedSLot(value)
    }

    const handleInputStatusChange = (putWallSlotStatus: any) => {
        setInputStatus(putWallSlotStatus)
    }

    useImperativeHandle(refs, () => ({
        onCustomActionDispatch,
        pickedNumber: actualPickingNum,
        putWallSlotCode: selectedSLot.putWallSlotCode,
        inputStatus
    }))

    return (
        <div className="max-h-150 overflow-auto pt-1">
            <div>
                <PutWall
                    onSlotClick={handleSlotChange}
                    putWallDisplayStyle={putWallDisplayStyle}
                    putWallViews={newValue}
                    putWallStatusTextMap={putWallStatusTextMap}
                    putWallTagConfigDTO={putWallTagConfigDTO}
                />
            </div>
            <Divider />
            <div className="d-flex">
                <div
                    className="d-flex flex-col items-center justify-center w-60 h-48 mr-6"
                    style={{ background: "#fafafa" }}
                >
                    <div className="text-3xl font-semibold">
                        {selectedSLot.putWallSlotCode}
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
                        <span>{t("skuArea.numberToPick")}：</span>{" "}
                        {toBePickedQty}
                    </div>
                    <div className={cx("actual-qty")}>
                        <span>{t("skuArea.numberPicked")}：</span>
                        <Count
                            width={292}
                            height={48}
                            onChange={handleCountChange}
                            value={actualPickingNum}
                            max={Number(toBePickedQty)}
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
