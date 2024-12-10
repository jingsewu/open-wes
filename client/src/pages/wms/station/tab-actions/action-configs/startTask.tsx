import React from "react"
import { Translation } from "react-i18next"

import StartSvg from "@/icon/station/icon_left_pzzx.svg" // path to your '*.svg' file.
import { TabActionType } from "@/pages/wms/station/tab-actions/constant"
import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"

const taskConfig: TabAction = {
    key: TabActionType.START_TASK,
    name: <Translation>{(t) => t("button.startTask")}</Translation>,
    position: "right",
    testid: "startTask",
    // <IntlMessages id="workstaion.outbound.action.startTask" />,
    icon: <StartSvg />,
    modalType: TabActionModalType.NONE,
    hide: (workStationEvent: any) => {
        return workStationEvent?.workStationStatus === "ONLINE"
    },
    emitter: (payload) => {
        return Promise.resolve()
    }
}

export default taskConfig
