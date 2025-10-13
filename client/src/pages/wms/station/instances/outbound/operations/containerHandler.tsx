import React from "react"

import {
    DevicePhysicalType,
    WorkLocationArea
} from "@/pages/wms/station/event-loop/types"
import MaterialHandler from "@/pages/wms/station/instances/outbound/operations/components/MaterialHandler"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import EmptyImage from "@/pages/wms/station/instances/outbound/operations/components/EmptyImage"
import { useWorkStation, observer } from "../../../state"

export interface ContainerHandlerProps {
    workLocationArea: WorkLocationArea
}

export interface ContainerHandlerConfirmProps {
    containerCode: string
}

const ContainerHandler = observer(
    (props: OperationProps<WorkLocationArea, ContainerHandlerConfirmProps>) => {
        const { workStationEvent } = useWorkStation()

        const workLocationArea = workStationEvent?.workLocationArea
        const containerViews = workLocationArea?.workLocationViews?.find(
            (item) => item.enable
        )
        const workLocationType = containerViews?.workLocationType || DevicePhysicalType.DEFAULT

        const isRobotOrBufferType = [
            DevicePhysicalType.ROBOT,
            DevicePhysicalType.BUFFER_SHELVING
        ].includes(workLocationType)

        const isDefaultLayout = workLocationType === DevicePhysicalType.DEFAULT
        const containerStyle: React.CSSProperties = {
            height: "100%",
            display: "flex",
            flexDirection: isDefaultLayout ? "column" : "row",
            alignItems: "center",
            justifyContent: "center",
            width: "100%"
        }

        return (
            <div style={containerStyle}>
                {isRobotOrBufferType ? <MaterialHandler /> : <EmptyImage />}
            </div>
        )
    }
)

export default ContainerHandler
