import React from "react"
import {Translation} from "react-i18next"
import SplitSvg from "@/icon/fontIcons/split.svg"
import Content from "@/pages/wms/station/instances/outbound/custom-actions/SplitContainer/SplitContent"
import {CustomActionType} from "@/pages/wms/station/instances/outbound/customActionType"
import type {TabAction} from "@/pages/wms/station/tab-actions/types"
import {TabActionModalType} from "@/pages/wms/station/tab-actions/types"
import {PutWallDialogWidth} from "@/pages/wms/station/instances/types"

const taskConfig: TabAction = {
    name: <Translation>{(t) => t("button.split")}</Translation>,
    key: "splitContainer",
    modalType: TabActionModalType.NORMAL,
    icon: <SplitSvg/>,
    position: "right",
    permissions: [10706],
    content: (props) => {
        return <Content {...props} />
    },
    modalConfig: {
        title: <Translation>{(t) => t("button.split")}</Translation>,
        width: PutWallDialogWidth
    },
    emitter: async (payload) => {
        const {setModalVisible} = payload
        setModalVisible(true)
    },
    disabled: (workStationEvent) => {
        return !workStationEvent?.toolbar?.enableSplitContainer
    },
    onSubmit: async (refs, payload) => {
        const {current} = refs
        const {message} = payload
        if (current.inputStatus === "error") {
            return false
        }
        const {code, msg} = await current.onCustomActionDispatch({
            eventCode: CustomActionType.SPLIT_TASKS,
            data: {
                operatedQty: current.pickedNumber,
                putWallSlotCode: current.putWallSlotCode
            }
        })
        return code !== "-1"
    }
}

export default taskConfig
