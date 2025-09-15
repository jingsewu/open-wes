import React from "react"

import {
    DevicePhysicalType,
    WorkLocationArea,
    WorkStationView
} from "@/pages/wms/station/event-loop/types"
import MaterialHandler from "@/pages/wms/station/instances/outbound/operations/components/MaterialHandler"
import type { OutboundProps } from "@/pages/wms/station/instances/outbound/type"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import EmptyImage from "@/pages/wms/station/instances/outbound/operations/components/EmptyImage"
import { useWorkStation, observer } from "../../../state"

// 常量定义
const DEFAULT_WORK_LOCATION_TYPE = DevicePhysicalType.DEFAULT

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
        const workLocationType =
            containerViews?.workLocationType || DEFAULT_WORK_LOCATION_TYPE

        const renderMaterialHandler = () => <MaterialHandler />

        const renderContainerComponent = () => {
            const isRobotType = workLocationType === DevicePhysicalType.ROBOT
            const isBufferShelvingType =
                workLocationType === DevicePhysicalType.BUFFER_SHELVING

            if (isRobotType || isBufferShelvingType) {
                return renderMaterialHandler()
            }

            return <EmptyImage />
        }

        const isDefaultLayout = workLocationType === DevicePhysicalType.DEFAULT
        const containerStyle: React.CSSProperties = {
            height: "100%",
            display: "flex",
            flexDirection: isDefaultLayout ? "column" : "row",
            alignItems: "center",
            justifyContent: "center",
            width: "100%"
        }

        return <div style={containerStyle}>{renderContainerComponent()}</div>
    }
)

export default ContainerHandler
