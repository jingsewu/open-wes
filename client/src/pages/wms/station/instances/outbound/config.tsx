import React from "react"
import { Translation } from "react-i18next"

import exceptionLog from "@/pages/wms/station/instances/outbound/custom-actions/ExceptionLog"
import SplitContainer from "@/pages/wms/station/instances/outbound/custom-actions/SplitContainer"
import taskDetail from "@/pages/wms/station/instances/outbound/custom-actions/TaskDetail"
import unbindBoxConfig from "@/pages/wms/station/instances/outbound/custom-actions/UnbindBox"
import { CustomActionType } from "@/pages/wms/station/instances/outbound/customActionType"
import type { WorkStationConfig } from "@/pages/wms/station/instances/types"
import { DebugType } from "@/pages/wms/station/instances/types"
import { TabActionType } from "@/pages/wms/station/tab-actions/constant"
import { MessageType } from "@/pages/wms/station/widgets/message"

import InstanceLayout from "./layout"
import mockData from "./mock-events"
import ContainerHandler from "./operations/containerHandler"
import PickingHandler from "./operations/pickingHandler"
import putWallHandler from "./operations/putWallHandler"
import TipsHandler from "./operations/tips"
import { ChooseArea } from "@/pages/wms/station/event-loop/types"

export const OPERATION_MAP = {
    [ChooseArea.workLocationArea]: ContainerHandler,
    [ChooseArea.skuArea]: PickingHandler,
    [ChooseArea.putWallArea]: putWallHandler,
    [ChooseArea.tips]: TipsHandler
}

const config: WorkStationConfig<any> = {
    type: "outbound",
    title: <Translation>{(t) => t("picking.title")}</Translation>,
    stepsDescribe: [
        {
            type: "containerArea",
            name: "绑定容器"
        },
        {
            type: "skuArea",
            name: "商品确认"
        },
        {
            type: "putWallArea",
            name: "播种"
        }
    ],
    actions: [
        // 操作按钮
        TabActionType.EXIT,
        {
            key: TabActionType.START_TASK,
            permissions: [10702],
            emitter: async (props) => {
                const { onCustomActionDispatch, message } = props
                const { code, msg } = await onCustomActionDispatch({
                    eventCode: CustomActionType.RESUME
                })
            }
        },
        {
            key: TabActionType.STOP_TASK,
            permissions: [10702],
            emitter: async (props) => {
                const { onCustomActionDispatch, message } = props
                const { code, msg } = await onCustomActionDispatch({
                    eventCode: CustomActionType.PAUSE
                })

                if (code === "-1") {
                    message?.({
                        type: MessageType.ERROR,
                        content: msg
                    })
                    return
                }
            }
        },
        taskDetail,
        unbindBoxConfig,
        SplitContainer,
        exceptionLog
    ],
    operationMap: OPERATION_MAP,
    layout: InstanceLayout,
    debugType: DebugType.STATIC,
    mockData
}

export default config
