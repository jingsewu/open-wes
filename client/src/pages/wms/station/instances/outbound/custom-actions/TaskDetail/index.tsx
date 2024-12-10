import React from "react"
import { Translation } from "react-i18next"

import TaskDetailSvg from "@/icon/fontIcons/task_detail.svg" // path to your '*.svg' file.
import Content from "@/pages/wms/station/instances/outbound/custom-actions/TaskDetail/OutboundTaskDetail"
import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"

const taskConfig: TabAction = {
    name: <Translation>{(t) => t("button.taskDetail")}</Translation>,
    key: "taskDetail",
    position: "left",
    permissions: [10704],
    modalType: TabActionModalType.FULL_SCREEN,
    icon: <TaskDetailSvg />,
    content: (props) => {
        // const info = { skuCode: 'sku1', skuName: 'sku1', containerCode: 'code1', requiredAmount: 3, isScanned: true };
        return <Content />
    },
    modalConfig: {
        title: <Translation>{(t) => t("button.taskDetail")}</Translation>,
        footer: null
    },
    emitter: async (payload) => {
        const { setModalVisible } = payload
        setModalVisible(true)
    }
}

export default taskConfig
