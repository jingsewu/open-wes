import type { WorkStationEvent } from "@/pages/wms/station/event-loop/types"
import type { replenishProps } from "@/pages/wms/station/instances/receive/type"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import MaterialRack from "@/pages/wms/station/widgets/common/Shelf"
import React from "react"
import { Typography } from "antd"
const { Title } = Typography

export interface ContainerHandlerConfirmProps {
    operationType: string
    operationId: string
    operationConfirmInfo: OperationConfirmInfo
}
interface OperationConfirmInfo {
    subContainerCode?: string
    containerCode?: string
}
export interface RobotHandlerProps {
    robotArea: any
    operationType: string
}

export enum MachineType {
    TANK_FEEDING_ROBOT = "TANK_FEEDING_ROBOT", // ("料箱机器人")
    LITTLE_LATENT_ROBOT = "LITTLE_LATENT_ROBOT", // ("小潜伏机器人-搬料箱")
    LARGE_LATENT_ROBOT = "LARGE_LATENT_ROBOT" // ("大潜伏机器人-搬料架")
}

/**
 * @Description: 对event中的数据进行filter处理
 * @param data
 */
export const valueFilter = (
    data: WorkStationEvent<replenishProps> | undefined
):
    | OperationProps<RobotHandlerProps, ContainerHandlerConfirmProps>["value"]
    | Record<string, any> => {
    if (!data) return {}
    return {
        robotArea: data.workLocationArea,
        // operationId: data.operationId,
        operationType: data.operationType
    }
}

const RobotHandler = (
    props: OperationProps<RobotHandlerProps, ContainerHandlerConfirmProps>
) => {
    const { value } = props

    const { robotArea } = value || {}
    const arrivedContainer =
        robotArea?.workLocationViews?.[0].workLocationSlots?.[0]
            ?.arrivedContainer

    return (
        <div className="h-full w-full">
            <Title level={3} className="pb-6">
                Container
            </Title>
            <div
                style={{
                    height: "100%",
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    // justifyContent: "center",
                    width: "100%"
                }}
            >
                <div
                    style={{
                        height: "70%",
                        width: "70%"
                    }}
                >
                    <MaterialRack
                        arrivedContainer={arrivedContainer}
                        showSlotCode={true}
                    />
                </div>
            </div>
        </div>
    )
}

export default RobotHandler
