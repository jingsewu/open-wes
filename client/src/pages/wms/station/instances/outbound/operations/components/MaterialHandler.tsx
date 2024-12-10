import type { WorkLocationViews } from "@/pages/wms/station/event-loop/types"
import MaterialRack from "@/pages/wms/station/widgets/common/Shelf"
import React from "react"
import type { OperationProps } from "@/pages/wms/station/instances/types"
export interface ContainerHandlerConfirmProps {
    workLocationCode: string
}
const MaterialHandler = (
    props: OperationProps<WorkLocationViews, ContainerHandlerConfirmProps>
) => {
    const { value } = props

    const { workLocationSlots = [] } = value || {}
    const arrivedContainer = workLocationSlots?.[0]?.arrivedContainer

    return (
        <MaterialRack
            // onCustomActionDispatch={onCustomActionDispatch}
            arrivedContainer={arrivedContainer}
        />
    )
}

export default MaterialHandler
