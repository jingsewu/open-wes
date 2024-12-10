import React from "react"

import {
    WorkStationEvent,
    WorkLocationArea,
    DevicePhysicalType
} from "@/pages/wms/station/event-loop/types"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import EmptyImage from "@/pages/wms/station/instances/outbound/operations/components/EmptyImage"
import MaterialHandler from "@/pages/wms/station/instances/outbound/operations/components/MaterialHandler"
import type { OutboundProps } from "@/pages/wms/station/instances/outbound/type"
import type { OperationProps } from "@/pages/wms/station/instances/types"

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
    data: WorkStationEvent<OutboundProps> | undefined
):
    | OperationProps<
          ContainerHandlerProps,
          ContainerHandlerConfirmProps
      >["value"]
    | Record<string, any> => {
    if (!data) return {}
    return data.workLocationArea
}

const ContainerHandler = (
    props: OperationProps<WorkLocationArea, ContainerHandlerConfirmProps>
) => {
    const { value, onCustomActionDispatch, message, isActive } = props

    const workStationInfo = props.workStationInfo
    const containerViews = value?.workLocationViews?.[0]
    const workLocationType = containerViews?.workLocationType || "DEFAULT"
    const handleScanContainer = async (containerCode: string) => {
        const { code, msg } = await onCustomActionDispatch({
            eventCode: CustomActionType.CONTAINER_ARRIVED,
            data: containerCode
        })
        // if (code !== "0") {
        //     message?.({
        //         type: MessageType.ERROR,
        //         content: msg
        //     })
        //     return
        // }
    }

    const handleShowInput = async () => {
        // 是否开启多拣选位
        if (
            workStationInfo?.extendsRunningInfo?.outboundCommonConfig
                ?.conveyorMultiplePickingPositions === "TRUE" &&
            !value?.workLocationViews
        ) {
            const { code, msg } = await onCustomActionDispatch({
                eventCode: CustomActionType.CLICK_SCAN_CODE_BOX
            })
            // if (code !== "0") {
            //     message?.({
            //         type: MessageType.ERROR,
            //         content: msg
            //     })
            //     return
            // }
        }
    }

    const containerComponent = {
        ROBOT: (
            <MaterialHandler
                value={containerViews}
                onCustomActionDispatch={onCustomActionDispatch}
            />
        ),
        BUFFER_SHELVING: (
            <MaterialHandler
                value={containerViews}
                onCustomActionDispatch={onCustomActionDispatch}
            />
        ),
        DEFAULT: (
            <EmptyImage
                workStationInfo={workStationInfo}
                handleShowInput={handleShowInput}
                onChange={handleScanContainer}
            />
        )
    }
    return (
        <div
            style={{
                height: "100%",
                display: "flex",
                flexDirection:
                    workLocationType === DevicePhysicalType.DEFAULT
                        ? "column"
                        : "row",
                alignItems: "center",
                justifyContent: "center",
                width: "100%"
            }}
        >
            {containerComponent[workLocationType]}
        </div>
    )
}

export default ContainerHandler
