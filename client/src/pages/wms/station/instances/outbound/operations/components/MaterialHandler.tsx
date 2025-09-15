import type { WorkLocationViews } from "@/pages/wms/station/event-loop/types"
import MaterialRack from "@/pages/wms/station/widgets/common/Shelf"
import React from "react"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import { useWorkStation } from "@/pages/wms/station/state"

export interface ContainerHandlerConfirmProps {
    workLocationCode: string
}

const MaterialHandler = (
    props: OperationProps<WorkLocationViews, ContainerHandlerConfirmProps>
) => {
    const { workStationEvent, onActionDispatch } = useWorkStation()

    const workLocationArea = workStationEvent?.workLocationArea
    const containerViews = workLocationArea?.workLocationViews?.find(
        (item) => item.enable
    )
    const { workLocationSlots = [] } = containerViews || {}
    const arrivedContainer = workLocationSlots?.[0]?.arrivedContainer

    return (
        <MaterialRack
            onActionDispatch={onActionDispatch}
            arrivedContainer={arrivedContainer}
        />
    )
}

export default MaterialHandler
