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

/**
 * @Description: 对event中的数据进行filter处理
 * @param data
 */
export const valueFilter = (
    data: WorkStationView<OutboundProps> | undefined
):
    | OperationProps<
          ContainerHandlerProps,
          ContainerHandlerConfirmProps
      >["value"]
    | Record<string, any> => {
    if (!data) return {}
    return data.workLocationArea
}

const ContainerHandler = observer(
    (props: OperationProps<WorkLocationArea, ContainerHandlerConfirmProps>) => {
        const { value } = props
        const { onActionDispatch } = useWorkStation()

        // 查找启用的工作位置视图
        const containerViews = value?.workLocationViews?.find(
            (item) => item.enable
        )
        const workLocationType =
            containerViews?.workLocationType || DEFAULT_WORK_LOCATION_TYPE

        // 渲染 MaterialHandler 组件
        const renderMaterialHandler = () => (
            <MaterialHandler
                value={containerViews}
                onActionDispatch={onActionDispatch}
            />
        )

        // 渲染对应的组件
        const renderContainerComponent = () => {
            // 检查是否为需要 MaterialHandler 的类型
            const isRobotType = workLocationType === DevicePhysicalType.ROBOT
            const isBufferShelvingType =
                (workLocationType as string) ===
                DevicePhysicalType.BUFFER_SHELVING

            if (isRobotType || isBufferShelvingType) {
                return renderMaterialHandler()
            }

            return <EmptyImage workStationEvent={value} />
        }

        // 根据工作位置类型确定布局方向
        const isDefaultLayout = workLocationType === DevicePhysicalType.DEFAULT

        // 容器样式
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
