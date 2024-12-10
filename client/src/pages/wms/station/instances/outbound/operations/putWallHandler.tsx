import React, { useImperativeHandle } from "react"
import { Translation } from "react-i18next"
import {
    WorkStationEvent,
    putWallSlotsItem,
    PutWallSlotStatus
} from "@/pages/wms/station/event-loop/types"
import type { OutboundProps } from "@/pages/wms/station/instances/outbound/type"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import PutWall from "@/pages/wms/station/widgets/PutWall"
import type { PutWallProps } from "../../../widgets/PutWall/types"

const putWallStatusTextMap = {
    waitingBinding: (
        <Translation>{(t) => t("putWallArea.bindBox")}</Translation>
    ),
    dispatch: <Translation>{(t) => t("putWallArea.sowing")}</Translation>,
    waitingSeal: <Translation>{(t) => t("putWallArea.seal")}</Translation>,
    disabled: <Translation>{(t) => t("putWallArea.disabled")}</Translation>
}

type PutWallHandlerProps = Pick<
    PutWallProps,
    "putWallDisplayStyle" | "putWallViews" | "putWallTagConfigDTO"
>

export const valueFilter = (
    data: WorkStationEvent<OutboundProps> | undefined
): any => {
    if (!data) return {}
    return {
        putWallViews: data.putWallArea?.putWallViews || [],
        putWallDisplayStyle: data.putWallArea?.putWallDisplayStyle,
        putWallTagConfigDTO: data.putWallArea?.putWallTagConfigDTO || {},
        chooseType: data.chooseArea
    }
}

const PutWallHandler = (props: OperationProps<PutWallHandlerProps, any>) => {
    const {
        value = {},
        onCustomActionDispatch,
        message,
        isActive,
        refs
    } = props

    useImperativeHandle(refs, () => {
        return value
    })

    const handleSlotClick = async (item: putWallSlotsItem) => {
        if (
            ![
                PutWallSlotStatus.WAITING_SEAL,
                PutWallSlotStatus.DISPATCH
                // PutWallStatus.WAITING_HANG
            ].includes(item.putWallSlotStatus as PutWallSlotStatus)
        ) {
            return
        }

        const { code, msg } = await onCustomActionDispatch({
            eventCode: "TAP_PUT_WALL_SLOT",
            data: {
                putWallSlotCode: item.putWallSlotCode
            }
        })
    }

    return (
        <PutWall
            {...value}
            isActive={isActive}
            putWallStatusTextMap={putWallStatusTextMap}
            onSlotClick={handleSlotClick}
        />
    )
}

export default PutWallHandler
