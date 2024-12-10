import React from "react"
import { Translation } from "react-i18next"
// import { ReactComponent as SplitSvg } from "@/icon/fontIcons/split.svg" // path to your '*.svg' file.
import SplitSvg from "@/icon/fontIcons/split.svg" // path to your '*.svg' file.

import Content from "@/pages/wms/station/instances/outbound/custom-actions/SplitContainer/SplitContent"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"
import { MessageType } from "@/pages/wms/station/widgets/message"
// import IntlMessages from "@/util/IntlMessages"
import { PutWallDialogWidth } from "@/pages/wms/station/instances/types"

const taskConfig: TabAction = {
    name: <Translation>{(t) => t("button.split")}</Translation>,
    // <IntlMessages id="workstaion.outbound.action.unpacking" />,
    key: "splitContainer",
    modalType: TabActionModalType.NORMAL,
    icon: <SplitSvg />,
    position: "right",
    permissions: [10706],
    // icon: () => <SplitSvg width={18} height={18} style={{ color: '#666' }} />,
    content: (props) => {
        return <Content {...props} />
    },
    modalConfig: {
        title: <Translation>{(t) => t("button.split")}</Translation>,
        // <IntlMessages id="workstaion.outbound.action.unpacking" />,
        width: PutWallDialogWidth
    },
    emitter: async (payload) => {
        const { setModalVisible } = payload
        setModalVisible(true)
    },
    disabled: (workStationEvent) => {
        return !workStationEvent?.toolbar?.enableSplitContainer
    },
    onSubmit: async (refs, payload) => {
        const { current } = refs
        const { message, setModalVisible } = payload
        if (current.inputStatus === "error") {
            return false
        }
        const { code, msg } = await current.onCustomActionDispatch({
            eventCode: CustomActionType.SPLIT_TASKS,
            data: {
                operatedQty: current.pickedNumber,
                putWallSlotCode: current.putWallSlotCode
            }
        })
        // if (!errorCode) {
        //     setModalVisible(false)
        // }
        // if (code !== "0") {
        //     message?.({
        //         type: MessageType.ERROR,
        //         content: msg
        //     })
        //     return false
        // }

        return code !== "-1"
    }
}

export default taskConfig
