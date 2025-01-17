import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"
import React from "react"

import Content from "./Content"
import {useTranslation} from "react-i18next";

const { t } = useTranslation();

const TaskDetail: TabAction = {
    name: t("receive.detail.title"),
    key: "TaskDetail",
    position: "left",
    modalType: TabActionModalType.FULL_SCREEN,
    icon: "",
    permissions: [10210],
    content: (props) => {
        return <Content {...props} />
    },
    modalConfig: {
        title: t("receive.detail.title"),
        okText: "",
        footer: null
    },
    emitter: async (payload) => {
        const { setModalVisible } = payload
        setModalVisible(true)
    }
}

export default TaskDetail
