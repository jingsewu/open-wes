import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"
import React from "react"

import Content from "./Content"
import {Translation} from "react-i18next";

const CallContainer: TabAction = {
    name: <Translation>{(t) => t("receive.callContainer.title")}</Translation>,
    key: "CallContainer",
    position: "left",
    modalType: TabActionModalType.FULL_SCREEN,
    icon: "",
    permissions: [10210],
    content: (props) => {
        return <Content {...props} />
    },
    modalConfig: {
        title: <Translation>{(t) => t("receive.callContainer.title")}</Translation>,
        okText: "",
        footer: null
    },
    emitter: async (payload) => {
        const { setModalVisible } = payload
        setModalVisible(true)
    }
}

export default CallContainer
