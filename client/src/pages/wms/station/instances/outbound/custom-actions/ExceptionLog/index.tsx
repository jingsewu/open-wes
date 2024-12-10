import React from "react"
import { Translation } from "react-i18next"
import AbnormalSvg from "@/icon/fontIcons/abnormal.svg" // path to your '*.svg' file.

import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"
import { TipType } from "@/pages/wms/station/instances/outbound/operations/tips/type"

const taskConfig: TabAction = {
    name: <Translation>{(t) => t("button.abnormalRecord")}</Translation>,
    key: "exceptionLog",
    position: "right",
    permissions: [10705],
    modalType: TabActionModalType.NORMAL,
    icon: <AbnormalSvg />,
    disabled: (workStationEvent) => {
        return !workStationEvent?.toolbar?.enableReportAbnormal
    },
    emitter: async (props) => {
        const { onCustomActionDispatch, message } = props
        const { code, msg } = await onCustomActionDispatch({
            eventCode: TipType.REPORT_ABNORMAL_TIP,
            data: "0"
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

export default taskConfig
