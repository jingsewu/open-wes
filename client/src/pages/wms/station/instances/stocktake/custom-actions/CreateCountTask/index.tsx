import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"
import React from "react"
import { Translation } from "react-i18next"
import Content from "./Content"

const taskConfig: TabAction = {
    name: <Translation>{(t) => t("button.createCountOrder")}</Translation>,
    key: "createCountOrder",
    modalType: TabActionModalType.FULL_SCREEN,
    icon: "",
    permissions: [10404],
    testid: "createCountOrder",
    content: (props) => {
        return <Content {...props} />
    },
    modalConfig: {
        // title: "领用单据",
        // okText: "领用单据",
        footer: null,
        bodyStyle: {
            overflow: "auto",
            height: "calc(100vh - 55px - 53px)"
        }
    },
    emitter: async (payload) => {
        const { setModalVisible } = payload
        setModalVisible(true)
    },
    onSubmit: (ref: any) => {
        return ref.current?.onSave()
    }
}

export default taskConfig
