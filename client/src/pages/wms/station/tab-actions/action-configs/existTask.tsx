import React from "react"
import { Translation } from "react-i18next"

import ExitSvg from "@/icon/fontIcons/exit.svg" // path to your '*.svg' file.
import { TabActionType } from "@/pages/wms/station/tab-actions/constant"
import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"
import { MessageType } from "@/pages/wms/station/widgets/message"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"

const disabledOperationType = [
    "SELECT_CONTAINER_PUT_AWAY"
]

const taskConfig: TabAction = {
    key: TabActionType.EXIT,
    name: <Translation>{(t) => t("button.exit")}</Translation>,
    modalConfig: {
        title: <Translation>{(t) => t("modal.confirmExit")}</Translation>
    },
    position: "right",
    icon: <ExitSvg />,
    modalType: TabActionModalType.CONFIRM,
    testid: "exit",
    disabled: (workStationEvent: any) => {
        return (
            disabledOperationType.includes(workStationEvent?.workStationMode) &&
            !!workStationEvent?.skuArea?.scanCode
        )
    },
    emitter: async (props) => {
        const { message, onActionDispatch } = props
        const { code, msg } = await onActionDispatch({
            eventCode: CustomActionType.OFFLINE
        })
        if (code === "-1") {
            message?.({ type: MessageType.ERROR, content: msg })
        }
        // Navigation is driven by the WebSocket DATA_CHANGED message:
        // server sends DATA_CHANGED → getApiData() returns OFFLINE →
        // workStationStore updates → header.useEffect detects OFFLINE →
        // navigates to /wms/workStation.
    }
}

export default taskConfig
