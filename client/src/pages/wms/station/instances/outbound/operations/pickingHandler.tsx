import type {
    WorkStationView,
    PickingViewItem
} from "@/pages/wms/station/event-loop/types"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import type { OutboundProps } from "@/pages/wms/station/instances/outbound/type"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import React, { useImperativeHandle } from "react"
import OutboundSkuInfo from "./components/OutboundSkuInfo"
import { useWorkStation } from "../../../state"

export interface PickerArea {
    pickingViews: PickingViewItem[]
}

export interface SKUHandlerConfirmProps {
    skuCode: string
}

const PickAreaHandler = (
    props: OperationProps<PickerArea, SKUHandlerConfirmProps>
) => {
    const { refs, isActive } = props
    const { workStationEvent } = useWorkStation()

    const value = workStationEvent?.skuArea

    useImperativeHandle(refs, () => {
        return value
    })

    return <OutboundSkuInfo isActive={isActive} />
}

export default PickAreaHandler
