import React from "react"
import { Translation } from "react-i18next"

import StopSvg from "@/icon/fontIcons/stop.svg" // path to your '*.svg' file.
import { TabActionType } from "@/pages/wms/station/tab-actions/constant"
import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"

const taskConfig: TabAction = {
    key: TabActionType.STOP_TASK,
    name: <Translation>{(t) => t("button.stopTask")}</Translation>,
    position: "right",
    testid: "stopTask",
    // <IntlMessages id="workstaion.outbound.action.stopTask" />,
    icon: <StopSvg />,
    modalType: TabActionModalType.NONE,
    hide: (workStationEvent: any) => {
        // 初始undefined时hide
        return (
            !workStationEvent ||
            workStationEvent?.workStationStatus === "PAUSED"
        )
    },
    emitter: (payload) => {
        return Promise.resolve()
    }
}

export default taskConfig
