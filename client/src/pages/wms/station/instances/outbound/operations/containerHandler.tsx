import React from "react"

import {DevicePhysicalType, WorkLocationArea, WorkStationView} from "@/pages/wms/station/event-loop/types"
import {CustomActionType} from "@/pages/wms/station/instances/outbound/customActionType"
import MaterialHandler from "@/pages/wms/station/instances/outbound/operations/components/MaterialHandler"
import type {OutboundProps} from "@/pages/wms/station/instances/outbound/type"
import type {OperationProps} from "@/pages/wms/station/instances/types"
import EmptyImage from "@/pages/wms/station/instances/outbound/operations/components/EmptyImage";

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

const ContainerHandler = (
    props: OperationProps<WorkLocationArea, ContainerHandlerConfirmProps>
) => {
    const {value, onActionDispatch, message, isActive} = props

    const containerViews = value?.workLocationViews?.find((item) => {
        item.enable
    });
    const workLocationType = containerViews?.workLocationType || "DEFAULT"
    const handleScanContainer = async (containerCode: string) => {
        const {code, msg} = await onActionDispatch({
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
        if (false) {
            const {code, msg} = await onActionDispatch({
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
                onActionDispatch={onActionDispatch}
            />
        ),
        BUFFER_SHELVING: (
            <MaterialHandler
                value={containerViews}
                onActionDispatch={onActionDispatch}
            />
        ),
        DEFAULT: (
            <EmptyImage
                workStationEvent={value}
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
