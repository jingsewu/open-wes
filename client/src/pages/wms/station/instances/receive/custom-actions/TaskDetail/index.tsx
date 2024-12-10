import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"
import React from "react"

import Content from "./Content"

const TaskDetail: TabAction = {
    name: "收货明细",
    key: "TaskDetail",
    position: "left",
    modalType: TabActionModalType.FULL_SCREEN,
    icon: "",
    permissions: [10210],
    content: (props) => {
        return <Content {...props} />
    },
    modalConfig: {
        title: "收货明细",
        okText: "",
        footer: null
    },
    emitter: async (payload) => {
        const { setModalVisible } = payload
        setModalVisible(true)
    }
}

export default TaskDetail
