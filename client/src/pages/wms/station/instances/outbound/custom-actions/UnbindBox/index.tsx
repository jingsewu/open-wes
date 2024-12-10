import React from "react"
import { Translation } from "react-i18next"
import ExchangeSvg from "@/icon/fontIcons/unbind.svg" // path to your '*.svg' file.
import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"
import { PutWallDialogWidth } from "@/pages/wms/station/instances/types"

import BatchMultiPutWall from "./batchMultiPutWall"

const unbindBoxConfig: TabAction = {
    name: <Translation>{(t) => t("button.cancelBinding")}</Translation>,
    permissions: [10711],
    key: "unbindBox",
    position: "right",
    modalType: TabActionModalType.NORMAL,
    icon: <ExchangeSvg />,
    disabled: (workStationEvent) => {
        return !workStationEvent?.toolbar?.enableReleaseSlot
    },
    content: (props) => {
        return <BatchMultiPutWall {...props} />
    },
    modalConfig: {
        title: <Translation>{(t) => t("button.cancelBinding")}</Translation>,
        width: PutWallDialogWidth
    },
    emitter: (payload) => {
        // init
        const { setModalVisible } = payload
        setModalVisible(true)
        return Promise.resolve()
    },
    onSubmit: (refs: any, payload) => {
        const { setModalVisible } = payload

        setModalVisible(false)
        return refs.current.onSave()
    }
}

export default unbindBoxConfig
