import type {
    WorkStationView,
    PickingViewItem
} from "@/pages/wms/station/event-loop/types"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import type { OutboundProps } from "@/pages/wms/station/instances/outbound/type"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import React, { useImperativeHandle } from "react"
import OutboundSkuInfo from "./components/OutboundSkuInfo"


export interface PickerArea {
    pickingViews: PickingViewItem[]
}

export interface SKUHandlerConfirmProps {
    skuCode: string
}

const PickAreaHandler = (
    props: OperationProps<PickerArea, SKUHandlerConfirmProps>
) => {
    const { value, refs, onActionDispatch, message, isActive } = props
    useImperativeHandle(refs, () => {
        return value
    })
    const handleScanCode = async (skuCode: string) => {
        const { code, msg } = await onActionDispatch({
            eventCode: CustomActionType.SCAN_BARCODE,
            data: skuCode
        })
    }

    return (
        <OutboundSkuInfo
            value={value}
            onChange={handleScanCode}
            isActive={isActive}
        />
    )
}

export const valueFilter = (
    data: WorkStationView<OutboundProps> | undefined
): any => {
    if (!data) return {}
    return data.skuArea
}

export default PickAreaHandler
