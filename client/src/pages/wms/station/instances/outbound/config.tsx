/**
 * 出库工作站配置
 */
import React from "react"
import { Translation } from "react-i18next"
import type { WorkStationConfig } from "../types"
import { DebugType } from "../types"
import { TabActionType } from "../../tab-actions/constant"
import { MessageType } from "../../widgets/message"
import { ChooseArea } from "../../event-loop/types"

import { CustomActionType } from "./customActionType"
import InstanceLayout from "./layout"
import mockData from "./mock-events"

// 操作处理器
import ContainerHandler from "./operations/containerHandler"
import PickingHandler from "./operations/pickingHandler"
import putWallHandler from "./operations/putWallHandler"
import TipsHandler from "./operations/tips"

// 自定义操作
import exceptionLog from "./custom-actions/ExceptionLog"
import SplitContainer from "./custom-actions/SplitContainer"
import taskDetail from "./custom-actions/TaskDetail"
import unbindBoxConfig from "./custom-actions/UnbindBox"

// 操作映射
export const OPERATION_MAP = {
    [ChooseArea.workLocationArea]: ContainerHandler,
    [ChooseArea.skuArea]: PickingHandler,
    [ChooseArea.putWallArea]: putWallHandler,
    [ChooseArea.tips]: TipsHandler
} as const

// 布局样式常量
export const LAYOUT_STYLES = {
    CONTAINER_AREA: { flex: 3, maxWidth: 550, padding: 12 },
    SKU_AREA: { flex: 7, padding: 12 },
} as const

// 创建任务控制操作的辅助函数
const createTaskAction = (actionType: CustomActionType, tabActionType: TabActionType) => ({
    key: tabActionType,
    permissions: [10702],
    emitter: async (props: any) => {
        const result = await props.onActionDispatch({ eventCode: actionType })
        
        if (actionType === CustomActionType.PAUSE && result.code === "-1") {
            props.message?.({
                type: MessageType.ERROR,
                content: result.msg
            })
        }
    }
})

// 工作站配置
const config: WorkStationConfig<any> = {
    type: "outbound",
    title: <Translation>{(t) => t("picking.title")}</Translation>,
    stepsDescribe: [
        {
            type: "containerArea",
            name: <Translation>{(t) => t("outbound.station.step.bindingContainer")}</Translation>
        },
        {
            type: "skuArea",
            name: <Translation>{(t) => t("outbound.station.step.picking")}</Translation>
        },
        {
            type: "putWallArea",
            name: <Translation>{(t) => t("outbound.station.step.dispatch")}</Translation>
        }
    ],
    actions: [
        TabActionType.EXIT,
        createTaskAction(CustomActionType.RESUME, TabActionType.START_TASK),
        createTaskAction(CustomActionType.PAUSE, TabActionType.STOP_TASK),
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
